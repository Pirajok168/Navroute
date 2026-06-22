package dev.pirajok.navroute.deeplink

import android.content.Intent
import android.net.Uri
import dev.pirajok.navroute.runtime.NavRouteDestination

public class DeepLinkResolver(
    providers: Iterable<DeepLinkPatternProvider>,
) {
    private val patterns: List<DeepLinkPattern<out NavRouteDestination>> =
        providers.flatMap { provider -> provider.patterns }

    public constructor(vararg providers: DeepLinkPatternProvider) : this(providers.asIterable())

    public fun resolve(intent: Intent): NavRouteDestination? =
        intent.data?.let(::resolve)

    public fun resolve(uri: Uri): NavRouteDestination? =
        resolve(DeepLinkRequest(uri))

    public fun resolve(request: DeepLinkRequest): NavRouteDestination? =
        patterns.firstNotNullOfOrNull { pattern ->
            pattern.resolve(request)
        }

    @Suppress("UNCHECKED_CAST")
    private fun DeepLinkPattern<out NavRouteDestination>.resolve(
        request: DeepLinkRequest,
    ): NavRouteDestination? {
        val pattern = this as DeepLinkPattern<NavRouteDestination>
        return DeepLinkMatcher(request, pattern)
            .match()
            ?.toRoute()
    }
}
