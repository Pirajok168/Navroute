package dev.pirajok.navroute.sample.koin.impl

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import dev.pirajok.navroute.annotations.NavDeepLink
import dev.pirajok.navroute.annotations.NavEntry
import dev.pirajok.navroute.sample.koin.api.KoinSampleRoute
import dev.pirajok.navroute.ui.DialogDestinationStyle

@NavEntry(
    route = KoinSampleRoute::class,
    style = KoinSampleDialogStyle::class,
    deepLinks = [
        NavDeepLink(uriPattern = "navroute-koin://sample/{title}"),
    ],
)
@Composable
public fun KoinSampleScreen(title: String?) {
    Text(text = title ?: "Koin sample")
}

public object KoinSampleDialogStyle : DialogDestinationStyle() {
    override val properties: DialogProperties
        get() = DialogProperties()
}
