package nerd.tuxmobil.fahrplan.congress.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import info.metadude.android.eventfahrplan.commons.flow.observe
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEffect.SetActivityResult

@Composable
internal fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(context = LocalContext.current),
    ),
    onBack: () -> Unit,
    onSetActivityResult: (List<String>) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        viewModel.effects.observe(lifecycleOwner) { effect ->
            when (effect) {
                is SetActivityResult -> onSetActivityResult(effect.keys)
            }
        }
    }

    // TODO: Add navigation for different settings sub-screens here
    SettingsListScreen(state, onViewEvent = viewModel::onViewEvent, onBack = onBack)
}
