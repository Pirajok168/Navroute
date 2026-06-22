package dev.pirajok.navroute.runtime

public interface NavRouteMetadataProvider<R : NavRouteDestination> {
    public fun metadata(): Map<String, Any>
}
