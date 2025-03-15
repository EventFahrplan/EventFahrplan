package nerd.tuxmobil.fahrplan.congress.designsystem.indicators

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator as Material3CircularProgressIndicator

@Composable
fun IndicatorCircularProgress(
    modifier: Modifier = Modifier,
) {
    Material3CircularProgressIndicator(
        modifier = modifier.size(48.dp),
    )
}
