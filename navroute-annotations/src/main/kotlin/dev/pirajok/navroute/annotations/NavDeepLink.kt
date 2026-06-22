package dev.pirajok.navroute.annotations

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
public annotation class NavDeepLink(
    val uriPattern: String,
)
