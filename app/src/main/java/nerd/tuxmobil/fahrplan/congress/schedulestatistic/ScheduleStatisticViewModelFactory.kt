package nerd.tuxmobil.fahrplan.congress.schedulestatistic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository

class ScheduleStatisticViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ScheduleStatisticViewModel(AppRepository) as T
    }

}
