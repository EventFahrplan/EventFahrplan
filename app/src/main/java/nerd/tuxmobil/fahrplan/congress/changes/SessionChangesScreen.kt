package nerd.tuxmobil.fahrplan.congress.changes

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import info.metadude.android.eventfahrplan.commons.flow.observe
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeEffect.CancelScheduleUpdateNotification
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeEffect.NavigateToSession
import nerd.tuxmobil.fahrplan.congress.changes.SessionChangeViewEvent.OnScheduleChangesSeen
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper

@Composable
internal fun SessionChangesScreen(
    showInSidePane: Boolean,
    onNavigateToSession: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel = viewModel<ChangeListViewModel>(factory = ChangeListViewModelFactory(context))

    LaunchedEffect(lifecycleOwner) {
        viewModel.effects.observe(lifecycleOwner) { effect ->
            when (effect) {
                is NavigateToSession -> onNavigateToSession(effect.sessionId)
                CancelScheduleUpdateNotification -> context.cancelScheduleUpdateNotification()
            }
        }
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == ON_RESUME) {
                viewModel.onViewEvent(OnScheduleChangesSeen)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val state by viewModel.sessionChangesState.collectAsStateWithLifecycle()

    SessionChangesContent(
        state = state,
        showInSidePane = showInSidePane,
        onViewEvent = viewModel::onViewEvent,
    )

}

private fun Context.cancelScheduleUpdateNotification() {
    NotificationHelper(this).cancelScheduleUpdateNotification()
}
