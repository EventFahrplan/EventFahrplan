package nerd.tuxmobil.fahrplan.congress.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.navigation.RoomForC3NavConverter
import nerd.tuxmobil.fahrplan.congress.repositories.AppExecutionContext
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import nerd.tuxmobil.fahrplan.congress.utils.FeedbackUrlComposer
import nerd.tuxmobil.fahrplan.congress.utils.MarkdownConverter
import nerd.tuxmobil.fahrplan.congress.utils.SessionUrlComposer

class SessionDetailsViewModelFactory(

    private val appRepository: AppRepository,
    private val alarmServices: AlarmServices,
    private val defaultEngelsystemRoomName: String,
    private val customEngelsystemRoomName: String

) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SessionDetailsViewModel(
            repository = appRepository,
            executionContext = AppExecutionContext,
            alarmServices = alarmServices,
            sessionFormatter = SessionFormatter(),
            simpleSessionFormat = SimpleSessionFormat(),
            jsonSessionFormat = JsonSessionFormat(),
            feedbackUrlComposer = FeedbackUrlComposer(),
            sessionUrlComposition = SessionUrlComposer(),
            roomForC3NavConverter = RoomForC3NavConverter(),
            markdownConversion = MarkdownConverter,
            c3NavBaseUrl = BuildConfig.C3NAV_URL,
            defaultEngelsystemRoomName = defaultEngelsystemRoomName,
            customEngelsystemRoomName = customEngelsystemRoomName
        ) as T
    }

}
