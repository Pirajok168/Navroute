package dev.pirajok.navroute.sample.koin.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.pirajok.navroute.annotations.NavEntry
import dev.pirajok.navroute.sample.koin.api.KoinMainScreen

@NavEntry(route = KoinMainScreen::class)
@Composable
public fun KoinMainScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "Koin sample")
    }
}
