package nerd.tuxmobil.fahrplan.congress.details

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import info.metadude.android.eventfahrplan.commons.flow.observe
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.calendar.CalendarSharing
import nerd.tuxmobil.fahrplan.congress.commons.AlarmTimePickerDialog
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsDestination.PickAlarmTime
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsDestination.SessionDetails
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.AddToCalendar
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.NavigateTo
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.NavigateToRoom
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.OpenFeedback
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.RequestPostNotificationsPermission
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.RequestScheduleExactAlarmsPermission
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.ShareJson
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.ShareSimple
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsEffect.ShowNotificationsDisabledError
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsViewEvent.OnAddAlarm
import nerd.tuxmobil.fahrplan.congress.extensions.showToast
import nerd.tuxmobil.fahrplan.congress.extensions.startActivity
import nerd.tuxmobil.fahrplan.congress.sharing.SessionSharer

@Composable
internal fun SessionDetailsScreen(
    viewModel: SessionDetailsViewModel,
    showInSidePane: Boolean,
    onBack: () -> Unit,
    onRequestPostNotificationsPermission: () -> Unit,
    onRequestScheduleExactAlarmsPermission: () -> Unit,
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        viewModel.effects.observe(lifecycleOwner) { effect ->
            when (effect) {
                is OpenFeedback -> context.startActivity(Intent(Intent.ACTION_VIEW, effect.uri)) {
                    context.showActivityNotFoundError()
                }

                is ShareJson -> if (!SessionSharer.shareJson(context, effect.formattedSession)) {
                    context.showActivityNotFoundError()
                }

                is ShareSimple -> SessionSharer.shareSimple(context, effect.formattedSession)
                is AddToCalendar -> CalendarSharing(context).addToCalendar(effect.session)
                is NavigateToRoom -> context.startActivity(effect.uri)
                is NavigateTo -> navController.navigate(effect.destination.route)
                is RequestPostNotificationsPermission -> onRequestPostNotificationsPermission()
                is RequestScheduleExactAlarmsPermission -> onRequestScheduleExactAlarmsPermission()
                is ShowNotificationsDisabledError -> context.showNotificationsDisabledError()
            }
        }
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val roomStateMessage by viewModel.roomStateMessage.collectAsStateWithLifecycle()
    val showRoomState = viewModel.showRoomState

    NavHost(
        navController = navController,
        startDestination = SessionDetails.route,
    ) {
        composable(SessionDetails.route) {
            SessionDetailsContent(
                sessionDetailsState = state.sessionDetailsState,
                showInSidePane = showInSidePane,
                onViewEvent = viewModel::onViewEvent,
                showRoomState = showRoomState,
                roomStateMessage = roomStateMessage,
                onBack = onBack,
            )
        }
        dialog(PickAlarmTime.route) {
            AlarmTimePickerDialog(
                title = stringResource(R.string.choose_alarm_time),
                currentValue = state.settings.alarmTime,
                onOptionSelected = { alarmTime ->
                    viewModel.onViewEvent(OnAddAlarm(alarmTime))
                    navController.popBackStack()
                },
                onDismiss = { navController.popBackStack() },
            )
        }
    }
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
