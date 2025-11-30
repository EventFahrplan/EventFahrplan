package nerd.tuxmobil.fahrplan.congress.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.changes.statistic.ChangeStatisticsUiStateFactory
import nerd.tuxmobil.fahrplan.congress.net.errors.ErrorMessage
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.repositories.AppExecutionContext
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository

internal class MainViewModelFactory(
    private val repository: AppRepository,
    private val notificationHelper: NotificationHelper,
    private val errorMessageFactory: ErrorMessage.Factory,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val logging = Logging.get()
        return MainViewModel(
            repository = repository,
            notificationHelper = notificationHelper,
            changeStatisticsUiStateFactory = ChangeStatisticsUiStateFactory(
                logging = logging,
            ),
            errorMessageFactory = errorMessageFactory,
            executionContext = AppExecutionContext,
        ) as T
    }

}
