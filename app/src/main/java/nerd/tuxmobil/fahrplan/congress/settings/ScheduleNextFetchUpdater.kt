package nerd.tuxmobil.fahrplan.congress.settings

import android.content.Context
import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc

internal class ScheduleNextFetchUpdater(
    private val context: Context,
    private val logging: Logging,
    private val appRepository: AppRepository,
    private val alarmServices: AlarmServices,
) {
    fun update(isAutoUpdateEnabled: Boolean) {
        if (isAutoUpdateEnabled) {
            FahrplanMisc.setUpdateAlarm(
                context = context,
                conferenceTimeFrame = appRepository.loadConferenceTimeFrame(),
                isInitial = true,
                logging = logging,
                onCancelScheduleNextFetch = appRepository::deleteScheduleNextFetch,
                onUpdateScheduleNextFetch = appRepository::updateScheduleNextFetch,
            )
        } else {
            appRepository.deleteScheduleNextFetch()
            alarmServices.discardAutoUpdateAlarm()
        }
    }

    companion object {
        fun newInstance(context: Context): ScheduleNextFetchUpdater {
            return ScheduleNextFetchUpdater(
                context = context,
                logging = Logging.get(),
                appRepository = AppRepository,
                alarmServices = AlarmServices.newInstance(context, AppRepository),
            )
        }
    }
}
