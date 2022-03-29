package nerd.tuxmobil.fahrplan.congress.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import info.metadude.android.eventfahrplan.commons.logging.Logging;
import info.metadude.android.eventfahrplan.commons.temporal.Moment;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmReceiver;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices;
import nerd.tuxmobil.fahrplan.congress.autoupdate.UpdateService;
import nerd.tuxmobil.fahrplan.congress.dataconverters.AlarmExtensions;
import nerd.tuxmobil.fahrplan.congress.models.Alarm;
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc;

public final class OnBootReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "OnBootReceiver";
    private final Logging logging = Logging.get();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (!action.equals(AlarmReceiver.ALARM_UPDATE)) {
            return;
        }

        logging.report(LOG_TAG, "onReceive (reboot)");

        Moment nowMoment = Moment.now().plusSeconds(15);

        AppRepository appRepository = AppRepository.INSTANCE;
        List<Alarm> alarms = appRepository.readAlarms();
        AlarmServices alarmServices = AlarmServices.newInstance(context, appRepository);
        for (Alarm alarm : alarms) {
            Moment storedAlarmTime = Moment.ofEpochMilli(alarm.getStartTime());
            if (nowMoment.isBefore(storedAlarmTime)) {
                logging.d(LOG_TAG, "Scheduling alarm for session: " + alarm.getSessionId() + ", " + alarm.getSessionTitle());
                SchedulableAlarm schedulableAlarm = AlarmExtensions.toSchedulableAlarm(alarm);
                alarmServices.scheduleSessionAlarm(schedulableAlarm);
            } else {
                logging.d(LOG_TAG, "Deleting alarm from database: " + alarm);
                appRepository.deleteAlarmForAlarmId(alarm.getId());
            }
        }

        // start auto updates
        boolean isAutoUpdateEnabled = appRepository.readAutoUpdateEnabled();
        if (isAutoUpdateEnabled) {
            long lastFetchedAt = appRepository.readScheduleLastFetchedAt();
            long nowMillis = Moment.now().toMilliseconds();

            long interval = FahrplanMisc.setUpdateAlarm(context, true, logging);

            logging.d(LOG_TAG, "now: " + nowMillis + ", lastFetchedAt: " + lastFetchedAt);
            if (interval > 0 && nowMillis - lastFetchedAt >= interval) {
                UpdateService.start(context);
            }
        }
    }

}
