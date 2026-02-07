package nerd.tuxmobil.fahrplan.congress.details

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.commons.BuildConfigProvider
import nerd.tuxmobil.fahrplan.congress.commons.DateFormatterDelegate
import nerd.tuxmobil.fahrplan.congress.commons.ExternalNavigator
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolver
import nerd.tuxmobil.fahrplan.congress.navigation.C3nav
import nerd.tuxmobil.fahrplan.congress.navigation.RoomForC3NavConverter
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.repositories.AppExecutionContext
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.roomstates.RoomStateFormatter
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatter
import nerd.tuxmobil.fahrplan.congress.utils.FeedbackUrlComposer
import nerd.tuxmobil.fahrplan.congress.utils.MarkdownConverter
import nerd.tuxmobil.fahrplan.congress.utils.ServerBackendType
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatter
import nerd.tuxmobil.fahrplan.congress.utils.SessionUrlComposer

internal class SessionDetailsViewModelFactory(
    private val context: Context,
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val resourceResolving = ResourceResolver(context)
        val appRepository = AppRepository
        val alarmServices = AlarmServices.newInstance(context, appRepository)
        val notificationHelper = NotificationHelper(context)
        val externalNavigation = ExternalNavigator(context)
        val defaultEngelsystemRoomName = AppRepository.ENGELSYSTEM_ROOM_NAME
        val customEngelsystemRoomName = context.getString(R.string.engelsystem_shifts_alias)
        val logging = Logging.get()
        val buildConfigProvision = BuildConfigProvider()
        @Suppress("UNCHECKED_CAST")
        return SessionDetailsViewModel(
            repository = appRepository,
            executionContext = AppExecutionContext,
            logging = logging,
            buildConfigProvision = buildConfigProvision,
            alarmServices = alarmServices,
            notificationHelper = notificationHelper,
            externalNavigation = externalNavigation,
            sessionDetailsParameterFactory = SessionDetailsParameterFactory(
                repository = appRepository,
                markupLanguage = ServerBackendType.getMarkupLanguage(buildConfigProvision.serverBackendType),
                sessionPropertiesFormatting = SessionPropertiesFormatter(resourceResolving),
                contentDescriptionFormatting = ContentDescriptionFormatter(resourceResolving),
                formattingDelegate = DateFormatterDelegate,
                markdownConversion = MarkdownConverter,
                sessionUrlComposition = SessionUrlComposer(),
                defaultEngelsystemRoomName = defaultEngelsystemRoomName,
                customEngelsystemRoomName = customEngelsystemRoomName,
            ),
            selectedSessionParameterFactory = SelectedSessionParameterFactory(
                indoorNavigation = C3nav(BuildConfig.C3NAV_URL, RoomForC3NavConverter()),
                feedbackUrlComposition = FeedbackUrlComposer(),
                defaultEngelsystemRoomName = defaultEngelsystemRoomName,
            ),
            simpleSessionFormat = SimpleSessionFormat(),
            jsonSessionFormat = JsonSessionFormat(),
            feedbackUrlComposition = FeedbackUrlComposer(),
            indoorNavigation = C3nav(BuildConfig.C3NAV_URL, RoomForC3NavConverter()),
            roomStateFormatting = RoomStateFormatter(resourceResolving),
        ) as T
    }

}
