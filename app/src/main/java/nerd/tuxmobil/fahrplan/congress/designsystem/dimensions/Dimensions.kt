package nerd.tuxmobil.fahrplan.congress.designsystem.dimensions

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

@Immutable
data class Dimensions(
    val iconSize: Dp,
)

internal val LocalDimensions = staticCompositionLocalOf<Dimensions> {
    error("No Dimensions provided")
}
