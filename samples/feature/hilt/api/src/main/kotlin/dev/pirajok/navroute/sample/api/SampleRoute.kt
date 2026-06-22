package dev.pirajok.navroute.sample.api

import dev.pirajok.navroute.annotations.NavRoute
import dev.pirajok.navroute.runtime.NavRouteDestination
import kotlinx.serialization.Serializable

@NavRoute
@Serializable
public data class SampleRoute(
    val title: String? = null,
) : NavRouteDestination



@NavRoute
@Serializable
public data object MainScreen : NavRouteDestination



@NavRoute
@Serializable
data object DetailBottomSheetRoute: NavRouteDestination
