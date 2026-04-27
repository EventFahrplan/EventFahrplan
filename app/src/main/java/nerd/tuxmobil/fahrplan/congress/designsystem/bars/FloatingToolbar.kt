@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package nerd.tuxmobil.fahrplan.congress.designsystem.bars

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults.ContainerCollapsedElevation
import androidx.compose.material3.FloatingToolbarDefaults.ContainerCollapsedElevationWithFab
import androidx.compose.material3.FloatingToolbarDefaults.ContainerShape
import androidx.compose.material3.FloatingToolbarHorizontalFabPosition.Companion.Start
import androidx.compose.material3.FloatingToolbarVerticalFabPosition.Companion.Top
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Unspecified
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.commons.ToolbarMetrics
import nerd.tuxmobil.fahrplan.congress.designsystem.bars.FloatingToolbarDefaults.FloatingToolbarColors
import nerd.tuxmobil.fahrplan.congress.designsystem.bars.FloatingToolbarDefaults.floatingToolbarColors
import androidx.compose.material3.FloatingToolbarDefaults as Material3FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar as Material3HorizontalFloatingToolbar
import androidx.compose.material3.VerticalFloatingToolbar as Material3VerticalFloatingToolbar

object FloatingToolbarDefaults {

    @Immutable
    data class FloatingToolbarColors(
        val toolbarContainerColor: Color,
        val toolbarContentColor: Color,
        val fabContainerColor: Color,
        val fabContentColor: Color,
    )

    @Composable
    fun floatingToolbarColors(
        toolbarContainerColor: Color = Unspecified,
        toolbarContentColor: Color = Unspecified,
        fabContainerColor: Color = Unspecified,
        fabContentColor: Color = Unspecified,
    ) = FloatingToolbarColors(
        toolbarContainerColor = toolbarContainerColor,
        toolbarContentColor = toolbarContentColor,
        fabContainerColor = fabContainerColor,
        fabContentColor = fabContentColor,
    )

    val shape: Shape
        @Composable get() = ContainerShape

}

@Composable
fun HorizontalFloatingToolbar(
    modifier: Modifier = Modifier,
    floatingActionButton: (@Composable () -> Unit)? = null,
    colors: FloatingToolbarColors = floatingToolbarColors(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    shape: Shape = FloatingToolbarDefaults.shape,
    content: @Composable RowScope.() -> Unit,
) {
    if (floatingActionButton == null) {
        Material3HorizontalFloatingToolbar(
            expanded = true,
            modifier = modifier.then(Modifier.padding(vertical = 8.dp)), // to match FAB version
            colors = Material3FloatingToolbarDefaults.standardFloatingToolbarColors(
                toolbarContainerColor = colors.toolbarContainerColor,
                toolbarContentColor = colors.toolbarContentColor,
                fabContainerColor = colors.fabContainerColor,
                fabContentColor = colors.fabContentColor,
            ),
            contentPadding = contentPadding,
            shape = shape,
            expandedShadowElevation = ToolbarMetrics.ELEVATION_LEVEL_2,
            collapsedShadowElevation = ContainerCollapsedElevation,
            content = content,
        )
    } else {
        Material3HorizontalFloatingToolbar(
            expanded = true,
            modifier = modifier,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = Start,
            colors = Material3FloatingToolbarDefaults.standardFloatingToolbarColors(
                toolbarContainerColor = colors.toolbarContainerColor,
                toolbarContentColor = colors.toolbarContentColor,
                fabContainerColor = colors.fabContainerColor,
                fabContentColor = colors.fabContentColor,
            ),
            contentPadding = contentPadding,
            shape = shape,
            expandedShadowElevation = ToolbarMetrics.ELEVATION_LEVEL_2,
            collapsedShadowElevation = ContainerCollapsedElevationWithFab,
            content = content,
        )
    }
}

@Composable
fun VerticalFloatingToolbar(
    modifier: Modifier = Modifier,
    floatingActionButton: (@Composable () -> Unit)? = null,
    colors: FloatingToolbarColors = floatingToolbarColors(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    shape: Shape = FloatingToolbarDefaults.shape,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (floatingActionButton == null) {
        Material3VerticalFloatingToolbar(
            expanded = true,
            modifier = modifier.then(Modifier.padding(horizontal = 8.dp)), // to match FAB version
            colors = Material3FloatingToolbarDefaults.standardFloatingToolbarColors(
                toolbarContainerColor = colors.toolbarContainerColor,
                toolbarContentColor = colors.toolbarContentColor,
                fabContainerColor = colors.fabContainerColor,
                fabContentColor = colors.fabContentColor,
            ),
            contentPadding = contentPadding,
            shape = shape,
            expandedShadowElevation = ToolbarMetrics.ELEVATION_LEVEL_2,
            collapsedShadowElevation = ContainerCollapsedElevation,
            content = content,
        )
    } else {
        Material3VerticalFloatingToolbar(
            expanded = true,
            modifier = modifier,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = Top,
            colors = Material3FloatingToolbarDefaults.standardFloatingToolbarColors(
                toolbarContainerColor = colors.toolbarContainerColor,
                toolbarContentColor = colors.toolbarContentColor,
                fabContainerColor = colors.fabContainerColor,
                fabContentColor = colors.fabContentColor,
            ),
            contentPadding = contentPadding,
            shape = shape,
            expandedShadowElevation = ToolbarMetrics.ELEVATION_LEVEL_2,
            collapsedShadowElevation = ContainerCollapsedElevationWithFab,
            content = content,
        )
    }
}
