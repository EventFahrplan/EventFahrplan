package nerd.tuxmobil.fahrplan.congress.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.ligi.tracedroid.logging.Log;

import java.util.List;

import info.metadude.android.eventfahrplan.commons.temporal.Moment;
import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmReceiver;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices;
import nerd.tuxmobil.fahrplan.congress.autoupdate.UpdateService;
import nerd.tuxmobil.fahrplan.congress.dataconverters.AlarmExtensions;
import nerd.tuxmobil.fahrplan.congress.models.Alarm;
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc;

public final class OnBootReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "onBoot";

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

        MyApp.LogDebug(LOG_TAG, "onReceive (reboot)");

        Moment nowMoment = Moment.now().plusSeconds(15);

        AppRepository appRepository = AppRepository.INSTANCE;
        List<Alarm> alarms = appRepository.readAlarms();
        AlarmServices alarmServices = AlarmServices.newInstance(context, appRepository);
        for (Alarm alarm : alarms) {
            Moment storedAlarmTime = Moment.ofEpochMilli(alarm.getStartTime());
            if (nowMoment.isBefore(storedAlarmTime)) {
                Log.d(getClass().getSimpleName(), "Scheduling alarm for session: " + alarm.getSessionId() + ", " + alarm.getSessionTitle());
                SchedulableAlarm schedulableAlarm = AlarmExtensions.toSchedulableAlarm(alarm);
                alarmServices.scheduleSessionAlarm(schedulableAlarm);
            } else {
                MyApp.LogDebug(LOG_TAG, "Deleting alarm from database: " + alarm);
                appRepository.deleteAlarmForAlarmId(alarm.getId());
            }
        }

        // start auto updates
        boolean isAutoUpdateEnabled = appRepository.readAutoUpdateEnabled();
        if (isAutoUpdateEnabled) {
            long lastFetchedAt = appRepository.readScheduleLastFetchedAt();
            long nowMillis = Moment.now().toMilliseconds();

            long interval = FahrplanMisc.setUpdateAlarm(context, true);

            MyApp.LogDebug(LOG_TAG, "now: " + nowMillis + ", lastFetchedAt: " + lastFetchedAt);
            if (interval > 0 && nowMillis - lastFetchedAt >= interval) {
                UpdateService.start(context);
            }
        }
    }

}
