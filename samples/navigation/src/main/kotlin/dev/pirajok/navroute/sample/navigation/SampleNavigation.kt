package dev.pirajok.navroute.sample.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.pirajok.navroute.runtime.NavRouteDestination

public val LocalSampleNavBackStack: androidx.compose.runtime.ProvidableCompositionLocal<NavBackStack<NavKey>> =
    staticCompositionLocalOf {
        error("Sample navigation back stack is not provided.")
    }

@Composable
public fun rememberSampleNavigator(): SampleNavigator =
    SampleNavigator(LocalSampleNavBackStack.current)

public class SampleNavigator internal constructor(
    private val backStack: NavBackStack<NavKey>,
) {
    public fun navigate(route: NavRouteDestination) {
        backStack.add(route)
    }

    public fun back() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }
}
