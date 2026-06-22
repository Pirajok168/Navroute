package dev.pirajok.navroute.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dev.pirajok.navroute.annotations.DestinationStyle
import androidx.navigation3.scene.DialogSceneStrategy

public open class DialogDestinationStyle : DestinationStyle.Dialog() {
    public open val properties: DialogProperties = DialogProperties()

    override fun metadata(): Map<String, Any> =
        DialogSceneStrategy.dialog(properties)
}

@OptIn(ExperimentalMaterial3Api::class)
public open class BottomSheetDestinationStyle : DestinationStyle.BottomSheet() {
    public open val sheetMaxWidth: Dp? = null
    public open val sheetGesturesEnabled: Boolean = true
    public open val shape: Shape? = null
    public open val containerColor: Color? = null
    public open val contentColor: Color? = null
    public open val tonalElevation: Dp = 0.dp
    public open val scrimColor: Color? = null
    public open val properties: ModalBottomSheetProperties = ModalBottomSheetProperties()
    public open val skipPartiallyExpanded: Boolean = false
    public open val keepWhenOverlaid: Boolean = false

    override fun metadata(): Map<String, Any> =
        BottomSheetSceneStrategy.bottomSheet(
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
        )
}

public open class ModalNavigationDrawerDestinationStyle : DestinationStyle.ModalNavigationDrawer() {
    public open val gesturesEnabled: Boolean = true
    public open val scrimColor: Color? = null
    public open val drawerShape: Shape? = null
    public open val drawerContainerColor: Color? = null
    public open val drawerContentColor: Color? = null
    public open val drawerTonalElevation: Dp? = null
    public open val windowInsets: WindowInsets? = null

    override fun metadata(): Map<String, Any> =
        ModalNavigationDrawerSceneStrategy.modalNavigationDrawer(
            gesturesEnabled = gesturesEnabled,
            scrimColor = scrimColor,
            drawerShape = drawerShape,
            drawerContainerColor = drawerContainerColor,
            drawerContentColor = drawerContentColor,
            drawerTonalElevation = drawerTonalElevation,
            windowInsets = windowInsets,
        )
}
