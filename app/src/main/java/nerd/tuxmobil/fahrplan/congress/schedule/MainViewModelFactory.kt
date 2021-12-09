package nerd.tuxmobil.fahrplan.congress.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.repositories.AppExecutionContext
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository

class MainViewModelFactory(

    private val repository: AppRepository

) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val logging = Logging.get()
        return MainViewModel(
            repository = repository,
            executionContext = AppExecutionContext,
            logging = logging
        ) as T
    }

}
