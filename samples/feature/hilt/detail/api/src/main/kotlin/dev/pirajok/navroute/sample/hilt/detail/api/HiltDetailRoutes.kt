package dev.pirajok.navroute.sample.hilt.detail.api

import dev.pirajok.navroute.annotations.NavRoute
import dev.pirajok.navroute.runtime.NavRouteDestination
import kotlinx.serialization.Serializable

@NavRoute
@Serializable
public data class HiltDetailRoute(
    val itemId: Int,
    val title: String,
) : NavRouteDestination

@NavRoute
@Serializable
public data class HiltDetailSheetRoute(
    val itemId: Int,
    val title: String,
) : NavRouteDestination

@NavRoute
@Serializable
public data class HiltNestedDetailSheetRoute(
    val itemId: Int,
) : NavRouteDestination
