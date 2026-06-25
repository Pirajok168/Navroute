package dev.pirajok.navroute.deeplink

import android.net.Uri
import dev.pirajok.navroute.runtime.NavRouteDestination
import java.io.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.encoding.CompositeDecoder

public class DeepLinkPattern<T : NavRouteDestination>(
    public val serializer: KSerializer<T>,
    public val uriPattern: Uri,
) {

    public val pathSegments: List<PathSegment> = buildList {
        uriPattern.pathSegments.forEach { segment ->
            val result = ARGUMENT_PATTERN.find(segment)
            if (result != null) {
                val argName = result.groups[1]!!.value
                val elementIndex = serializer.descriptor.getElementIndex(argName)
                if (elementIndex == CompositeDecoder.UNKNOWN_NAME) {
                    throw IllegalArgumentException(
                        "Path parameter '{$argName}' defined in the DeepLink $uriPattern does not " +
                            "exist in the Serializable class '${serializer.descriptor.serialName}'.",
                    )
                }

                val elementDescriptor = serializer.descriptor.getElementDescriptor(elementIndex)
                add(PathSegment(argName, isArgument = true, typeParser = getTypeParser(elementDescriptor.kind)))
            } else {
                add(PathSegment(segment, isArgument = false, typeParser = getTypeParser(PrimitiveKind.STRING)))
            }
        }
    }

    public val queryValueParsers: Map<String, TypeParser> = buildMap {
        uriPattern.queryParameterNames.forEach { paramName ->
            val elementIndex = serializer.descriptor.getElementIndex(paramName)
            if (elementIndex != CompositeDecoder.UNKNOWN_NAME) {
                val elementDescriptor = serializer.descriptor.getElementDescriptor(elementIndex)
                this[paramName] = getTypeParser(elementDescriptor.kind)
            }
        }
    }

    public class PathSegment(
        public val stringValue: String,
        public val isArgument: Boolean,
        public val typeParser: TypeParser,
    )

    private companion object {
        val ARGUMENT_PATTERN: Regex = Regex("\\{(.+?)\\}")
    }
}

public typealias TypeParser = (String) -> Serializable

internal fun getTypeParser(kind: SerialKind): TypeParser =
    when (kind) {
        PrimitiveKind.STRING -> { value -> value }
        PrimitiveKind.INT -> String::toInt
        PrimitiveKind.BOOLEAN -> String::toBooleanStrict
        PrimitiveKind.BYTE -> String::toByte
        PrimitiveKind.CHAR -> { value ->
            require(value.length == 1) { "Expected a single character, but was '$value'." }
            value.single()
        }
        PrimitiveKind.DOUBLE -> String::toDouble
        PrimitiveKind.FLOAT -> String::toFloat
        PrimitiveKind.LONG -> String::toLong
        PrimitiveKind.SHORT -> String::toShort
        else -> throw IllegalArgumentException(
            "Unsupported argument type of SerialKind:$kind. The argument type must be a primitive.",
        )
    }
