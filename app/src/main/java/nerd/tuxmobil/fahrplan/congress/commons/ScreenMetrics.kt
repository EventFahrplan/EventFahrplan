package nerd.tuxmobil.fahrplan.congress.commons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

object ScreenMetrics {

    private val START_PADDING = 22.dp
    val END_PADDING = 22.dp

    fun navigationSectionPaddingValues(
        showInSidePane: Boolean,
        isInDetailsScreen: Boolean,
        isAboveHeaderDayDate: Boolean,
    ): PaddingValues {
        val negativeIndent = if (isInDetailsScreen) 4.dp else 0.dp
        return PaddingValues(
            start = if (showInSidePane) START_PADDING - negativeIndent else 11.dp, // to indent ButtonNavigation
            end = if (showInSidePane) 8.dp else END_PADDING,
            top = 0.dp,
            bottom = if (isAboveHeaderDayDate) 0.dp else 16.dp
        )
    }

    fun screenContentPaddingValues() = PaddingValues(
        start = START_PADDING,
        end = END_PADDING,
        top = 0.dp,
        bottom = 0.dp
    )

}

object ToolbarMetrics {

    val ELEVATION_LEVEL_2 = 3.dp
    val ELEVATION_LEVEL_3 = 6.dp

    private val VERTICAL_BAR_WIDTH = 56.dp
    private val HORIZONTAL_BAR_HEIGHT = 48.dp
    private val BAR_PADDING = 16.dp
    private val CONTENT_PADDING = 8.dp

    val BUTTON_NAVIGATION_WIDTH = VERTICAL_BAR_WIDTH + 8.dp // to match vertical toolbar width
    val HORIZONTAL_BAR_POSITION = HORIZONTAL_BAR_HEIGHT + BAR_PADDING

    fun toolbarContentPaddingValues(useVerticalToolbar: Boolean) = PaddingValues(
        horizontal = if (useVerticalToolbar) 0.dp else CONTENT_PADDING,
        vertical = if (useVerticalToolbar) CONTENT_PADDING else 0.dp,
    )

    fun screenContentPaddingValues(useVerticalToolbar: Boolean) = PaddingValues(
        start = 0.dp,
        end = if (useVerticalToolbar) VERTICAL_BAR_WIDTH + BAR_PADDING else 0.dp,
        top = 0.dp,
        bottom = if (useVerticalToolbar) 0.dp else HORIZONTAL_BAR_HEIGHT + BAR_PADDING
    )

}
