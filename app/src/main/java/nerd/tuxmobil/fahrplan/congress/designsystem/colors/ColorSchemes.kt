package nerd.tuxmobil.fahrplan.congress.designsystem.colors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import nerd.tuxmobil.fahrplan.congress.R
import androidx.compose.material3.ColorScheme as Material3ColorScheme

@Composable
internal fun darkColorScheme() = androidx.compose.material3.darkColorScheme(
    primary = colorResource(R.color.colorAccent),
    background = colorResource(R.color.windowBackground),
    onBackground = colorResource(R.color.text_primary), // used by LazyColumn -> Text header
    surface = colorResource(android.R.color.transparent), // used by ListItem background
    onSurface = colorResource(R.color.text_primary), // used by ListItem -> headlineContent
    onSurfaceVariant = colorResource(R.color.text_secondary), // used by SearchBarDefaults.InputField placeholder, ListItem -> overlineContent
    inverseOnSurface = colorResource(R.color.session_item_text_on_highlight_background), // used by SessionCard
    surfaceContainer = colorResource(R.color.colorPrimaryDark), // used by DropdownMenu
).toColorScheme(
    listItemPrefixSymbol = colorResource(R.color.session_details_list_item_prefix_symbol),
    divider = colorResource(R.color.divider),
    searchBarDivider = colorResource(R.color.colorAccent),
    sessionListHeaderDayDate = colorResource(R.color.session_list_header_day_date),
    textLink = colorResource(R.color.text_link_on_dark),
    textPastContent = colorResource(R.color.text_past_content_on_dark),
    // AppBar
    appBarContainer = colorResource(R.color.colorPrimary),
    appBarTitleText = colorResource(R.color.text_primary),
    appBarNavigationIcon = colorResource(R.color.text_primary),
    appBarActionIcon = colorResource(R.color.app_bar_action_icon),
    // Alarms screen
    sessionAlarmItemBellIconText = colorResource(R.color.session_alarm_item_bell_icon_text),
    // Session details screen
    sessionDetailBarBackground = colorResource(R.color.session_detailbar_background),
    sessionDetailBarText = colorResource(R.color.session_detailbar_text),
    sessionDetailBarIcon = colorResource(R.color.session_detailbar_icon),
    sessionDetailsRoomStateInfoBackground = colorResource(R.color.session_details_room_state_info_background),
    sessionDetailsRoomStateInfoText = colorResource(R.color.session_details_room_state_info_text),
    // Session card
    sessionCardSelectionStroke = colorResource(R.color.session_drawable_selection_stroke),
    sessionCardBellIcon = colorResource(R.color.session_item_alarm_icon),
    sessionCardTrackNameBackground = colorResource(R.color.session_item_track_name_background),
    sessionCardTrackNameText = colorResource(R.color.session_item_track_name_text),
    // Schedule statistic screen
    scheduleStatisticBarWarningLevel1Background = colorResource(R.color.schedule_statistic_bar_background_warning_level_1),
    scheduleStatisticBarWarningLevel2Background = colorResource(R.color.schedule_statistic_bar_background_warning_level_2),
    scheduleStatisticBarWarningLevel3Background = colorResource(R.color.schedule_statistic_bar_background_warning_level_3),
    scheduleStatisticBarNoWarningBackground = colorResource(R.color.schedule_statistic_bar_background_no_warning),
    // Schedule changes screen
    scheduleChangeUnchangedText = colorResource(R.color.session_list_item_text),
    scheduleChangeNewText = colorResource(R.color.schedule_change_new_on_dark),
    scheduleChangeCanceledText = colorResource(R.color.schedule_change_canceled_on_dark),
    scheduleChangeChangedText = colorResource(R.color.schedule_change_on_dark),
    scheduleChangeBarBackground = colorResource(R.color.schedule_changes_statistic_bar_background_on_dark),
)

@Composable
internal fun lightColorScheme() = androidx.compose.material3.lightColorScheme(
    primary = colorResource(R.color.colorAccent),
    background = colorResource(R.color.window_background_inverted),
    onBackground = colorResource(R.color.text_primary_inverted),
    surface = colorResource(android.R.color.transparent),
    onSurface = colorResource(R.color.text_primary_inverted),
    onSurfaceVariant = colorResource(R.color.text_secondary_inverted),
    inverseOnSurface = colorResource(R.color.session_item_text_on_highlight_background),
    surfaceContainer = colorResource(R.color.colorPrimaryDark),
).toColorScheme(
    listItemPrefixSymbol = colorResource(R.color.session_details_list_item_prefix_symbol),
    divider = colorResource(R.color.divider),
    searchBarDivider = colorResource(R.color.colorAccent),
    sessionListHeaderDayDate = colorResource(R.color.session_list_header_day_date),
    textLink = colorResource(R.color.text_link_on_light),
    textPastContent = colorResource(R.color.text_past_content_on_light),
    // AppBar
    appBarContainer = colorResource(R.color.colorPrimary),
    appBarTitleText = colorResource(R.color.text_primary),
    appBarNavigationIcon = colorResource(R.color.text_primary),
    appBarActionIcon = colorResource(R.color.app_bar_action_icon),
    // Alarms screen
    sessionAlarmItemBellIconText = colorResource(R.color.session_alarm_item_bell_icon_text),
    // Session details screen
    sessionDetailBarBackground = colorResource(R.color.session_detailbar_background),
    sessionDetailBarText = colorResource(R.color.session_detailbar_text),
    sessionDetailBarIcon = colorResource(R.color.session_detailbar_icon),
    sessionDetailsRoomStateInfoBackground = colorResource(R.color.session_details_room_state_info_background),
    sessionDetailsRoomStateInfoText = colorResource(R.color.session_details_room_state_info_text),
    // Session card
    sessionCardSelectionStroke = colorResource(R.color.session_drawable_selection_stroke),
    sessionCardBellIcon = colorResource(R.color.session_item_alarm_icon),
    sessionCardTrackNameBackground = colorResource(R.color.session_item_track_name_background),
    sessionCardTrackNameText = colorResource(R.color.session_item_track_name_text),
    // Schedule statistic screen
    scheduleStatisticBarWarningLevel1Background = colorResource(R.color.schedule_statistic_bar_background_warning_level_1),
    scheduleStatisticBarWarningLevel2Background = colorResource(R.color.schedule_statistic_bar_background_warning_level_2),
    scheduleStatisticBarWarningLevel3Background = colorResource(R.color.schedule_statistic_bar_background_warning_level_3),
    scheduleStatisticBarNoWarningBackground = colorResource(R.color.schedule_statistic_bar_background_no_warning),
    // Schedule changes screen
    scheduleChangeUnchangedText = colorResource(R.color.session_list_item_text),
    scheduleChangeNewText = colorResource(R.color.schedule_change_new_on_light),
    scheduleChangeCanceledText = colorResource(R.color.schedule_change_canceled_on_light),
    scheduleChangeChangedText = colorResource(R.color.schedule_change_on_light),
    scheduleChangeBarBackground = colorResource(R.color.schedule_changes_statistic_bar_background_on_light),
)

internal val LocalColorScheme = staticCompositionLocalOf<ColorScheme> {
    error("No ColorScheme provided")
}

private fun Material3ColorScheme.toColorScheme(
    listItemPrefixSymbol: Color,
    divider: Color,
    searchBarDivider: Color,
    sessionListHeaderDayDate: Color,
    textLink: Color,
    textPastContent: Color,
    appBarContainer: Color,
    appBarTitleText: Color,
    appBarNavigationIcon: Color,
    appBarActionIcon: Color,
    sessionAlarmItemBellIconText: Color,
    sessionDetailBarBackground: Color,
    sessionDetailBarText: Color,
    sessionDetailBarIcon: Color,
    sessionDetailsRoomStateInfoBackground: Color,
    sessionDetailsRoomStateInfoText: Color,
    sessionCardSelectionStroke: Color,
    sessionCardBellIcon: Color,
    sessionCardTrackNameBackground: Color,
    sessionCardTrackNameText: Color,
    scheduleStatisticBarWarningLevel1Background: Color,
    scheduleStatisticBarWarningLevel2Background: Color,
    scheduleStatisticBarWarningLevel3Background: Color,
    scheduleStatisticBarNoWarningBackground: Color,
    scheduleChangeUnchangedText: Color,
    scheduleChangeNewText: Color,
    scheduleChangeCanceledText: Color,
    scheduleChangeChangedText: Color,
    scheduleChangeBarBackground: Color,
): ColorScheme {
    return ColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = inversePrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = surfaceTint,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        outline = outline,
        outlineVariant = outlineVariant,
        scrim = scrim,
        surfaceBright = surfaceBright,
        surfaceDim = surfaceDim,
        surfaceContainer = surfaceContainer,
        surfaceContainerHigh = surfaceContainerHigh,
        surfaceContainerHighest = surfaceContainerHighest,
        surfaceContainerLow = surfaceContainerLow,
        surfaceContainerLowest = surfaceContainerLowest,
        primaryFixed = primaryFixed,
        primaryFixedDim = primaryFixedDim,
        onPrimaryFixed = onPrimaryFixed,
        onPrimaryFixedVariant = onPrimaryFixedVariant,
        secondaryFixed = secondaryFixed,
        secondaryFixedDim = secondaryFixedDim,
        onSecondaryFixed = onSecondaryFixed,
        onSecondaryFixedVariant = onSecondaryFixedVariant,
        tertiaryFixed = tertiaryFixed,
        tertiaryFixedDim = tertiaryFixedDim,
        onTertiaryFixed = onTertiaryFixed,
        onTertiaryFixedVariant = onTertiaryFixedVariant,

        // Custom colors
        listItemPrefixSymbol = listItemPrefixSymbol,
        divider = divider,
        searchBarDivider = searchBarDivider,
        sessionListHeaderDayDate = sessionListHeaderDayDate,
        textLink = textLink,
        textPastContent = textPastContent,
        appBarContainer = appBarContainer,
        appBarTitleText = appBarTitleText,
        appBarNavigationIcon = appBarNavigationIcon,
        appBarActionIcon = appBarActionIcon,
        sessionAlarmItemBellIconText = sessionAlarmItemBellIconText,
        sessionDetailBarBackground = sessionDetailBarBackground,
        sessionDetailBarText = sessionDetailBarText,
        sessionDetailBarIcon = sessionDetailBarIcon,
        sessionDetailsRoomStateInfoBackground = sessionDetailsRoomStateInfoBackground,
        sessionDetailsRoomStateInfoText = sessionDetailsRoomStateInfoText,
        sessionCardSelectionStroke = sessionCardSelectionStroke,
        sessionCardBellIcon = sessionCardBellIcon,
        sessionCardTrackNameBackground = sessionCardTrackNameBackground,
        sessionCardTrackNameText = sessionCardTrackNameText,
        scheduleStatisticBarWarningLevel1Background = scheduleStatisticBarWarningLevel1Background,
        scheduleStatisticBarWarningLevel2Background = scheduleStatisticBarWarningLevel2Background,
        scheduleStatisticBarWarningLevel3Background = scheduleStatisticBarWarningLevel3Background,
        scheduleStatisticBarNoWarningBackground = scheduleStatisticBarNoWarningBackground,
        scheduleChangeUnchangedText = scheduleChangeUnchangedText,
        scheduleChangeNew = scheduleChangeNewText,
        scheduleChangeCanceled = scheduleChangeCanceledText,
        scheduleChangeChanged = scheduleChangeChangedText,
        scheduleChangeBarBackground = scheduleChangeBarBackground,
    )
}
