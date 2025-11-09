package nerd.tuxmobil.fahrplan.congress.utils.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalWindowInfo
import kotlinx.coroutines.flow.first

@Composable
fun RequestFocusOnLaunch(focusRequester: FocusRequester) {
    val windowInfo = LocalWindowInfo.current
    LaunchedEffect(windowInfo) {
        snapshotFlow { windowInfo.isWindowFocused }.first { it }
        focusRequester.requestFocus()
    }
}
