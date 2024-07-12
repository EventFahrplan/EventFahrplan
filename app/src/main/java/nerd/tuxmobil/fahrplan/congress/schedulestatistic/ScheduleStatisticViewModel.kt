package nerd.tuxmobil.fahrplan.congress.schedulestatistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import info.metadude.android.eventfahrplan.database.models.ColumnStatistic
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.schedulestatistic.ScheduleStatisticState.Loading
import nerd.tuxmobil.fahrplan.congress.schedulestatistic.ScheduleStatisticState.Success
import nerd.tuxmobil.fahrplan.congress.schedulestatistic.ScheduleStatisticViewEvent.OnBackClick
import nerd.tuxmobil.fahrplan.congress.schedulestatistic.ScheduleStatisticViewEvent.OnToggleSorting

class ScheduleStatisticViewModel(
    repository: AppRepository,
) : ViewModel() {

    private val mutableScheduleStatisticState = MutableStateFlow<ScheduleStatisticState>(Loading)
    val scheduleStatisticState = mutableScheduleStatisticState.asStateFlow()

    private val mutableNavigateBack = Channel<Unit>()
    val navigateBack = mutableNavigateBack.receiveAsFlow()

    private val worseFirst = MutableStateFlow(true)

    init {
        combine(repository.scheduleStatistic, worseFirst) { statistic, worseFirst ->
            val stats = statistic.filterNot { it.countNone == 0 && it.countPresent == 0 }
            Success(
                when (worseFirst) {
                    true -> stats.sortedByDescending(ColumnStatistic::countNone)
                    false -> stats.sortedBy(ColumnStatistic::name)
                }
            )
        }
            .onEach { mutableScheduleStatisticState.value = it }
            .launchIn(viewModelScope)
    }

    fun onViewEvent(viewEvent: ScheduleStatisticViewEvent) {
        when (viewEvent) {
            OnBackClick -> mutableNavigateBack.sendOneTimeEvent(Unit)
            OnToggleSorting -> worseFirst.value = !worseFirst.value
        }
    }

    private fun <E> SendChannel<E>.sendOneTimeEvent(event: E) {
        viewModelScope.launch {
            send(event)
        }
    }

}
