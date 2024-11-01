package nerd.tuxmobil.fahrplan.congress.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.commons.DateFormatterDelegate
import nerd.tuxmobil.fahrplan.congress.navigation.C3nav
import nerd.tuxmobil.fahrplan.congress.navigation.RoomForC3NavConverter
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.repositories.AppExecutionContext
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import nerd.tuxmobil.fahrplan.congress.utils.FeedbackUrlComposer
import nerd.tuxmobil.fahrplan.congress.utils.MarkdownConverter
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatter
import nerd.tuxmobil.fahrplan.congress.utils.SessionUrlComposer

internal class SessionDetailsViewModelFactory(

    private val appRepository: AppRepository,
    private val alarmServices: AlarmServices,
    private val notificationHelper: NotificationHelper,
    private val defaultEngelsystemRoomName: String,
    private val customEngelsystemRoomName: String

) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SessionDetailsViewModel(
            repository = appRepository,
            executionContext = AppExecutionContext,
            alarmServices = alarmServices,
            notificationHelper = notificationHelper,
            sessionPropertiesFormatter = SessionPropertiesFormatter(),
            simpleSessionFormat = SimpleSessionFormat(),
            jsonSessionFormat = JsonSessionFormat(),
            feedbackUrlComposer = FeedbackUrlComposer(),
            sessionUrlComposition = SessionUrlComposer(),
            indoorNavigation = C3nav(BuildConfig.C3NAV_URL, RoomForC3NavConverter()),
            markdownConversion = MarkdownConverter,
            formattingDelegate = DateFormatterDelegate,
            defaultEngelsystemRoomName = defaultEngelsystemRoomName,
            customEngelsystemRoomName = customEngelsystemRoomName
        ) as T
    }

}
