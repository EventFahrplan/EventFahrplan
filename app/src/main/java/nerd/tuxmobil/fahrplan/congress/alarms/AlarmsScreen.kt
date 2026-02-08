package nerd.tuxmobil.fahrplan.congress.alarms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun AlarmsScreen(
    viewModel: AlarmsViewModel,
    showInSidePane: Boolean,
) {
    val state by viewModel.alarmsState.collectAsStateWithLifecycle()

    AlarmsContent(
        state = state,
        showInSidePane = showInSidePane,
    )
}
