package nerd.tuxmobil.fahrplan.congress.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.repositories.AppExecutionContext
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat

class StarredListViewModelFactory(

    private val appRepository: AppRepository

) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val logging = Logging.get()
        @Suppress("UNCHECKED_CAST")
        return StarredListViewModel(
            repository = appRepository,
            executionContext = AppExecutionContext,
            logging = logging,
            simpleSessionFormat = SimpleSessionFormat,
            jsonSessionFormat = JsonSessionFormat()
        ) as T
    }

}
