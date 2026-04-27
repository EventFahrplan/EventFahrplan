package nerd.tuxmobil.fahrplan.congress.designsystem.bars

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.PreviewLightDark
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ToolbarMetrics
import nerd.tuxmobil.fahrplan.congress.designsystem.bars.FloatingToolbarDefaults.FloatingToolbarColors
import nerd.tuxmobil.fahrplan.congress.designsystem.bars.FloatingToolbarDefaults.floatingToolbarColors
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.FloatingActionButtonIcon
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconActionable
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
fun GenericFloatingToolbar(
    visible: Boolean,
    useVerticalToolbar: Boolean,
    modifier: Modifier = Modifier,
    colors: FloatingToolbarColors = floatingToolbarColors(),
    floatingActionButton: (@Composable () -> Unit)? = null,
    toolbarContent: AppBarScope.() -> Unit,
) {
    val density = LocalDensity.current
    val contentPadding = ToolbarMetrics.toolbarContentPaddingValues(useVerticalToolbar)
    val toolbarModifier = modifier.wrapContentWidth()

    if (useVerticalToolbar) {
        VerticalFloatingToolbar(
            modifier = toolbarModifier
                .windowInsetsPadding(WindowInsets.navigationBars),
            colors = colors,
            floatingActionButton = floatingActionButton,
            contentPadding = contentPadding,
        ) {
            AppBarColumn {
                toolbarContent()
            }
        }
    } else {
        val horizontalToolbarTranslationY by animateFloatAsState(
            targetValue = if (visible) {
                0f
            } else {
                with(density) { (ToolbarMetrics.HORIZONTAL_BAR_POSITION).toPx() }
            },
            label = "horizontalToolbarTranslationY",
        )
        val horizontalToolbarAlpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            label = "horizontalToolbarAlpha",
        )
        HorizontalFloatingToolbar(
            modifier = toolbarModifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .graphicsLayer {
                    translationY = horizontalToolbarTranslationY
                    alpha = horizontalToolbarAlpha
                },
            colors = colors,
            floatingActionButton = floatingActionButton,
            contentPadding = contentPadding,
        ) {
            AppBarRow {
                toolbarContent()
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun GenericFloatingHorizontalToolbar() {
    GenericFloatingToolbar(false)
}

@PreviewLightDark
@Composable
private fun GenericFloatingVerticalToolbar() {
    GenericFloatingToolbar(true)
}

@Composable
private fun GenericFloatingToolbar(useVerticalToolbar: Boolean) {
    EventFahrplanTheme {
        GenericFloatingToolbar(
            visible = true,
            useVerticalToolbar = useVerticalToolbar,
            floatingActionButton = {
                FloatingActionButtonIcon(
                    icon = R.drawable.ic_star_outline,
                    contentDescription = 0,
                    onClick = {},
                )
            },
            toolbarContent = {
                clickableItem(
                    icon = {
                        IconActionable(
                            modifier = Modifier.size(EventFahrplanTheme.dimensions.iconSize),
                            icon = R.drawable.ic_bell_outline,
                            contentDescription = 0,
                        )
                    },
                    label = "",
                    onClick = {},
                )
                clickableItem(
                    icon = {
                        IconActionable(
                            modifier = Modifier.size(EventFahrplanTheme.dimensions.iconSize),
                            icon = R.drawable.ic_share,
                            contentDescription = 0,
                        )
                    },
                    label = "",
                    onClick = {},
                )
            },
        )
    }
}
