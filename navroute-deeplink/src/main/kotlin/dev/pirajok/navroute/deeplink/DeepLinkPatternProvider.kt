package dev.pirajok.navroute.deeplink

import dev.pirajok.navroute.runtime.NavRouteDestination

public interface DeepLinkPatternProvider {
    public val patterns: List<DeepLinkPattern<out NavRouteDestination>>
}
