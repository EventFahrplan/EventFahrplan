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
)

internal val LocalColorScheme = staticCompositionLocalOf<ColorScheme> {
    error("No ColorScheme provided")
}

private fun Material3ColorScheme.toColorScheme(
    listItemPrefixSymbol: Color,
    divider: Color,
    searchBarDivider: Color,
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
    )
}
