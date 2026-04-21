package nerd.tuxmobil.fahrplan.congress.designsystem.modifiers

import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun WithoutRipple(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        content()
    }
}
