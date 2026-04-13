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

}

object ToolbarMetrics {

    private val VERTICAL_BAR_WIDTH = 56.dp

    val BUTTON_NAVIGATION_WIDTH = VERTICAL_BAR_WIDTH + 8.dp // to match vertical toolbar width

}
