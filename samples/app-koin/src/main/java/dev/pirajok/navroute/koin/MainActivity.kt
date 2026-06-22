package dev.pirajok.navroute.koin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import dev.pirajok.navroute.deeplink.DeepLinkResolver
import dev.pirajok.navroute.di.koin.NavRouteKoinRegistry
import dev.pirajok.navroute.di.koin.getNavRouteKoinRegistry
import dev.pirajok.navroute.runtime.NavRouteDestination
import dev.pirajok.navroute.sample.koin.api.KoinMainScreen
import dev.pirajok.navroute.ui.BottomSheetSceneStrategy
import dev.pirajok.navroute.ui.ModalNavigationDrawerSceneStrategy
import org.koin.android.ext.android.getKoin
import org.koin.androidx.compose.navigation3.getEntryProvider
import org.koin.compose.navigation3.EntryProvider
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
public class MainActivity : ComponentActivity() {

    private lateinit var navRouteRegistry: NavRouteKoinRegistry
    private val pendingDeepLinkRoute: MutableState<NavRouteDestination?> = mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        navRouteRegistry = getKoin().getNavRouteKoinRegistry()
        val deepLinkResolver = DeepLinkResolver(navRouteRegistry.deepLinkPatternProviders)
        val startRoute = deepLinkResolver.resolve(intent) ?: KoinMainScreen
        val entryProvider = getEntryProvider<NavKey>()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavRouteKoinSampleHost(
                        startRoute = startRoute,
                        pendingDeepLinkRoute = pendingDeepLinkRoute,
                        entryProvider = entryProvider,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeepLinkRoute.value = DeepLinkResolver(navRouteRegistry.deepLinkPatternProviders).resolve(intent)
    }
}

@Composable
@OptIn(KoinExperimentalAPI::class)
private fun NavRouteKoinSampleHost(
    startRoute: NavRouteDestination,
    pendingDeepLinkRoute: MutableState<NavRouteDestination?>,
    entryProvider: EntryProvider<NavKey>,
) {
    val backStack = rememberNavBackStack(startRoute)

    LaunchedEffect(pendingDeepLinkRoute.value) {
        val route = pendingDeepLinkRoute.value ?: return@LaunchedEffect
        backStack.add(route)
        pendingDeepLinkRoute.value = null
    }

    val sceneStrategies = remember {
        listOf(
            BottomSheetSceneStrategy<NavKey>(),
            ModalNavigationDrawerSceneStrategy<NavKey>(),
            DialogSceneStrategy<NavKey>(),
        )
    }

    NavDisplay(
        backStack = backStack,
        sceneStrategies = sceneStrategies,
        entryProvider = entryProvider,
    )
}
