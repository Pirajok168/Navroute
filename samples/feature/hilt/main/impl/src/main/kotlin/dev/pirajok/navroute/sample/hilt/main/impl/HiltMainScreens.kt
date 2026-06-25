package dev.pirajok.navroute.sample.hilt.main.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dev.pirajok.navroute.annotations.DestinationStyle
import dev.pirajok.navroute.annotations.NavDeepLink
import dev.pirajok.navroute.annotations.NavEntry
import dev.pirajok.navroute.sample.hilt.detail.api.HiltDetailRoute
import dev.pirajok.navroute.sample.hilt.main.api.HiltMainDialogRoute
import dev.pirajok.navroute.sample.hilt.main.api.HiltMainModalRoute
import dev.pirajok.navroute.sample.hilt.main.api.HiltMainRoute
import dev.pirajok.navroute.sample.navigation.rememberSampleNavigator
import dev.pirajok.navroute.ui.DialogDestinationStyle
import dev.pirajok.navroute.ui.ModalNavigationDrawerDestinationStyle

private val sampleItems = listOf(
    "Lake route",
    "Mountain pass",
    "City loop",
    "Forest trail",
    "Night express",
)

@NavEntry(
    route = HiltMainRoute::class,
    style = DestinationStyle.Screen::class,
    deepLinks = [
        NavDeepLink(uriPattern = "navroute-hilt://main"),
    ],
)
@Composable
public fun HiltMainScreen() {
    val navigator = rememberSampleNavigator()

    Row(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "Hilt routes",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            items(sampleItems) { title ->
                val itemId = sampleItems.indexOf(title) + 1
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navigator.navigate(HiltDetailRoute(itemId = itemId, title = title))
                        },
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = title, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Open detail route #$itemId",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .width(112.dp)
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Button(onClick = { navigator.navigate(HiltMainModalRoute) }) {
                Text(text = "Modal")
            }
        }
    }
}

@NavEntry(
    route = HiltMainModalRoute::class,
    style = HiltMainModalStyle::class,
)
@Composable
public fun HiltMainModal() {
    val navigator = rememberSampleNavigator()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Side modal", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "This route is rendered through ModalNavigationDrawerSceneStrategy.",
            style = MaterialTheme.typography.bodyMedium,
        )
        HorizontalDivider()
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                navigator.navigate(
                    HiltMainDialogRoute(
                        title = "Hilt dialog",
                        body = "Opened from a modal drawer route.",
                    )
                )
            },
        ) {
            Text(text = "Open dialog")
        }
    }
}

@NavEntry(
    route = HiltMainDialogRoute::class,
    style = HiltMainDialogStyle::class,
)
@Composable
public fun HiltMainDialog(title: String, body: String) {
    val navigator = rememberSampleNavigator()

    AlertDialog(
        onDismissRequest = navigator::back,
        confirmButton = {
            TextButton(onClick = navigator::back) {
                Text(text = "Close")
            }
        },
        title = { Text(text = title) },
        text = { Text(text = body) },
    )
}

public object HiltMainModalStyle : ModalNavigationDrawerDestinationStyle()

public object HiltMainDialogStyle : DialogDestinationStyle() {
    override val properties: DialogProperties = DialogProperties()
}
