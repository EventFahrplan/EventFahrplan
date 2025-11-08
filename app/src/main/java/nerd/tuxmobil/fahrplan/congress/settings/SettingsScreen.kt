package nerd.tuxmobil.fahrplan.congress.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.activity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import info.metadude.android.eventfahrplan.commons.flow.observe
import nerd.tuxmobil.fahrplan.congress.schedulestatistic.ScheduleStatisticActivity
import nerd.tuxmobil.fahrplan.congress.settings.AlarmToneResult.AlarmToneUri
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.LaunchNotificationSettingsScreen
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.NavigateBack
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.NavigateTo
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.PickAlarmTone
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.SetActivityResult
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.SetAlarmTone
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.SetAlarmTime
import nerd.tuxmobil.fahrplan.congress.settings.SettingsNavigationDestination.AlarmTime
import nerd.tuxmobil.fahrplan.congress.settings.SettingsNavigationDestination.ScheduleStatistic
import nerd.tuxmobil.fahrplan.congress.settings.SettingsNavigationDestination.SettingsList

@Composable
internal fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context = LocalContext.current),
    ),
    onBack: () -> Unit,
    onSetActivityResult: (List<String>) -> Unit,
) {
    val navController = rememberNavController()
    val pickAlarmToneLauncher = rememberLauncherForActivityResult(PickAlarmToneContract()) { result ->
        if (result is AlarmToneUri) {
            viewModel.onViewEvent(SetAlarmTone(result.alarmToneUri))
        }
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    LaunchedEffect(lifecycleOwner) {
        viewModel.effects.observe(lifecycleOwner) { effect ->
            when (effect) {
                is NavigateTo -> navController.navigate(effect.destination.route)
                NavigateBack -> navController.popBackStack()
                LaunchNotificationSettingsScreen -> context.launchSystemNotificationScreen()
                is PickAlarmTone -> pickAlarmToneLauncher.launch(effect.currentAlarmTone)
                is SetActivityResult -> onSetActivityResult(effect.keys)
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = SettingsList.route,
    ) {
        composable(route = SettingsList.route) {
            SettingsListScreen(state, onViewEvent = viewModel::onViewEvent, onBack = onBack)
        }
        activity(route = ScheduleStatistic.route) {
            activityClass = ScheduleStatisticActivity::class
        }
        dialog(route = AlarmTime.route) {
            AlarmTimeDialog(
                currentValue = state.settings.alarmTime,
                onOptionSelected = { viewModel.onViewEvent(SetAlarmTime(it)) },
                onDismiss = { navController.popBackStack() },
            )
        }
    }
}

private fun Context.launchSystemNotificationScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        startActivity(intent)
    }
}
