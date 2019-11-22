package nerd.tuxmobil.fahrplan.congress.alarms;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import org.ligi.tracedroid.logging.Log;

import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.autoupdate.UpdateService;
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys;
import nerd.tuxmobil.fahrplan.congress.exceptions.BuilderException;
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;
import nerd.tuxmobil.fahrplan.congress.schedule.MainActivity;

public final class AlarmReceiver extends BroadcastReceiver {

    public static final String ALARM_LECTURE = "nerd.tuxmobil.fahrplan.congress.ALARM_LECTURE";

    public static final String ALARM_UPDATE = "nerd.tuxmobil.fahrplan.congress.ALARM_UPDATE";

    public static final String ALARM_DELETE = "de.machtnix.fahrplan.ALARM";

    private static final String LOG_TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        MyApp.LogDebug(LOG_TAG, "got alarm");
        MyApp.LogDebug(LOG_TAG, "action " + intent.getAction());

        if (intent.getAction().equals(ALARM_LECTURE)) {
            String lectureId = intent.getStringExtra(BundleKeys.ALARM_LECTURE_ID);
            Log.d(getClass().getName(), "onReceive: lectureId: " + lectureId + ", intent: " + intent);
            int lid = Integer.parseInt(lectureId);
            int day = intent.getIntExtra(BundleKeys.ALARM_DAY, 1);
            long when = intent
                    .getLongExtra(BundleKeys.ALARM_START_TIME, System.currentTimeMillis());
            String title = intent.getStringExtra(BundleKeys.ALARM_TITLE);
            //Toast.makeText(context, "Alarm worked.", Toast.LENGTH_LONG).show();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean defaultValue = context.getResources().getBoolean(R.bool.preferences_insistent_alarm_enabled_default_value);
            boolean insistent = prefs.getBoolean("insistent", defaultValue);

            Intent notificationIntent = new Intent(context, MainActivity.class);
            notificationIntent.putExtra("lecture_id", lectureId);
            notificationIntent.putExtra("day", day);
            notificationIntent.setFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            PendingIntent contentIntent = PendingIntent
                    .getActivity(context, lid, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

            NotificationHelper notificationHelper = new NotificationHelper(context);
            String defaultReminderTone = context.getString(R.string.preferences_reminder_tone_default_value);
            Uri soundUri = Uri.parse(prefs.getString("reminder_tone", defaultReminderTone));
            NotificationCompat.Builder builder = notificationHelper.getEventAlarmNotificationBuilder(contentIntent, title, when, soundUri);
            MyApp.LogDebug("alarm", "insistent is " + insistent);
            notificationHelper.notify(NotificationHelper.EVENT_ALARM_ID, builder, insistent);

            AppRepository.INSTANCE.deleteAlarmForEventId(lectureId);

            if (MainActivity.getInstance() != null) {
                MainActivity.getInstance().reloadAlarms();
                MainActivity.getInstance().refreshEventMarkers();
            }
        } else if (intent.getAction().equals(ALARM_UPDATE)) {
            UpdateService.start(context);
        }
    }

    private static Intent getAddAlarmIntent(@NonNull Context context,
                                            @NonNull String lectureId,
                                            int day,
                                            @NonNull String title,
                                            long startTime) {
        return getAlarmIntent(context, lectureId, day, title, startTime, ALARM_LECTURE);
    }

    private static Intent getDeleteAlarmIntent(@NonNull Context context,
                                               @NonNull String lectureId,
                                               int day,
                                               @NonNull String title,
                                               long startTime) {
        return getAlarmIntent(context, lectureId, day, title, startTime, ALARM_DELETE);
    }

    private static Intent getAlarmIntent(@NonNull Context context,
                                         @NonNull String lectureId,
                                         int day,
                                         @NonNull String title,
                                         long startTime,
                                         @NonNull String intentAction) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(BundleKeys.ALARM_LECTURE_ID, lectureId);
        intent.putExtra(BundleKeys.ALARM_DAY, day);
        intent.putExtra(BundleKeys.ALARM_TITLE, title);
        intent.putExtra(BundleKeys.ALARM_START_TIME, startTime);
        intent.setAction(intentAction);
        intent.setData(Uri.parse("alarm://" + lectureId));
        return intent;
    }

    public static class AlarmIntentBuilder {

        private Context context = null;
        private String lectureId = null;
        private int day = Integer.MAX_VALUE;
        private String title = null;
        private long startTime = Long.MIN_VALUE;
        private boolean isAddAlarmIntent = true;
        private boolean addMethodWasInvoked = false;
        private boolean deleteMethodWasInvoked = false;

        public AlarmIntentBuilder setContext(@NonNull Context context) {
            this.context = context;
            return this;
        }

        public AlarmIntentBuilder setLectureId(@NonNull String lectureId) {
            this.lectureId = lectureId;
            return this;
        }

        public AlarmIntentBuilder setDay(int day) {
            this.day = day;
            return this;
        }

        public AlarmIntentBuilder setTitle(@NonNull String title) {
            this.title = title;
            return this;
        }

        public AlarmIntentBuilder setStartTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public AlarmIntentBuilder setIsAddAlarm() {
            this.isAddAlarmIntent = true;
            this.addMethodWasInvoked = true;
            return this;
        }

        public AlarmIntentBuilder setIsDeleteAlarm() {
            this.isAddAlarmIntent = false;
            this.deleteMethodWasInvoked = true;
            return this;
        }

        public Intent build() {
            if (context == null) {
                throw new BuilderException("Field 'context' is not set.");
            }
            if (lectureId == null) {
                throw new BuilderException("Field 'lectureId' is not set.");
            }
            if (day == Integer.MAX_VALUE) {
                throw new BuilderException("Field 'day' is not set.");
            }
            if (title == null) {
                throw new BuilderException("Field 'title' is not set.");
            }
            if (startTime == Long.MIN_VALUE) {
                throw new BuilderException("Field 'startTime' is not set.");
            }
            if (addMethodWasInvoked && deleteMethodWasInvoked) {
                throw new BuilderException("Either call 'setIsAddAlarm()' OR 'setIsDeleteAlarm()' - not both.");
            }
            if (isAddAlarmIntent) {
                return AlarmReceiver.getAddAlarmIntent(context, lectureId, day, title, startTime);
            } else {
                return AlarmReceiver.getDeleteAlarmIntent(context, lectureId, day, title, startTime);
            }
        }

    }

}
