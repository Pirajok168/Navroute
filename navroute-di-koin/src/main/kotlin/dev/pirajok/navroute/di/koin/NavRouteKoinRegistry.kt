package dev.pirajok.navroute.di.koin

import dev.pirajok.navroute.deeplink.DeepLinkPatternProvider
import dev.pirajok.navroute.runtime.EntryBuilder
import org.koin.core.Koin

public class NavRouteKoinRegistry(
    public val entryBuilders: List<EntryBuilder>,
    public val deepLinkPatternProviders: List<DeepLinkPatternProvider>,
)

public fun Koin.getNavRouteKoinRegistry(): NavRouteKoinRegistry =
    NavRouteKoinRegistry(
        entryBuilders = getAll(),
        deepLinkPatternProviders = getAll(),
    )