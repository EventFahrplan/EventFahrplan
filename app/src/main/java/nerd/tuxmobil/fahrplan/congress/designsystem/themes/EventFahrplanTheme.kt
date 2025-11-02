package nerd.tuxmobil.fahrplan.congress.designsystem.themes

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import nerd.tuxmobil.fahrplan.congress.designsystem.colors.ColorScheme
import nerd.tuxmobil.fahrplan.congress.designsystem.colors.LocalColorScheme
import nerd.tuxmobil.fahrplan.congress.designsystem.colors.darkColorScheme
import nerd.tuxmobil.fahrplan.congress.designsystem.colors.lightColorScheme
import nerd.tuxmobil.fahrplan.congress.designsystem.colors.toMaterial3ColorScheme
import nerd.tuxmobil.fahrplan.congress.designsystem.typography.LocalTypography
import nerd.tuxmobil.fahrplan.congress.designsystem.typography.Typography
import nerd.tuxmobil.fahrplan.congress.designsystem.typography.createEventFahrplanTypography
import nerd.tuxmobil.fahrplan.congress.designsystem.typography.toMaterial3Typography

@Composable
fun EventFahrplanTheme(
    darkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkMode) darkColorScheme() else lightColorScheme()
    val typography = createEventFahrplanTypography(MaterialTheme.typography)

    CompositionLocalProvider(
        LocalColorScheme provides colorScheme,
        LocalTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = colorScheme.toMaterial3ColorScheme(),
            typography = typography.toMaterial3Typography(),
            content = content,
        )
    }

}

object EventFahrplanTheme {

    val colorScheme: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalColorScheme.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

}
