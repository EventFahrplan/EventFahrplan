package nerd.tuxmobil.fahrplan.congress.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.autoupdate.UpdateService
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSchedulableAlarm
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc

class OnBootReceiver : BroadcastReceiver() {

    private companion object {
        const val LOG_TAG = "OnBootReceiver"
    }

    private val logging = Logging.get()

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        logging.report(LOG_TAG, "onReceive (reboot)")
        val nowMoment = Moment.now().plusSeconds(15)
        val appRepository = AppRepository.apply {
            initialize(
                context = context.applicationContext,
                logging = logging,
            )
        }
        val alarmServices = AlarmServices.newInstance(context, appRepository)
        val alarms = appRepository.readAlarms()
        for (alarm in alarms) {
            // Check if the alarm time has passed
            if (nowMoment.isBefore(Moment.ofEpochMilli(alarm.startTime))) {
                logging.d(LOG_TAG, "Scheduling alarm for session: ${alarm.sessionTitle}, ${alarm.sessionTitle}")
                alarmServices.scheduleSessionAlarm(alarm.toSchedulableAlarm())
            } else {
                logging.d(LOG_TAG, "Deleting alarm from database: $alarm")
                appRepository.deleteAlarmForAlarmId(alarm.id)
            }
        }

        // Start auto updates
        if (appRepository.readAutoUpdateEnabled()) { // Check if auto update is enabled
            val lastFetchedAt = appRepository.readScheduleLastFetchedAt()
            val nowMillis = Moment.now().toMilliseconds()
            val interval = FahrplanMisc.setUpdateAlarm(
                context = context,
                conferenceTimeFrame = appRepository.loadConferenceTimeFrame(),
                isInitial = true,
                logging = logging,
                onCancelScheduleNextFetch = appRepository::deleteScheduleNextFetch,
                onUpdateScheduleNextFetch = appRepository::updateScheduleNextFetch,
            )

            logging.d(LOG_TAG, "now: $nowMillis, lastFetchedAt: $lastFetchedAt")

            if (interval > 0 && nowMillis - lastFetchedAt >= interval) {
                UpdateService.start(context)
            }
        }
    }

}
