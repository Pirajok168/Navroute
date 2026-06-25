package dev.pirajok.navroute.sample.koin.main.api

import dev.pirajok.navroute.annotations.NavRoute
import dev.pirajok.navroute.runtime.NavRouteDestination
import kotlinx.serialization.Serializable

@NavRoute
@Serializable
public data object KoinMainRoute : NavRouteDestination

@NavRoute
@Serializable
public data object KoinMainModalRoute : NavRouteDestination

@NavRoute
@Serializable
public data class KoinMainDialogRoute(
    val title: String,
    val body: String,
) : NavRouteDestination
