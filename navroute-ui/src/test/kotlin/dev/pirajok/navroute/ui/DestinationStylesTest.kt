package dev.pirajok.navroute.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.get
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DestinationStylesTest {

    @Test
    fun dialogStyleProvidesDialogMetadata() {
        val metadata = TestDialogStyle.metadata()

        assertFalse(metadata.isEmpty())
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun bottomSheetStyleProvidesBottomSheetMetadata() {
        val metadata = TestBottomSheetStyle.metadata()

        assertFalse(metadata.isEmpty())
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun bottomSheetStyleProvidesConfiguredBottomSheetMetadata() {
        val metadata = ConfiguredBottomSheetStyle.metadata()
        val config = metadata[BottomSheetSceneStrategy.Companion.BottomSheetKey]

        assertEquals(Color.Red, config?.containerColor)
        assertEquals(Color.White, config?.contentColor)
        assertEquals(Color.Black, config?.scrimColor)
        assertEquals(12.dp, config?.tonalElevation)
        assertEquals(false, config?.sheetGesturesEnabled)
        assertEquals(true, config?.skipPartiallyExpanded)
        assertEquals(true, config?.keepWhenOverlaid)
    }

    @Test
    fun modalDrawerStyleProvidesModalDrawerMetadata() {
        val metadata = TestModalDrawerStyle.metadata()

        assertFalse(metadata.isEmpty())
    }

    @Test
    fun modalDrawerStyleProvidesConfiguredModalDrawerMetadata() {
        val metadata = ConfiguredModalDrawerStyle.metadata()
        val config = metadata[ModalNavigationDrawerSceneStrategy.Companion.ModalNavigationDrawerKey]

        assertEquals(Color.Green, config?.drawerContainerColor)
        assertEquals(Color.Blue, config?.drawerContentColor)
        assertEquals(Color.Yellow, config?.scrimColor)
        assertEquals(16.dp, config?.drawerTonalElevation)
        assertEquals(false, config?.gesturesEnabled)
    }

    private object TestDialogStyle : DialogDestinationStyle()

    private object TestBottomSheetStyle : BottomSheetDestinationStyle()

    private object ConfiguredBottomSheetStyle : BottomSheetDestinationStyle() {
        override val containerColor: Color = Color.Red
        override val contentColor: Color = Color.White
        override val scrimColor: Color = Color.Black
        override val tonalElevation = 12.dp
        override val sheetGesturesEnabled: Boolean = false
        override val skipPartiallyExpanded: Boolean = true
        override val keepWhenOverlaid: Boolean = true
    }

    private object TestModalDrawerStyle : ModalNavigationDrawerDestinationStyle()

    private object ConfiguredModalDrawerStyle : ModalNavigationDrawerDestinationStyle() {
        override val drawerContainerColor: Color = Color.Green
        override val drawerContentColor: Color = Color.Blue
        override val scrimColor: Color = Color.Yellow
        override val drawerTonalElevation = 16.dp
        override val gesturesEnabled: Boolean = false
    }
}
