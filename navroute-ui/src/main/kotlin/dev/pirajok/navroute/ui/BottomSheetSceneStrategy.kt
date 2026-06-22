package dev.pirajok.navroute.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

@OptIn(ExperimentalMaterial3Api::class)
public object NavRouteBottomSheetDefaults {
    public val DragHandle: @Composable () -> Unit = {
        BottomSheetDefaults.DragHandle()
    }

    public val ContentWindowInsets: @Composable () -> WindowInsets = {
        BottomSheetDefaults.windowInsets
    }
}

@OptIn(ExperimentalMaterial3Api::class)
public data class BottomSheetSceneConfig(
    val sheetMaxWidth: Dp? = null,
    val sheetGesturesEnabled: Boolean = true,
    val shape: Shape? = null,
    val containerColor: Color? = null,
    val contentColor: Color? = null,
    val tonalElevation: Dp = 0.dp,
    val scrimColor: Color? = null,
    val properties: ModalBottomSheetProperties = ModalBottomSheetProperties(),
    val skipPartiallyExpanded: Boolean = false,
    val keepWhenOverlaid: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
private data class BottomSheetScene<T : Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val config: BottomSheetSceneConfig,
    private val onBack: () -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    override val content: @Composable () -> Unit = {
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = config.skipPartiallyExpanded,
        )
        val containerColor = config.containerColor ?: BottomSheetDefaults.ContainerColor

        ModalBottomSheet(
            onDismissRequest = onBack,
            modifier = Modifier.statusBarsPadding(),
            sheetState = sheetState,
            sheetMaxWidth = config.sheetMaxWidth ?: BottomSheetDefaults.SheetMaxWidth,
            sheetGesturesEnabled = config.sheetGesturesEnabled,
            shape = config.shape ?: BottomSheetDefaults.ExpandedShape,
            containerColor = containerColor,
            contentColor = config.contentColor ?: contentColorFor(containerColor),
            tonalElevation = config.tonalElevation,
            scrimColor = config.scrimColor ?: BottomSheetDefaults.ScrimColor,
            dragHandle = null,
            properties = config.properties,
        ) {
            entry.Content()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BottomSheetScene<*>) return false

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
        return "BottomSheetScene(key=$key, entry=${entry.contentKey}, " +
                "previousEntries=${previousEntries}, " +
                "overlaidEntries=${overlaidEntries})"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
public class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.lastOrNull() ?: return null
        val config = lastEntry.metadata[BottomSheetKey] ?: return null
        val baseEntries = entries.dropLast(trailingBottomSheetCount(entries) + 1)
        return BottomSheetScene(
            key = lastEntry.contentKey,
            previousEntries = baseEntries,
            overlaidEntries = baseEntries,
            entry = lastEntry,
            config = config,
            onBack = onBack,
        )
    }

    private fun trailingBottomSheetCount(entries: List<NavEntry<T>>): Int {
        val currentEntry = entries.lastOrNull()
        val currentKeepWhenOverlaid = currentEntry?.metadata?.get(BottomSheetKey)?.keepWhenOverlaid ?: false

        if (currentKeepWhenOverlaid) {
            return 0
        }

        var count = 0
        for (index in entries.lastIndex - 1 downTo 0) {
            val entry = entries[index]
            val isBottomSheet = entry.metadata[BottomSheetKey] != null
            if (!isBottomSheet) break
            count++
        }
        return count
    }

    public companion object {
        public object BottomSheetKey : NavMetadataKey<BottomSheetSceneConfig>

        public fun bottomSheet(
            config: BottomSheetSceneConfig,
        ): Map<String, Any> = metadata {
            put(BottomSheetKey, config)
        }

        public fun bottomSheet(
            sheetMaxWidth: Dp? = null,
            sheetGesturesEnabled: Boolean = true,
            shape: Shape? = null,
            containerColor: Color? = null,
            contentColor: Color? = null,
            tonalElevation: Dp = 0.dp,
            scrimColor: Color? = null,
            properties: ModalBottomSheetProperties = ModalBottomSheetProperties(),
            skipPartiallyExpanded: Boolean = false,
            keepWhenOverlaid: Boolean = false,
        ): Map<String, Any> = metadata {
            put(
                BottomSheetKey,
                BottomSheetSceneConfig(
                    sheetMaxWidth = sheetMaxWidth,
                    sheetGesturesEnabled = sheetGesturesEnabled,
                    shape = shape,
                    containerColor = containerColor,
                    contentColor = contentColor,
                    tonalElevation = tonalElevation,
                    scrimColor = scrimColor,
                    properties = properties,
                    skipPartiallyExpanded = skipPartiallyExpanded,
                    keepWhenOverlaid = keepWhenOverlaid,
                ),
            )
        }
    }
}
