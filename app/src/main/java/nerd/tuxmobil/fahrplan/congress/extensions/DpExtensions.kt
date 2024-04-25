package nerd.tuxmobil.fahrplan.congress.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit

@Composable
fun Dp.toTextUnit(): TextUnit {
    val density = LocalDensity.current
    return with(density) { this@toTextUnit.toSp() }
}
