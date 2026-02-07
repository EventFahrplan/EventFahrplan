package nerd.tuxmobil.fahrplan.congress.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun SessionDetailsScreen(
    viewModel: SessionDetailsViewModel,
) {
    with(viewModel) {
        val state by sessionDetailsState.collectAsStateWithLifecycle()
        val roomStateMessage by roomStateMessage.collectAsStateWithLifecycle()
        val showRoomState = showRoomState
        SessionDetailsContent(
            sessionDetailsState = state,
            onViewEvent = viewModel::onViewEvent,
            showRoomState = showRoomState,
            roomStateMessage = roomStateMessage,
        )
    }
}
