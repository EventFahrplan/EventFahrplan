package nerd.tuxmobil.fahrplan.congress.commons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

@Composable
fun Loading() {
    Box(
        Modifier.fillMaxSize(),
        Alignment.Center
    ) {
        CircularProgressIndicator(Modifier.size(48.dp))
    }
}

@Composable
fun NoData(text: String) {
    Box(
        Modifier.fillMaxSize(),
        Alignment.Center
    ) {
        Text(
            modifier = Modifier.padding(100.dp),
            text = text,
            lineHeight = TextUnit(1.5f, TextUnitType.Em),
        )
    }
}
