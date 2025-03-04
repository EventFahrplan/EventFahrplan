package nerd.tuxmobil.fahrplan.congress.designsystem.themes

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import nerd.tuxmobil.fahrplan.congress.designsystem.colors.LocalColorScheme
import nerd.tuxmobil.fahrplan.congress.designsystem.colors.darkColorScheme
import nerd.tuxmobil.fahrplan.congress.designsystem.colors.lightColorScheme

@Composable
fun EventFahrplanTheme(
    darkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkMode) darkColorScheme() else lightColorScheme()

    CompositionLocalProvider(
        LocalColorScheme provides colorScheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            // typography = Material Design's default type scale uses Roboto, see "main/assets" folder
            content = content,
        )
    }

}
