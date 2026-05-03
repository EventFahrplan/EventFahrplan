package nerd.tuxmobil.fahrplan.congress.extensions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Bottom
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Horizontal
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Top
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable

@Composable
fun WindowInsets.Companion.navigationBarsImeBottomPaddingValues(): PaddingValues {
    return navigationBars.union(ime).only(Bottom).asPaddingValues()
}

@Composable
fun WindowInsets.Companion.safeDrawingHorizontalAndTop(): WindowInsets {
    return safeDrawing.only(Horizontal + Top)
}
