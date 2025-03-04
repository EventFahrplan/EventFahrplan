package nerd.tuxmobil.fahrplan.congress.designsystem.screenstates

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import nerd.tuxmobil.fahrplan.congress.designsystem.indicators.IndicatorCircularProgress

@Composable
fun Loading() {
    Box(
        Modifier.fillMaxSize(),
        Alignment.Center,
    ) {
        IndicatorCircularProgress()
    }
}
