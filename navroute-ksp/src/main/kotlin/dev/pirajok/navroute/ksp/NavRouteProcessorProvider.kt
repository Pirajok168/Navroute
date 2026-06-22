package dev.pirajok.navroute.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.symbol.Variance
import java.io.OutputStreamWriter

public class NavRouteProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        NavRouteProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options,
        )
}

private class NavRouteProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {

    private var processed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (processed) return emptyList()
        processed = true

        val diMode = validateDiMode()

        val entryDeclarations = resolver
            .getSymbolsWithAnnotation(NAV_ENTRY_ANNOTATION)
            .filterIsInstance<KSFunctionDeclaration>()
            .toList()

        val entryModels = entryDeclarations.mapNotNull { entry ->
            val routeDeclaration = entry.navEntryRouteDeclaration()
            if (routeDeclaration == null || routeDeclaration.qualifiedName?.asString() == GENERATED_ROUTE_MARKER_FQ_NAME) {
                createGeneratedEntryModel(entry)
            } else {
                createDeclaredEntryModel(resolver, entry, routeDeclaration)
            }
        }

        val duplicateRoutes = entryModels
            .groupBy { it.route.qualifiedName }
            .filterValues { it.size > 1 }
            .keys
        duplicateRoutes.forEach { routeName ->
            logger.error("NavRoute: more than one @NavEntry was found for route '$routeName'.")
        }

        entryModels
            .groupBy { it.generatedPackageName }
            .forEach { (packageName, entries) ->
                when (diMode) {
                    DiMode.NONE -> generateEntryRegistry(packageName, entries)
                    DiMode.HILT -> {
                        generateEntryRegistry(packageName, entries)
                        generateHiltEntryBuilderModule(packageName, entries)
                        generateDeepLinkPatternProviderForEntries(packageName, entries, diMode)
                    }
                    DiMode.KOIN -> {
                        generateKoinEntryBuilderModule(packageName, entries)
                        generateDeepLinkPatternProviderForEntries(packageName, entries, diMode)
                    }
                    else -> Unit
                }
            }

        return emptyList()
    }

    private fun validateDiMode(): DiMode? {
        return when (val diMode = options[DI_MODE_OPTION]) {
            "none" -> DiMode.NONE
            "hilt" -> DiMode.HILT
            "koin" -> DiMode.KOIN
            null -> {
                logger.error("NavRoute: KSP option 'navroute.di' must be set explicitly to one of: none, hilt, koin.")
                null
            }
            else -> {
                logger.error("NavRoute: unsupported navroute.di='$diMode'. Expected one of: none, hilt, koin.")
                null
            }
        }
    }

    private fun validateRoute(resolver: Resolver, route: KSClassDeclaration) {
        val destinationFqName = options[DESTINATION_FQ_NAME_OPTION] ?: DEFAULT_DESTINATION_FQ_NAME
        val destination = resolver.getClassDeclarationByName(resolver.getKSNameFromString(destinationFqName))
        if (destination == null) {
            logger.error("NavRoute: destination contract '$destinationFqName' was not found.", route)
            return
        }

        if (!destination.asStarProjectedType().isAssignableFrom(route.asStarProjectedType())) {
            logger.error(
                "NavRoute: '${route.qualifiedName?.asString()}' must implement '$destinationFqName'.",
                route,
            )
        }

        if (!route.hasAnnotation(SERIALIZABLE_ANNOTATION)) {
            logger.error("NavRoute: '${route.qualifiedName?.asString()}' must be annotated with @Serializable.", route)
        }
    }

    private fun validateDeepLinks(route: RouteModel, node: KSAnnotated) {
        if (route.deepLinks.isEmpty()) return

        val parametersByName = route.parameters.associateBy { it.name }
        route.deepLinks.forEach { deepLink ->
            deepLink.pathArgumentNames.forEach { argumentName ->
                val parameter = parametersByName[argumentName]
                if (parameter == null) {
                    logger.error(
                        "NavRoute: deep link '${deepLink.uriPattern}' references path argument '{$argumentName}', " +
                            "but route '${route.qualifiedName}' has no parameter named '$argumentName'.",
                        node,
                    )
                    return@forEach
                }

                if (!parameter.isSupportedDeepLinkType) {
                    logger.error(
                        "NavRoute: deep link argument '{$argumentName}' in '${deepLink.uriPattern}' has unsupported " +
                            "type '${parameter.typeName}'. Supported deep link argument types are primitive Kotlin types.",
                        node,
                    )
                }
            }

            deepLink.queryParameterNames.forEach { parameterName ->
                val parameter = parametersByName[parameterName] ?: return@forEach
                if (!parameter.isSupportedDeepLinkType) {
                    logger.error(
                        "NavRoute: deep link query parameter '$parameterName' in '${deepLink.uriPattern}' has unsupported " +
                            "type '${parameter.typeName}'. Supported deep link argument types are primitive Kotlin types.",
                        node,
                    )
                }
            }
        }
    }

    private fun createDeclaredEntryModel(
        resolver: Resolver,
        entry: KSFunctionDeclaration,
        routeDeclaration: KSClassDeclaration,
    ): EntryModel? {
        if (!validateEntry(entry)) return null

        if (!routeDeclaration.hasAnnotation(NAV_ROUTE_ANNOTATION)) {
            logger.error(
                "NavRoute: route '${routeDeclaration.qualifiedName?.asString()}' referenced by @NavEntry is not annotated with @NavRoute.",
                entry,
            )
            return null
        }

        validateRoute(resolver, routeDeclaration)
        val route = RouteModel.from(
            declaration = routeDeclaration,
            deepLinks = entry.deepLinkPatterns(NAV_ENTRY_ANNOTATION),
        )
        validateDeepLinks(route, entry)
        val screen = ScreenModel.from(entry)
        val call = createScreenCall(route, screen, entry) ?: return null
        val moduleName = options[MODULE_NAME_OPTION] ?: inferModuleName(entry.packageName.asString())
        val entryStyle = entry.annotationClassArgument(NAV_ENTRY_ANNOTATION, "style")
        val selectedStyle = selectEntryStyle(entryStyle)

        return EntryModel(
            route = route,
            screen = screen,
            screenCall = call,
            style = selectedStyle,
            generatedPackageName = "${entry.packageName.asString()}.generated",
            moduleName = moduleName.toPascalCase(),
            originatingFiles = listOfNotNull(entry.containingFile, routeDeclaration.containingFile),
        )
    }

    private fun createGeneratedEntryModel(entry: KSFunctionDeclaration): EntryModel? {
        if (!validateEntry(entry)) return null

        val screen = ScreenModel.from(entry)
        val generatedPackageName = "${entry.packageName.asString()}.generated"
        val route = RouteModel.generatedFrom(
            packageName = generatedPackageName,
            simpleName = "${entry.simpleName.asString()}Route",
            parameters = screen.parameters,
            deepLinks = entry.deepLinkPatterns(NAV_ENTRY_ANNOTATION),
            originatingFile = entry.containingFile,
        )
        validateDeepLinks(route, entry)
        generateGeneratedRoute(route)
        val call = createScreenCall(route, screen, entry) ?: return null
        val moduleName = options[MODULE_NAME_OPTION] ?: inferModuleName(entry.packageName.asString())
        val entryStyle = entry.annotationClassArgument(NAV_ENTRY_ANNOTATION, "style")
        val selectedStyle = selectEntryStyle(entryStyle)

        return EntryModel(
            route = route,
            screen = screen,
            screenCall = call,
            style = selectedStyle,
            generatedPackageName = generatedPackageName,
            moduleName = moduleName.toPascalCase(),
            originatingFiles = listOfNotNull(entry.containingFile),
        )
    }

    private fun validateEntry(entry: KSFunctionDeclaration): Boolean {
        if (Modifier.PRIVATE in entry.modifiers) {
            logger.error("NavRoute: @NavEntry function '${entry.simpleName.asString()}' must be visible to generated code.", entry)
            return false
        }

        if (!entry.hasAnnotation(COMPOSABLE_ANNOTATION)) {
            logger.error("NavRoute: @NavEntry function '${entry.simpleName.asString()}' must be annotated with @Composable.", entry)
            return false
        }

        return true
    }

    private fun generateGeneratedRoute(route: RouteModel) {
        val body = buildString {
            appendLine("package ${route.packageName}")
            appendLine()
            appendLine("import dev.pirajok.navroute.runtime.NavRouteDestination")
            appendLine("import kotlinx.serialization.Serializable")
            appendLine()
            appendLine("@Serializable")
            if (route.isObject) {
                appendLine("public data object ${route.simpleName} : NavRouteDestination")
            } else {
                appendLine("public data class ${route.simpleName}(")
                route.parameters.forEachIndexed { index, parameter ->
                    val comma = if (index == route.parameters.lastIndex) "" else ","
                    appendLine("    public val ${parameter.name}: ${parameter.typeName}$comma")
                }
                appendLine(") : NavRouteDestination")
            }
        }

        writeSource(
            packageName = route.packageName,
            fileName = route.simpleName,
            body = body,
            dependencies = Dependencies(false, *listOfNotNull(route.originatingFile).toTypedArray()),
        )
    }

    private fun selectEntryStyle(entryStyle: StyleModel?): StyleModel? =
        entryStyle?.takeUnless { it.isScreen }

    private fun createScreenCall(route: RouteModel, screen: ScreenModel, node: KSAnnotated): String? {
        if (route.isObject) {
            if (screen.parameters.isNotEmpty()) {
                logger.error("NavRoute: object route '${route.qualifiedName}' must be bound to a no-arg screen.", node)
                return null
            }
            return "${screen.qualifiedName}()"
        }

        if (screen.parameters.size == route.parameters.size) {
            val compatible = screen.parameters.zip(route.parameters).all { (screenParameter, routeParameter) ->
                screenParameter.typeName == routeParameter.typeName
            }
            if (compatible) {
                return buildString {
                    appendLine("${screen.qualifiedName}(")
                    screen.parameters.zip(route.parameters).forEachIndexed { index, (screenParameter, routeParameter) ->
                        val comma = if (index == screen.parameters.lastIndex) "" else ","
                        appendLine("            ${screenParameter.name} = route.${routeParameter.name}$comma")
                    }
                    append("        )")
                }
            }
        }

        logger.error(
            "NavRoute: screen '${screen.qualifiedName}' signature is not compatible with route '${route.qualifiedName}'. " +
                "Use no args for object routes or direct route parameters in the same order and compatible types.",
            node,
        )
        return null
    }

    private fun generateEntryRegistry(packageName: String, entries: List<EntryModel>) {
        if (entries.isEmpty()) return

        val moduleName = entries.first().moduleName
        val builderName = "generated${moduleName}EntryBuilder"
        val registryName = "Generated${moduleName}NavRouteRegistry"
        val imports = ImportCollector(packageName)
        imports.reference("androidx.navigation3.runtime.EntryProviderScope")
        imports.reference("androidx.navigation3.runtime.NavKey")
        imports.reference("dev.pirajok.navroute.runtime.EntryBuilder")
        entries.forEach { entry ->
            imports.reference(entry.route.qualifiedName)
            imports.reference(entry.screen.qualifiedName)
            entry.style?.let { imports.reference(it.qualifiedName) }
        }
        val body = buildString {
            appendLine("package $packageName")
            appendLine()
            appendImports(imports)
            appendLine("public fun ${imports.render("androidx.navigation3.runtime.EntryProviderScope")}<${imports.render("androidx.navigation3.runtime.NavKey")}>.$builderName() {")
            entries.forEach { entry ->
                val routeName = imports.render(entry.route.qualifiedName)
                if (entry.style != null) {
                    appendLine("    entry<$routeName>(")
                    appendLine("        metadata = ${entry.style.metadataExpression(imports)},")
                    appendLine("    ) { route ->")
                } else {
                    appendLine("    entry<$routeName> { route ->")
                }
                append("        ")
                appendLine(imports.renderReferences(entry.screenCall))
                appendLine("    }")
            }
            appendLine("}")
            appendLine()
            appendLine("public object $registryName {")
            appendLine("    public val entryBuilder: ${imports.render("dev.pirajok.navroute.runtime.EntryBuilder")} = {")
            appendLine("        $builderName()")
            appendLine("    }")
            appendLine("}")
        }

        writeSource(
            packageName = packageName,
            fileName = registryName,
            body = body,
            dependencies = Dependencies(true, *entries.flatMap { it.originatingFiles }.distinct().toTypedArray()),
        )
    }

    private fun generateHiltEntryBuilderModule(packageName: String, entries: List<EntryModel>) {
        if (entries.isEmpty()) return

        val moduleName = entries.first().moduleName
        val registryName = "Generated${moduleName}NavRouteRegistry"
        val hiltModuleName = "Generated${moduleName}EntryBuilderModule"
        val providerName = "provideGenerated${moduleName}EntryBuilder"
        val body = buildString {
            appendLine("package $packageName")
            appendLine()
            appendLine("import dagger.Module")
            appendLine("import dagger.Provides")
            appendLine("import dagger.hilt.InstallIn")
            appendLine("import dagger.hilt.components.SingletonComponent")
            appendLine("import dagger.multibindings.IntoSet")
            appendLine("import dev.pirajok.navroute.runtime.EntryBuilder")
            appendLine()
            appendLine("@Module")
            appendLine("@InstallIn(SingletonComponent::class)")
            appendLine("public object $hiltModuleName {")
            appendLine()
            appendLine("    @Provides")
            appendLine("    @IntoSet")
            appendLine("    public fun $providerName(): EntryBuilder =")
            appendLine("        $registryName.entryBuilder")
            appendLine("}")
        }

        writeSource(
            packageName = packageName,
            fileName = hiltModuleName,
            body = body,
            dependencies = Dependencies(true, *entries.flatMap { it.originatingFiles }.distinct().toTypedArray()),
        )
    }

    private fun generateKoinEntryBuilderModule(packageName: String, entries: List<EntryModel>) {
        if (entries.isEmpty()) return

        val moduleName = entries.first().moduleName
        val koinModuleName = "generated${moduleName}EntryBuilderKoinModule"
        val imports = ImportCollector(packageName)
        imports.reference("org.koin.core.annotation.KoinExperimentalAPI")
        imports.reference("org.koin.dsl.module")
        imports.reference("org.koin.dsl.navigation3.navigation")
        entries.forEach { entry ->
            imports.reference(entry.route.qualifiedName)
            imports.reference(entry.screen.qualifiedName)
            entry.style?.let { imports.reference(it.qualifiedName) }
        }
        val body = buildString {
            appendLine("package $packageName")
            appendLine()
            appendImports(imports)
            appendLine("@OptIn(${imports.render("org.koin.core.annotation.KoinExperimentalAPI")}::class)")
            appendLine("public val $koinModuleName = module {")
            entries.forEach { entry ->
                val routeName = imports.render(entry.route.qualifiedName)
                if (entry.style != null) {
                    appendLine("    navigation<$routeName>(")
                    appendLine("        metadata = ${entry.style.metadataExpression(imports)},")
                    appendLine("    ) { route ->")
                } else {
                    appendLine("    navigation<$routeName> { route ->")
                }
                append("        ")
                appendLine(imports.renderReferences(entry.screenCall))
                appendLine("    }")
            }
            appendLine("}")
        }

        writeSource(
            packageName = packageName,
            fileName = "Generated${moduleName}EntryBuilderKoinModule",
            body = body,
            dependencies = Dependencies(true, *entries.flatMap { it.originatingFiles }.distinct().toTypedArray()),
        )
    }

    private fun generateDeepLinkPatternProviderForEntries(
        packageName: String,
        entries: List<EntryModel>,
        diMode: DiMode,
    ) {
        val routes = entries
            .map { it.route }
            .filter { it.deepLinks.isNotEmpty() }
            .distinctBy { it.qualifiedName }

        if (routes.isEmpty()) return

        generateDeepLinkPatternProvider(packageName, routes)
        when (diMode) {
            DiMode.HILT -> generateHiltDeepLinkPatternProviderModule(packageName, routes)
            DiMode.KOIN -> generateKoinDeepLinkPatternProviderModule(packageName, routes)
            else -> Unit
        }
    }

    private fun generateDeepLinkPatternProvider(packageName: String, routes: List<RouteModel>) {
        if (routes.isEmpty()) return

        val moduleName = (options[MODULE_NAME_OPTION] ?: inferModuleName(routes.first().packageName)).toPascalCase()
        val providerName = "Generated${moduleName}DeepLinkPatternProvider"
        val imports = ImportCollector(packageName)
        imports.reference("android.net.Uri")
        imports.reference("dev.pirajok.navroute.deeplink.DeepLinkPattern")
        imports.reference("dev.pirajok.navroute.deeplink.DeepLinkPatternProvider")
        imports.reference("dev.pirajok.navroute.runtime.NavRouteDestination")
        routes.forEach { route -> imports.reference(route.qualifiedName) }
        val body = buildString {
            appendLine("package $packageName")
            appendLine()
            appendImports(imports)
            appendLine("public object $providerName : ${imports.render("dev.pirajok.navroute.deeplink.DeepLinkPatternProvider")} {")
            appendLine("    override val patterns: List<${imports.render("dev.pirajok.navroute.deeplink.DeepLinkPattern")}<out ${imports.render("dev.pirajok.navroute.runtime.NavRouteDestination")}>> = listOf(")
            routes.forEach { route ->
                route.deepLinks.forEach { deepLink ->
                    appendLine("        DeepLinkPattern(")
                    appendLine("            serializer = ${imports.render(route.qualifiedName)}.serializer(),")
                    appendLine("            uriPattern = ${imports.render("android.net.Uri")}.parse(${deepLink.uriPattern.toKotlinStringLiteral()}),")
                    appendLine("        ),")
                }
            }
            appendLine("    )")
            appendLine("}")
        }

        writeSource(
            packageName = packageName,
            fileName = providerName,
            body = body,
            dependencies = Dependencies(true, *routes.mapNotNull { it.originatingFile }.distinct().toTypedArray()),
        )
    }

    private fun generateHiltDeepLinkPatternProviderModule(packageName: String, routes: List<RouteModel>) {
        if (routes.isEmpty()) return

        val moduleName = (options[MODULE_NAME_OPTION] ?: inferModuleName(routes.first().packageName)).toPascalCase()
        val providerName = "Generated${moduleName}DeepLinkPatternProvider"
        val hiltModuleName = "Generated${moduleName}DeepLinkPatternProviderModule"
        val providerMethodName = "provideGenerated${moduleName}DeepLinkPatternProvider"
        val body = buildString {
            appendLine("package $packageName")
            appendLine()
            appendLine("import dagger.Module")
            appendLine("import dagger.Provides")
            appendLine("import dagger.hilt.InstallIn")
            appendLine("import dagger.hilt.components.SingletonComponent")
            appendLine("import dagger.multibindings.IntoSet")
            appendLine("import dev.pirajok.navroute.deeplink.DeepLinkPatternProvider")
            appendLine()
            appendLine("@Module")
            appendLine("@InstallIn(SingletonComponent::class)")
            appendLine("public object $hiltModuleName {")
            appendLine()
            appendLine("    @Provides")
            appendLine("    @IntoSet")
            appendLine("    public fun $providerMethodName(): DeepLinkPatternProvider =")
            appendLine("        $providerName")
            appendLine("}")
        }

        writeSource(
            packageName = packageName,
            fileName = hiltModuleName,
            body = body,
            dependencies = Dependencies(true, *routes.mapNotNull { it.originatingFile }.distinct().toTypedArray()),
        )
    }

    private fun generateKoinDeepLinkPatternProviderModule(packageName: String, routes: List<RouteModel>) {
        if (routes.isEmpty()) return

        val moduleName = (options[MODULE_NAME_OPTION] ?: inferModuleName(routes.first().packageName)).toPascalCase()
        val providerName = "Generated${moduleName}DeepLinkPatternProvider"
        val koinModuleName = "generated${moduleName}DeepLinkPatternProviderKoinModule"
        val qualifierName = "Generated${moduleName}DeepLinkPatternProvider"
        val body = buildString {
            appendLine("package $packageName")
            appendLine()
            appendLine("import dev.pirajok.navroute.deeplink.DeepLinkPatternProvider")
            appendLine("import org.koin.core.qualifier.named")
            appendLine("import org.koin.dsl.module")
            appendLine()
            appendLine("public val $koinModuleName = module {")
            appendLine("    single<DeepLinkPatternProvider>(qualifier = named(${qualifierName.toKotlinStringLiteral()})) {")
            appendLine("        $providerName")
            appendLine("    }")
            appendLine("}")
        }

        writeSource(
            packageName = packageName,
            fileName = "Generated${moduleName}DeepLinkPatternProviderKoinModule",
            body = body,
            dependencies = Dependencies(true, *routes.mapNotNull { it.originatingFile }.distinct().toTypedArray()),
        )
    }

    private fun writeSource(packageName: String, fileName: String, body: String, dependencies: Dependencies) {
        codeGenerator.createNewFile(
            dependencies = dependencies,
            packageName = packageName,
            fileName = fileName,
        ).use { output ->
            OutputStreamWriter(output, Charsets.UTF_8).use { writer ->
                writer.write(body)
            }
        }
    }

    private fun KSDeclaration.annotationClassArgument(annotationQualifiedName: String, argumentName: String): StyleModel? {
        val declaration = annotations
            .firstOrNull { it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationQualifiedName }
            ?.arguments
            ?.firstOrNull { it.name?.asString() == argumentName }
            ?.value
            ?.let { it as? com.google.devtools.ksp.symbol.KSType }
            ?.declaration as? KSClassDeclaration
            ?: return null

        return StyleModel(
            qualifiedName = declaration.qualifiedName?.asString() ?: return null,
            isObject = declaration.classKind == ClassKind.OBJECT,
        )
    }

    private fun KSFunctionDeclaration.navEntryRouteDeclaration(): KSClassDeclaration? =
        annotations
            .firstOrNull { it.annotationType.resolve().declaration.qualifiedName?.asString() == NAV_ENTRY_ANNOTATION }
            ?.arguments
            ?.firstOrNull { it.name?.asString() == "route" }
            ?.value
            ?.let { it as? com.google.devtools.ksp.symbol.KSType }
            ?.declaration as? KSClassDeclaration

    private fun KSDeclaration.deepLinkPatterns(
        annotationQualifiedName: String = NAV_ROUTE_ANNOTATION,
    ): List<DeepLinkModel> {
        val navRouteDeepLinks = annotations
            .firstOrNull { it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationQualifiedName }
            ?.arguments
            ?.firstOrNull { it.name?.asString() == "deepLinks" }
            ?.value
            ?.let { value -> value as? List<*> }
            .orEmpty()
            .filterIsInstance<com.google.devtools.ksp.symbol.KSAnnotation>()
            .mapNotNull { annotation ->
                annotation.stringArgument("uriPattern")?.let(::DeepLinkModel)
            }

        val repeatableDeepLinks = annotations
            .filter { it.annotationType.resolve().declaration.qualifiedName?.asString() == NAV_DEEP_LINK_ANNOTATION }
            .mapNotNull { annotation ->
                annotation.stringArgument("uriPattern")?.let(::DeepLinkModel)
            }

        return (navRouteDeepLinks + repeatableDeepLinks).distinct()
    }

    private fun com.google.devtools.ksp.symbol.KSAnnotation.stringArgument(argumentName: String): String? =
        arguments
            .firstOrNull { it.name?.asString() == argumentName }
            ?.value as? String

    private fun inferModuleName(packageName: String): String =
        packageName
            .split('.')
            .lastOrNull { it.isNotBlank() && it != "api" && it != "impl" && it != "screen" && it != "screens" }
            ?: "Feature"

    private fun String.toPascalCase(): String =
        split('-', '_', '.')
            .filter { it.isNotBlank() }
            .joinToString(separator = "") { part ->
                part.replaceFirstChar { char -> char.uppercaseChar() }
            }
            .ifBlank { "Feature" }

    private data class RouteModel(
        val qualifiedName: String,
        val simpleName: String,
        val packageName: String,
        val isObject: Boolean,
        val parameters: List<ParameterModel>,
        val deepLinks: List<DeepLinkModel>,
        val originatingFile: KSFile?,
    ) {
        val generatedDeepLinkPackageName: String = "$packageName.generated"

        companion object {
            fun from(
                declaration: KSClassDeclaration,
                deepLinks: List<DeepLinkModel> = emptyList(),
            ): RouteModel {
                val parameters = declaration.primaryConstructor
                    ?.parameters
                    .orEmpty()
                    .map { parameter ->
                        ParameterModel(
                            name = parameter.requireName(),
                            typeName = parameter.type.resolve().render(),
                        )
                    }
                return RouteModel(
                    qualifiedName = declaration.qualifiedName!!.asString(),
                    simpleName = declaration.simpleName.asString(),
                    packageName = declaration.packageName.asString(),
                    isObject = declaration.classKind == ClassKind.OBJECT,
                    parameters = parameters,
                    deepLinks = deepLinks,
                    originatingFile = declaration.containingFile,
                )
            }

            fun generatedFrom(
                packageName: String,
                simpleName: String,
                parameters: List<ParameterModel>,
                deepLinks: List<DeepLinkModel>,
                originatingFile: KSFile?,
            ): RouteModel =
                RouteModel(
                    qualifiedName = "$packageName.$simpleName",
                    simpleName = simpleName,
                    packageName = packageName,
                    isObject = parameters.isEmpty(),
                    parameters = parameters,
                    deepLinks = deepLinks,
                    originatingFile = originatingFile,
                )
        }
    }

    private data class ScreenModel(
        val qualifiedName: String,
        val parameters: List<ParameterModel>,
    ) {
        companion object {
            fun from(declaration: KSFunctionDeclaration): ScreenModel =
                ScreenModel(
                    qualifiedName = "${declaration.packageName.asString()}.${declaration.simpleName.asString()}",
                    parameters = declaration.parameters.map { parameter ->
                        ParameterModel(
                            name = parameter.requireName(),
                            typeName = parameter.type.resolve().render(),
                        )
                    },
                )
        }
    }

    private data class EntryModel(
        val route: RouteModel,
        val screen: ScreenModel,
        val screenCall: String,
        val style: StyleModel?,
        val generatedPackageName: String,
        val moduleName: String,
        val originatingFiles: List<KSFile>,
    )

    private data class ParameterModel(
        val name: String,
        val typeName: String,
    ) {
        val isSupportedDeepLinkType: Boolean =
            typeName.removeSuffix("?") in SUPPORTED_DEEP_LINK_TYPES
    }

    private data class DeepLinkModel(
        val uriPattern: String,
    ) {
        val pathArgumentNames: Set<String> =
            PATH_ARGUMENT_PATTERN
                .findAll(uriPattern.substringBefore('?'))
                .map { match -> match.groupValues[1] }
                .toSet()

        val queryParameterNames: Set<String> =
            uriPattern
                .substringAfter('?', missingDelimiterValue = "")
                .split('&')
                .asSequence()
                .mapNotNull { query ->
                    query.substringBefore('=').takeIf { it.isNotBlank() }
                }
                .toSet()
    }

    private data class StyleModel(
        val qualifiedName: String,
        val isObject: Boolean,
    ) {
        val isScreen: Boolean = qualifiedName == SCREEN_STYLE_FQ_NAME

        fun metadataExpression(imports: ImportCollector): String =
            if (isObject) {
                "${imports.render(qualifiedName)}.metadata()"
            } else {
                "${imports.render(qualifiedName)}().metadata()"
            }
    }

    private enum class DiMode {
        NONE,
        HILT,
        KOIN,
    }

    private companion object {
        const val NAV_ROUTE_ANNOTATION = "dev.pirajok.navroute.annotations.NavRoute"
        const val NAV_ENTRY_ANNOTATION = "dev.pirajok.navroute.annotations.NavEntry"
        const val NAV_DEEP_LINK_ANNOTATION = "dev.pirajok.navroute.annotations.NavDeepLink"
        const val COMPOSABLE_ANNOTATION = "androidx.compose.runtime.Composable"
        const val SERIALIZABLE_ANNOTATION = "kotlinx.serialization.Serializable"
        const val SCREEN_STYLE_FQ_NAME = "dev.pirajok.navroute.annotations.DestinationStyle.Screen"
        const val GENERATED_ROUTE_MARKER_FQ_NAME = "dev.pirajok.navroute.annotations.GeneratedNavRoute"
        const val DI_MODE_OPTION = "navroute.di"
        const val DESTINATION_FQ_NAME_OPTION = "navroute.destinationFqName"
        const val MODULE_NAME_OPTION = "navroute.moduleName"
        const val DEFAULT_DESTINATION_FQ_NAME = "dev.pirajok.navroute.runtime.NavRouteDestination"
        val PATH_ARGUMENT_PATTERN = Regex("\\{(.+?)\\}")
        val SUPPORTED_DEEP_LINK_TYPES = setOf(
            "kotlin.String",
            "kotlin.Int",
            "kotlin.Boolean",
            "kotlin.Byte",
            "kotlin.Char",
            "kotlin.Double",
            "kotlin.Float",
            "kotlin.Long",
            "kotlin.Short",
        )
    }
}

private fun KSDeclaration.hasAnnotation(qualifiedName: String): Boolean =
    annotations.any { it.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName }

private class ImportCollector(
    private val packageName: String,
) {
    private val importsByQualifiedName = linkedMapOf<String, String>()

    fun reference(qualifiedName: String): String {
        val simpleName = qualifiedName.substringAfterLast('.')
        if (!qualifiedName.contains('.') || qualifiedName.substringBeforeLast('.') == packageName) {
            return simpleName
        }

        val existing = importsByQualifiedName[qualifiedName]
        if (existing != null) return existing

        val usedNames = importsByQualifiedName.values.toSet()
        val alias = if (simpleName in usedNames) {
            qualifiedName
                .split('.')
                .filter { it.isNotBlank() }
                .takeLast(3)
                .joinToString(separator = "") { part -> part.replaceFirstChar { char -> char.uppercaseChar() } }
        } else {
            simpleName
        }

        importsByQualifiedName[qualifiedName] = alias
        return alias
    }

    fun render(qualifiedName: String): String =
        reference(qualifiedName)

    fun renderReferences(source: String): String =
        importsByQualifiedName
            .entries
            .sortedByDescending { it.key.length }
            .fold(source) { result, (qualifiedName, alias) ->
                result.replace(qualifiedName, alias)
            }

    fun importLines(): List<String> =
        importsByQualifiedName
            .entries
            .sortedBy { it.key }
            .map { (qualifiedName, alias) ->
                val simpleName = qualifiedName.substringAfterLast('.')
                if (alias == simpleName) {
                    "import $qualifiedName"
                } else {
                    "import $qualifiedName as $alias"
                }
            }
}

private fun StringBuilder.appendImports(imports: ImportCollector) {
    val importLines = imports.importLines()
    if (importLines.isEmpty()) return

    importLines.forEach(::appendLine)
    appendLine()
}

private fun KSValueParameter.requireName(): String =
    name?.asString() ?: error("Constructor and screen parameters must be named.")

private fun com.google.devtools.ksp.symbol.KSType.render(): String {
    val declarationName = declaration.qualifiedName?.asString() ?: declaration.simpleName.asString()
    val renderedArguments = arguments
        .takeIf { it.isNotEmpty() }
        ?.joinToString(prefix = "<", postfix = ">") { argument ->
            val variancePrefix = when (argument.variance) {
                Variance.COVARIANT -> "out "
                Variance.CONTRAVARIANT -> "in "
                Variance.STAR -> "*"
                else -> ""
            }
            if (argument.variance == Variance.STAR) {
                variancePrefix
            } else {
                variancePrefix + (argument.type?.resolve()?.render() ?: "*")
            }
        }
        .orEmpty()
    val nullableSuffix = if (nullability == Nullability.NULLABLE) "?" else ""
    return declarationName + renderedArguments + nullableSuffix
}

private fun String.toKotlinStringLiteral(): String =
    buildString {
        append('"')
        this@toKotlinStringLiteral.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
        append('"')
    }

private fun KSClassDeclaration.containingFileOrNull(): KSFile =
    containingFile ?: error("Expected '${qualifiedName?.asString()}' to have a containing file.")
