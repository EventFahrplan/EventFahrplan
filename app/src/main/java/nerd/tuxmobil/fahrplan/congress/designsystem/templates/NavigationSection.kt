package nerd.tuxmobil.fahrplan.congress.designsystem.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Horizontal
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Top
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import nerd.tuxmobil.fahrplan.congress.commons.ScreenMetrics
import nerd.tuxmobil.fahrplan.congress.commons.ToolbarMetrics
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.ButtonNavigation
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
fun NavigationSection(
    showInSidePane: Boolean,
    modifier: Modifier = Modifier,
    isInDetailsScreen: Boolean = false,
    isAboveHeaderDayDate: Boolean = true,
    onNavClick: () -> Unit,
) {
    NavigationSectionWithContent(
        showInSidePane = showInSidePane,
        modifier = modifier,
        isInDetailsScreen = isInDetailsScreen,
        isAboveHeaderDayDate = isAboveHeaderDayDate,
        contentLeftOfCloseButton = {},
        contentRightOfBackButton = {},
        onNavClick = onNavClick,
    )
}

@Composable
fun NavigationSectionWithContent(
    showInSidePane: Boolean,
    modifier: Modifier = Modifier,
    isInDetailsScreen: Boolean = false,
    isAboveHeaderDayDate: Boolean = true,
    contentLeftOfCloseButton: @Composable BoxWithConstraintsScope.() -> Unit,
    contentRightOfBackButton: @Composable () -> Unit,
    onNavClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                when {
                    showInSidePane -> Modifier
                    else -> Modifier.windowInsetsPadding(WindowInsets.statusBars.only(Top))
                },
            )
            .windowInsetsPadding(WindowInsets.safeContent.only(Horizontal))
            .padding(
                ScreenMetrics.navigationSectionPaddingValues(
                    showInSidePane = showInSidePane,
                    isInDetailsScreen = isInDetailsScreen,
                    isAboveHeaderDayDate = isAboveHeaderDayDate,
                )
            ),
        verticalAlignment = CenterVertically,
    ) {
        if (showInSidePane) {
            BoxWithConstraints(Modifier
                .weight(1f)
                .padding(end = ScreenMetrics.END_PADDING) // to clip long text before the close button, vertical aligned with screen content
            ) {
                contentLeftOfCloseButton()
            }
            Row(
                modifier = Modifier.width(ToolbarMetrics.BUTTON_NAVIGATION_WIDTH), // to center above toolbar
                horizontalArrangement = Arrangement.Center,
            ) {
                ButtonNavigation(useCloseIcon = true, onNavClick)
            }
        } else {
            ButtonNavigation(useCloseIcon = false, onNavClick)
            contentRightOfBackButton()
        }
    }
}

@PreviewLightDark
@Composable
private fun NavigationSectionPreview() {
    NavigationSection(showInSidePane = false)
}

@PreviewLightDark
@Composable
private fun NavigationSectionSidePanePreview() {
    NavigationSection(showInSidePane = true)
}

@Composable
private fun NavigationSection(showInSidePane: Boolean) {
    EventFahrplanTheme {
        NavigationSectionWithContent(
            showInSidePane = showInSidePane,
            contentLeftOfCloseButton = {
                Text("Left of close button")
            },
            contentRightOfBackButton = {
                Text("Right of back button")
            },
            onNavClick = {},
        )
    }
}
