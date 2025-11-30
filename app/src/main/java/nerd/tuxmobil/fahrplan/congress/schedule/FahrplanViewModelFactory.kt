package nerd.tuxmobil.fahrplan.congress.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.net.errors.ErrorMessage
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.repositories.AppExecutionContext
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat

internal class FahrplanViewModelFactory(

    private val repository: AppRepository,
    private val errorMessageFactory: ErrorMessage.Factory,
    private val alarmServices: AlarmServices,
    private val navigationMenuEntriesGenerator: NavigationMenuEntriesGenerator,
    private val defaultEngelsystemRoomName: String,
    private val customEngelsystemRoomName: String,
    private val notificationHelper: NotificationHelper

) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val logging = Logging.get()
        return FahrplanViewModel(
            repository = repository,
            executionContext = AppExecutionContext,
            logging = logging,
            errorMessageFactory = errorMessageFactory,
            alarmServices = alarmServices,
            notificationHelper = notificationHelper,
            navigationMenuEntriesGenerator = navigationMenuEntriesGenerator,
            simpleSessionFormat = SimpleSessionFormat(),
            jsonSessionFormat = JsonSessionFormat(),
            scrollAmountCalculator = ScrollAmountCalculator(logging),
            defaultEngelsystemRoomName = defaultEngelsystemRoomName,
            customEngelsystemRoomName = customEngelsystemRoomName
        ) as T
    }

}
