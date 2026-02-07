package nerd.tuxmobil.fahrplan.congress.schedulestatistic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import info.metadude.android.eventfahrplan.commons.flow.observe
import nerd.tuxmobil.fahrplan.congress.schedulestatistic.ScheduleStatisticEffect.NavigateBack

@Composable
internal fun ScheduleStatisticScreen(
    viewModel: ScheduleStatisticViewModel = viewModel(
        factory = ScheduleStatisticViewModelFactory()
    ),
    onBack: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        viewModel.effects.observe(lifecycleOwner) { effect ->
            when (effect) {
                NavigateBack -> onBack()
            }
        }
    }

    val state by viewModel.scheduleStatisticState.collectAsStateWithLifecycle()

    ScheduleStatisticContent(
        state = state,
        onViewEvent = viewModel::onViewEvent,
    )
}
