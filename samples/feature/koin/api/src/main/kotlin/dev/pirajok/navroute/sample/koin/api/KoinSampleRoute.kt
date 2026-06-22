package dev.pirajok.navroute.sample.koin.api

import dev.pirajok.navroute.annotations.NavRoute
import dev.pirajok.navroute.runtime.NavRouteDestination
import kotlinx.serialization.Serializable

@NavRoute
@Serializable
public data class KoinSampleRoute(
    val title: String? = null,
) : NavRouteDestination

@NavRoute
@Serializable
public data object KoinMainScreen : NavRouteDestination
