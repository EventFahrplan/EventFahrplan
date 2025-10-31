package nerd.tuxmobil.fahrplan.congress.designsystem.modifiers

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithoutRipple(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalRippleConfiguration provides null) {
        content()
    }
}
