package dev.pirajok.navroute.sample.impl

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import dev.pirajok.navroute.annotations.NavDeepLink
import dev.pirajok.navroute.annotations.NavEntry
import dev.pirajok.navroute.sample.api.SampleRoute
import dev.pirajok.navroute.ui.DialogDestinationStyle

@NavEntry(
    route = SampleRoute::class,
    style = SampleDialogStyle::class,
    deepLinks = [
        NavDeepLink(uriPattern = "navroute://sample/{title}"),
    ],
)
@Composable
public fun SampleScreen(title: String?) {
    Text(text = title ?: "Sample")
}

public object SampleDialogStyle : DialogDestinationStyle() {
    override val properties: DialogProperties
        get() = DialogProperties()
}
