package dev.pirajok.navroute.sample.hilt.main.api

import dev.pirajok.navroute.annotations.NavRoute
import dev.pirajok.navroute.runtime.NavRouteDestination
import kotlinx.serialization.Serializable

@NavRoute
@Serializable
public data object HiltMainRoute : NavRouteDestination

@NavRoute
@Serializable
public data object HiltMainModalRoute : NavRouteDestination

@NavRoute
@Serializable
public data class HiltMainDialogRoute(
    val title: String,
    val body: String,
) : NavRouteDestination
