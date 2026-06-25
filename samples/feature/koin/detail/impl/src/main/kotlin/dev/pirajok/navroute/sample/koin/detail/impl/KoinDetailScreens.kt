package dev.pirajok.navroute.sample.koin.detail.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.pirajok.navroute.annotations.DestinationStyle
import dev.pirajok.navroute.annotations.NavEntry
import dev.pirajok.navroute.sample.koin.detail.api.KoinDetailRoute
import dev.pirajok.navroute.sample.koin.detail.api.KoinDetailSheetRoute
import dev.pirajok.navroute.sample.koin.detail.api.KoinNestedDetailSheetRoute
import dev.pirajok.navroute.sample.navigation.rememberSampleNavigator
import dev.pirajok.navroute.ui.BottomSheetDestinationStyle

@NavEntry(
    route = KoinDetailRoute::class,
    style = DestinationStyle.Screen::class,
)
@Composable
public fun KoinDetailScreen(itemId: Int, title: String) {
    val navigator = rememberSampleNavigator()

    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Detail route for item #$itemId. From here the sample opens a bottom sheet route.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = navigator::back) {
                Text(text = "Back")
            }
            Button(
                onClick = {
                    navigator.navigate(
                        KoinDetailSheetRoute(
                            itemId = itemId,
                            title = title,
                        )
                    )
                },
            ) {
                Text(text = "Open sheet")
            }
        }
    }
}

@NavEntry(
    route = KoinDetailSheetRoute::class,
    style = KoinDetailSheetStyle::class,
)
@Composable
public fun KoinDetailSheet(itemId: Int, title: String) {
    val navigator = rememberSampleNavigator()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "Sheet for $title", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "The sheet route keeps itself visible when another bottom sheet is pushed.",
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navigator.navigate(KoinNestedDetailSheetRoute(itemId = itemId)) },
        ) {
            Text(text = "Open nested sheet")
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@NavEntry(
    route = KoinNestedDetailSheetRoute::class,
    style = KoinNestedDetailSheetStyle::class,
)
@Composable
public fun KoinNestedDetailSheet(itemId: Int) {
    val navigator = rememberSampleNavigator()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "Nested sheet", style = MaterialTheme.typography.titleLarge)
            Text(text = "Opened from sheet for item #$itemId.")
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = navigator::back,
            ) {
                Text(text = "Close nested sheet")
            }
        }
    }
}

public object KoinDetailSheetStyle : BottomSheetDestinationStyle() {
    override val keepWhenOverlaid: Boolean = true
}

public object KoinNestedDetailSheetStyle : BottomSheetDestinationStyle()
