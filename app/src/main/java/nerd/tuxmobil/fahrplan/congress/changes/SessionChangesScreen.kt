package nerd.tuxmobil.fahrplan.congress.changes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun SessionChangesScreen(
    viewModel: ChangeListViewModel,
    showInSidePane: Boolean,
) {
    val state by viewModel.sessionChangesState.collectAsStateWithLifecycle()

    SessionChangesContent(
        state = state,
        showInSidePane = showInSidePane,
        onViewEvent = viewModel::onViewEvent,
    )
}
