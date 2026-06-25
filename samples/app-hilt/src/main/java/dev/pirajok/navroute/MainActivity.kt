package dev.pirajok.navroute

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import dev.pirajok.navroute.deeplink.DeepLinkPatternProvider
import dev.pirajok.navroute.deeplink.DeepLinkResolver
import dev.pirajok.navroute.runtime.EntryBuilder
import dev.pirajok.navroute.runtime.NavRouteDestination
import dev.pirajok.navroute.sample.hilt.main.api.HiltMainRoute
import dev.pirajok.navroute.sample.navigation.LocalSampleNavBackStack
import dev.pirajok.navroute.ui.BottomSheetSceneStrategy
import dev.pirajok.navroute.ui.ModalNavigationDrawerSceneStrategy
import dev.pirajok.navroute.ui.theme.NavrouteTheme
import javax.inject.Inject

@AndroidEntryPoint
public class MainActivity : ComponentActivity() {

    @Inject
    public lateinit var entryBuilders: Set<@JvmSuppressWildcards EntryBuilder>

    @Inject
    public lateinit var deepLinkProviders: Set<@JvmSuppressWildcards DeepLinkPatternProvider>

    private val pendingDeepLinkRoute: MutableState<NavRouteDestination?> = mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val deepLinkResolver = DeepLinkResolver(deepLinkProviders)
        val startRoute = deepLinkResolver.resolve(intent) ?: HiltMainRoute

        setContent {
            NavrouteTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavRouteSampleHost(
                        startRoute = startRoute,
                        pendingDeepLinkRoute = pendingDeepLinkRoute,
                        entryBuilders = entryBuilders,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeepLinkRoute.value = DeepLinkResolver(deepLinkProviders).resolve(intent)
    }
}

@Composable
private fun NavRouteSampleHost(
    startRoute: NavRouteDestination,
    pendingDeepLinkRoute: MutableState<NavRouteDestination?>,
    entryBuilders: Set<EntryBuilder>,
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

    CompositionLocalProvider(LocalSampleNavBackStack provides backStack) {
        NavDisplay(
            backStack = backStack,
            sceneStrategies = sceneStrategies,
            entryProvider = entryProvider {
                entryBuilders.forEach { entryBuilder ->
                    entryBuilder()
                }
            },
        )
    }
}
