package nerd.tuxmobil.fahrplan.congress.extensions

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Bottom
import androidx.compose.foundation.layout.WindowInsetsSides.Companion.Horizontal
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Modifier.safeContentHorizontalAndBottomPadding(): Modifier {
    return windowInsetsPadding(WindowInsets.safeContent.only(Horizontal + Bottom))
}
