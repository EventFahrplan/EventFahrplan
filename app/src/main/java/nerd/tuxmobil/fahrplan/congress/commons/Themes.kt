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
    background = colorResource(R.color.windowBackground),
    surface = colorResource(android.R.color.transparent), // used by ListItem background
    surfaceContainerHigh = colorResource(android.R.color.transparent), // used by SearchBarDefaults.InputField container background
    outline = colorResource(R.color.colorAccent), // used by SearchBarDefaults.InputField divider
    primary = colorResource(R.color.text_primary),
)

@Composable
private fun lightColorScheme() = androidx.compose.material3.lightColorScheme(
    background = colorResource(R.color.window_background_inverted),
    surface = colorResource(android.R.color.transparent),
    surfaceContainerHigh = colorResource(android.R.color.transparent),
    outline = colorResource(R.color.colorAccent),
    primary = colorResource(R.color.text_primary_inverted),
)
