package nerd.tuxmobil.fahrplan.congress.designsystem.buttons

import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonDefaults.elevation
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import nerd.tuxmobil.fahrplan.congress.commons.ToolbarMetrics
import androidx.compose.material3.FloatingActionButton as Material3FloatingActionButton

@Composable
fun FloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    elevation: FloatingActionButtonElevation = elevation(
        defaultElevation = ToolbarMetrics.ELEVATION_LEVEL_2,
        pressedElevation = ToolbarMetrics.ELEVATION_LEVEL_2,
        hoveredElevation = ToolbarMetrics.ELEVATION_LEVEL_3,
        focusedElevation = ToolbarMetrics.ELEVATION_LEVEL_2,
    ),
    content: @Composable () -> Unit,
) {
    Material3FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = elevation,
        content = content,
    )
}
