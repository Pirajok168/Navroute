package dev.pirajok.navroute.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class NavEntry(
    val route: KClass<*> = GeneratedNavRoute::class,
    val style: KClass<out DestinationStyle> = DestinationStyle.Screen::class,
    val deepLinks: Array<NavDeepLink> = [],
)

public class GeneratedNavRoute private constructor()
