package dev.pirajok.navroute.sample.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.pirajok.navroute.annotations.DestinationStyle
import dev.pirajok.navroute.annotations.NavDeepLink
import dev.pirajok.navroute.annotations.NavEntry
import dev.pirajok.navroute.sample.api.MainScreen

@Composable
@NavEntry(
    MainScreen::class,
    style = DestinationStyle.Screen::class,
)
fun MainScreen() {

}