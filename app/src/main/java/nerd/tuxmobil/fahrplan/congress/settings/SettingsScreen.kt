package nerd.tuxmobil.fahrplan.congress.settings

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
import androidx.navigation.compose.rememberNavController
import info.metadude.android.eventfahrplan.commons.flow.observe
import nerd.tuxmobil.fahrplan.congress.schedulestatistic.ScheduleStatisticActivity
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.NavigateTo
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.SetActivityResult
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
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        viewModel.effects.observe(lifecycleOwner) { effect ->
            when (effect) {
                is NavigateTo -> navController.navigate(effect.destination.route)
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
    }
}
