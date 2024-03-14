package nerd.tuxmobil.fahrplan.congress.commons

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

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
