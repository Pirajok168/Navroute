package dev.pirajok.navroute.sample.koin.detail.api

import dev.pirajok.navroute.annotations.NavRoute
import dev.pirajok.navroute.runtime.NavRouteDestination
import kotlinx.serialization.Serializable

@NavRoute
@Serializable
public data class KoinDetailRoute(
    val itemId: Int,
    val title: String,
) : NavRouteDestination

@NavRoute
@Serializable
public data class KoinDetailSheetRoute(
    val itemId: Int,
    val title: String,
) : NavRouteDestination

@NavRoute
@Serializable
public data class KoinNestedDetailSheetRoute(
    val itemId: Int,
) : NavRouteDestination
