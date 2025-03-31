package nerd.tuxmobil.fahrplan.congress.designsystem.colors

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.colorResource
import nerd.tuxmobil.fahrplan.congress.R

@Composable
internal fun darkColorScheme() = androidx.compose.material3.darkColorScheme(
    background = colorResource(R.color.windowBackground),
    onBackground = colorResource(R.color.text_primary), // used by LazyColumn -> Text header
    surface = colorResource(android.R.color.transparent), // used by ListItem background
    onSurface = colorResource(R.color.text_primary), // used by ListItem -> headlineContent
    onSurfaceVariant = colorResource(R.color.text_secondary), // used by SearchBarDefaults.InputField placeholder, ListItem -> overlineContent
    inverseOnSurface = colorResource(R.color.session_item_text_on_highlight_background), // used by SessionCard
    outline = colorResource(R.color.colorAccent), // used by SearchBarDefaults.InputField divider
    outlineVariant = colorResource(R.color.outline_variant), // used by HorizontalDivider
    surfaceContainer = colorResource(R.color.colorPrimaryDark), // used by DropdownMenu
    surfaceContainerHigh = colorResource(android.R.color.transparent), // used by SearchBarDefaults.InputField container background
)

@Composable
internal fun lightColorScheme() = androidx.compose.material3.lightColorScheme(
    primary = colorResource(R.color.text_primary_inverted),
    background = colorResource(R.color.window_background_inverted),
    onBackground = colorResource(R.color.text_primary_inverted),
    surface = colorResource(android.R.color.transparent),
    onSurface = colorResource(R.color.text_primary_inverted),
    onSurfaceVariant = colorResource(R.color.text_secondary_inverted),
    inverseOnSurface = colorResource(R.color.session_item_text_on_highlight_background),
    outline = colorResource(R.color.colorAccent),
    outlineVariant = colorResource(R.color.outline_variant),
    surfaceContainer = colorResource(R.color.colorPrimaryDark),
    surfaceContainerHigh = colorResource(android.R.color.transparent),
)

internal val LocalColorScheme = staticCompositionLocalOf<ColorScheme> {
    error("No ColorScheme provided")
}
