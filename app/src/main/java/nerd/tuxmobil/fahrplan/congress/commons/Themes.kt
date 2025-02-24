package nerd.tuxmobil.fahrplan.congress.commons

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import nerd.tuxmobil.fahrplan.congress.R

@Composable
fun EventFahrplanTheme(
    darkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkMode) darkColorScheme() else lightColorScheme(),
        // typography = Material Design's default type scale uses Roboto, see "main/assets" folder
        content = content
    )
}

@Composable
private fun darkColorScheme() = androidx.compose.material3.darkColorScheme(
    primary = colorResource(R.color.text_primary), // used by CircularProgressIndicator, OutlinedButton -> Text, SearchBarDefaults.InputField cursor
    background = colorResource(R.color.windowBackground),
    onBackground = colorResource(R.color.text_primary), // used by LazyColumn -> Text header
    surface = colorResource(android.R.color.transparent), // used by ListItem background
    onSurface = colorResource(R.color.text_primary), // used by ListItem -> headlineContent
    onSurfaceVariant = colorResource(R.color.text_secondary), // used by SearchBarDefaults.InputField placeholder, ListItem -> overlineContent
    outline = colorResource(R.color.colorAccent), // used by SearchBarDefaults.InputField divider
    outlineVariant = colorResource(R.color.outline_variant), // used by HorizontalDivider
    surfaceContainerHigh = colorResource(android.R.color.transparent), // used by SearchBarDefaults.InputField container background
)

@Composable
private fun lightColorScheme() = androidx.compose.material3.lightColorScheme(
    primary = colorResource(R.color.text_primary_inverted),
    background = colorResource(R.color.window_background_inverted),
    onBackground = colorResource(R.color.text_primary_inverted),
    surface = colorResource(android.R.color.transparent),
    onSurface = colorResource(R.color.text_primary_inverted),
    onSurfaceVariant = colorResource(R.color.text_secondary_inverted),
    outline = colorResource(R.color.colorAccent),
    outlineVariant = colorResource(R.color.outline_variant),
    surfaceContainerHigh = colorResource(android.R.color.transparent),
)
