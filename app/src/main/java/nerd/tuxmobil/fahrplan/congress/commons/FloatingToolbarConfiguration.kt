package nerd.tuxmobil.fahrplan.congress.commons

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
internal fun useVerticalFloatingToolbar(showInSidePane: Boolean): Boolean {
    val configuration = LocalConfiguration.current
    return showInSidePane || configuration.orientation == ORIENTATION_LANDSCAPE
}
