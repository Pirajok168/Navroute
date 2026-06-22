package dev.pirajok.navroute.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import kotlinx.coroutines.launch

public data class ModalNavigationDrawerSceneConfig(
    val gesturesEnabled: Boolean = true,
    val scrimColor: Color? = null,
    val drawerShape: Shape? = null,
    val drawerContainerColor: Color? = null,
    val drawerContentColor: Color? = null,
    val drawerTonalElevation: Dp? = null,
    val windowInsets: WindowInsets? = null,
)

private data class ModalNavigationDrawerScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val config: ModalNavigationDrawerSceneConfig,
    private val onBack: () -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    override val content: @Composable () -> Unit = {
        var dismissHandled by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(
            initialValue = DrawerValue.Closed,
            confirmStateChange = { value ->
                if (value == DrawerValue.Closed && !dismissHandled) {
                    dismissHandled = true
                    onBack()
                }
                true
            },
        )

        LaunchedEffect(Unit) {
            drawerState.open()
        }

        BackHandler(drawerState.isOpen) {
            scope.launch { drawerState.close() }
        }

        val drawerContainerColor = config.drawerContainerColor ?: DrawerDefaults.modalContainerColor

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = config.gesturesEnabled,
            scrimColor = config.scrimColor ?: DrawerDefaults.scrimColor,
            drawerContent = {
                ModalDrawerSheet(
                    drawerShape = config.drawerShape ?: DrawerDefaults.shape,
                    drawerContainerColor = drawerContainerColor,
                    drawerContentColor = config.drawerContentColor ?: contentColorFor(drawerContainerColor),
                    drawerTonalElevation = config.drawerTonalElevation ?: DrawerDefaults.ModalDrawerElevation,
                    windowInsets = config.windowInsets ?: DrawerDefaults.windowInsets,
                ) {
                    entry.Content()
                }
            },
        ) {
            Box(modifier = Modifier.fillMaxSize())
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ModalNavigationDrawerScene<*>) return false

        return key == other.key &&
                previousEntries == other.previousEntries &&
                overlaidEntries == other.overlaidEntries &&
                entry.contentKey == other.entry.contentKey &&
                config == other.config
    }

    override fun hashCode(): Int {
        return key.hashCode() * 31 +
                previousEntries.hashCode() * 31 +
                overlaidEntries.hashCode() * 31 +
                entry.contentKey.hashCode() * 31 +
                config.hashCode()
    }

    override fun toString(): String {
        return "ModalNavigationDrawerScene(key=$key, " +
                "entry=${entry.contentKey}, " +
                "previousEntries=${previousEntries}, " +
                "overlaidEntries=${overlaidEntries}, " +
                "config=$config)"
    }
}

public class ModalNavigationDrawerSceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.lastOrNull() ?: return null
        val config = lastEntry.metadata[ModalNavigationDrawerKey] ?: return null
        val baseEntries = entries.dropLast(trailingDrawerCount(entries))
        return ModalNavigationDrawerScene(
            key = lastEntry.contentKey,
            previousEntries = baseEntries,
            overlaidEntries = baseEntries,
            entry = lastEntry,
            config = config,
            onBack = onBack,
        )
    }

    private fun trailingDrawerCount(entries: List<NavEntry<T>>): Int {
        var count = 0
        for (index in entries.lastIndex downTo 0) {
            if (entries[index].metadata[ModalNavigationDrawerKey] == null) break
            count++
        }
        return count
    }

    public companion object {
        public object ModalNavigationDrawerKey : NavMetadataKey<ModalNavigationDrawerSceneConfig>



        public fun modalNavigationDrawer(
            gesturesEnabled: Boolean = true,
            scrimColor: Color? = null,
            drawerShape: Shape? = null,
            drawerContainerColor: Color? = null,
            drawerContentColor: Color? = null,
            drawerTonalElevation: Dp? = null,
            windowInsets: WindowInsets? = null,
        ): Map<String, Any> = metadata {
            put(
                ModalNavigationDrawerKey,
                ModalNavigationDrawerSceneConfig(
                    gesturesEnabled = gesturesEnabled,
                    scrimColor = scrimColor,
                    drawerShape = drawerShape,
                    drawerContainerColor = drawerContainerColor,
                    drawerContentColor = drawerContentColor,
                    drawerTonalElevation = drawerTonalElevation,
                    windowInsets = windowInsets,
                ),
            )
        }
    }
}
