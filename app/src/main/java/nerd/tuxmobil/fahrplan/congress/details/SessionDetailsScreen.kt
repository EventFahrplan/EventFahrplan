package nerd.tuxmobil.fahrplan.congress.details

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import info.metadude.android.eventfahrplan.commons.flow.observe
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.calendar.CalendarSharing
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.AddToCalendar
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.CloseDetails
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.NavigateToRoom
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.OpenFeedback
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.RequestPostNotificationsPermission
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.RequestScheduleExactAlarmsPermission
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.ShareJson
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.ShareSimple
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.ShowAlarmTimePicker
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.ShowNotificationsDisabledError
import nerd.tuxmobil.fahrplan.congress.extensions.showToast
import nerd.tuxmobil.fahrplan.congress.extensions.startActivity
import nerd.tuxmobil.fahrplan.congress.sharing.SessionSharer

@Composable
internal fun SessionDetailsScreen(
    viewModel: SessionDetailsViewModel,
    onBack: () -> Unit,
    onRequestPostNotificationsPermission: () -> Unit,
    onRequestScheduleExactAlarmsPermission: () -> Unit,
    onShowAlarmTimePicker: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        viewModel.effects.observe(lifecycleOwner) { effect ->
            when (effect) {
                is OpenFeedback -> {
                    context.startActivity(Intent(Intent.ACTION_VIEW, effect.uri)) {
                        context.showActivityNotFoundError()
                    }
                }

                is ShareJson -> {
                    if (!SessionSharer.shareJson(context, effect.formattedSession)) {
                        context.showActivityNotFoundError()
                    }
                }

                is ShareSimple -> SessionSharer.shareSimple(context, effect.formattedSession)
                is AddToCalendar -> CalendarSharing(context).addToCalendar(effect.session)
                is NavigateToRoom -> context.startActivity(effect.uri)
                is CloseDetails -> onBack()
                is ShowAlarmTimePicker -> onShowAlarmTimePicker()
                is RequestPostNotificationsPermission -> onRequestPostNotificationsPermission()
                is RequestScheduleExactAlarmsPermission -> onRequestScheduleExactAlarmsPermission()
                is ShowNotificationsDisabledError -> context.showNotificationsDisabledError()
            }
        }
    }

    val state by viewModel.sessionDetailsState.collectAsStateWithLifecycle()
    val roomStateMessage by viewModel.roomStateMessage.collectAsStateWithLifecycle()
    val showRoomState = viewModel.showRoomState
    SessionDetailsContent(
        sessionDetailsState = state,
        onViewEvent = viewModel::onViewEvent,
        showRoomState = showRoomState,
        roomStateMessage = roomStateMessage,
    )
}

private fun Context.startActivity(uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(intent)
}

private fun Context.showNotificationsDisabledError() {
    showToast(R.string.alarms_disabled_notifications_are_disabled, showShort = false)
}

private fun Context.showActivityNotFoundError() {
    showToast(R.string.share_error_activity_not_found, showShort = true)
}
