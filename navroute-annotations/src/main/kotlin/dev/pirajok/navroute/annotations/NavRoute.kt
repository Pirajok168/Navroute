package dev.pirajok.navroute.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
public annotation class NavRoute

public abstract class DestinationStyle {
    public open fun metadata(): Map<String, Any> = emptyMap()

    public object Screen : DestinationStyle()

    public abstract class Dialog : DestinationStyle()

    public abstract class BottomSheet : DestinationStyle()

    public abstract class ModalNavigationDrawer : DestinationStyle()
}
