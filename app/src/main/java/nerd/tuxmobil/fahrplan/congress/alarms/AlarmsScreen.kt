package nerd.tuxmobil.fahrplan.congress.alarms

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import info.metadude.android.eventfahrplan.commons.flow.observe
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsDestination.AlarmsList
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsEffect.NavigateTo
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsEffect.NavigateToSession
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsViewEvent.OnDeleteAllClick
import nerd.tuxmobil.fahrplan.congress.designsystem.dialogs.ConfirmationDialog

@Composable
internal fun AlarmsScreen(
    viewModel: AlarmsViewModel,
    showInSidePane: Boolean,
    onNavigateToSession: (String) -> Unit,
) {
    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        viewModel.effects.observe(lifecycleOwner) { effect ->
            when (effect) {
                is NavigateTo -> navController.navigate(effect.destination.route)
                is NavigateToSession -> onNavigateToSession(effect.sessionId)
            }
        }
    }

    val state by viewModel.alarmsState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = AlarmsList.route,
    ) {
        composable(AlarmsList.route) {
            AlarmsContent(
                state = state,
                showInSidePane = showInSidePane,
                onViewEvent = viewModel::onViewEvent,
            )
        }
        dialog(AlarmsDestination.ConfirmDeleteAll.route) {
            ConfirmationDialog(
                title = stringResource(R.string.alarms_delete_all_alarms),
                confirmationButtonText = stringResource(R.string.alarms_delete_all),
                onConfirm = {
                    viewModel.onViewEvent(OnDeleteAllClick)
                    navController.popBackStack()
                },
                onDismiss = { navController.popBackStack() },
            )
        }
    }

}
