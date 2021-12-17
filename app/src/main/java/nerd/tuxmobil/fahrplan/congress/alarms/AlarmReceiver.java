package nerd.tuxmobil.fahrplan.congress.alarms;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import org.ligi.tracedroid.logging.Log;

import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.autoupdate.UpdateService;
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys;
import nerd.tuxmobil.fahrplan.congress.exceptions.BuilderException;
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;
import nerd.tuxmobil.fahrplan.congress.schedule.MainActivity;

public final class AlarmReceiver extends BroadcastReceiver {

    public static final String ALARM_SESSION = "nerd.tuxmobil.fahrplan.congress.ALARM_SESSION";

    public static final String ALARM_UPDATE = "nerd.tuxmobil.fahrplan.congress.ALARM_UPDATE";

    private static final String ALARM_DISMISSED = "nerd.tuxmobil.fahrplan.congress.ALARM_DISMISSED";

    public static final String ALARM_DELETE = "de.machtnix.fahrplan.ALARM";

    private static final String LOG_TAG = "AlarmReceiver";

    private static final String BUNDLE_KEY_NOTIFICATION_ID = "BUNDLE_KEY_NOTIFICATION_ID";
    private static final int INVALID_NOTIFICATION_ID = -1;
    private static final int DEFAULT_REQUEST_CODE = 0;
    private static final int NO_FLAGS = 0;

    private final AppRepository appRepository = AppRepository.INSTANCE;

    @Override
    public void onReceive(Context context, Intent intent) {

        MyApp.LogDebug(LOG_TAG, "Received alarm = " + intent.getAction() + ".");

        if (ALARM_SESSION.equals(intent.getAction())) {
            String sessionId = intent.getStringExtra(BundleKeys.ALARM_SESSION_ID);
            Log.d(LOG_TAG, "sessionId = " + sessionId + ", intent = " + intent);
            int day = intent.getIntExtra(BundleKeys.ALARM_DAY, 1);
            long when = intent
                    .getLongExtra(BundleKeys.ALARM_START_TIME, System.currentTimeMillis());
            String title = intent.getStringExtra(BundleKeys.ALARM_TITLE);
            //Toast.makeText(context, "Alarm worked.", Toast.LENGTH_LONG).show();

            int uniqueNotificationId = appRepository.createSessionAlarmNotificationId(sessionId);
            Intent launchIntent = MainActivity.createLaunchIntent(context, sessionId, day, uniqueNotificationId);
            PendingIntent contentIntent = PendingIntent
                    .getActivity(context, DEFAULT_REQUEST_CODE, launchIntent, PendingIntent.FLAG_ONE_SHOT);

            NotificationHelper notificationHelper = new NotificationHelper(context);
            Uri soundUri = appRepository.readAlarmToneUri();

            Intent deleteNotificationIntent = createDeleteNotificationIntent(context, uniqueNotificationId);
            PendingIntent deleteBroadcastIntent = PendingIntent
                    .getBroadcast(context, DEFAULT_REQUEST_CODE, deleteNotificationIntent, NO_FLAGS);

            NotificationCompat.Builder builder = notificationHelper.getSessionAlarmNotificationBuilder(
                    contentIntent, title, when, soundUri, deleteBroadcastIntent);
            boolean isInsistentAlarmsEnabled = appRepository.readInsistentAlarmsEnabled();
            MyApp.LogDebug(LOG_TAG, "Preference 'isInsistentAlarmsEnabled' = " + isInsistentAlarmsEnabled + ".");
            notificationHelper.notify(uniqueNotificationId, builder, isInsistentAlarmsEnabled);

            appRepository.deleteAlarmForSessionId(sessionId);

        } else if (ALARM_DISMISSED.equals(intent.getAction())) {
            onSessionAlarmNotificationDismissed(intent);

        } else if (ALARM_UPDATE.equals(intent.getAction())) {
            UpdateService.start(context);
        }
    }

    private void onSessionAlarmNotificationDismissed(@NonNull Intent intent) {
        int notificationId = intent.getIntExtra(BUNDLE_KEY_NOTIFICATION_ID, INVALID_NOTIFICATION_ID);
        if (notificationId == INVALID_NOTIFICATION_ID) {
            throw new IllegalStateException("Bundle does not contain NOTIFICATION_ID.");
        }
        appRepository.deleteSessionAlarmNotificationId(notificationId);
    }

    /**
     * Returns a unique {@link Intent} to delete the data associated with the
     * given session alarm {@code notificationId}.
     * The {@code Intent} is composed with a fake data URI to comply with the uniqueness rules
     * defined by {@link Intent#filterEquals}.
     */
    private static Intent createDeleteNotificationIntent(@NonNull Context context, int notificationId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ALARM_DISMISSED);
        intent.putExtra(BUNDLE_KEY_NOTIFICATION_ID, notificationId);
        intent.setData(Uri.parse("fake://" + notificationId));
        return intent;
    }

    private static Intent getAddAlarmIntent(@NonNull Context context,
                                            @NonNull String sessionId,
                                            int day,
                                            @NonNull String title,
                                            long startTime) {
        return getAlarmIntent(context, sessionId, day, title, startTime, ALARM_SESSION);
    }

    private static Intent getDeleteAlarmIntent(@NonNull Context context,
                                               @NonNull String sessionId,
                                               int day,
                                               @NonNull String title,
                                               long startTime) {
        return getAlarmIntent(context, sessionId, day, title, startTime, ALARM_DELETE);
    }

    private static Intent getAlarmIntent(@NonNull Context context,
                                         @NonNull String sessionId,
                                         int day,
                                         @NonNull String title,
                                         long startTime,
                                         @NonNull String intentAction) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(BundleKeys.ALARM_SESSION_ID, sessionId);
        intent.putExtra(BundleKeys.ALARM_DAY, day);
        intent.putExtra(BundleKeys.ALARM_TITLE, title);
        intent.putExtra(BundleKeys.ALARM_START_TIME, startTime);
        intent.setAction(intentAction);
        intent.setData(Uri.parse("alarm://" + sessionId));
        return intent;
    }

    public static class AlarmIntentBuilder {

        private Context context = null;
        private String sessionId = null;
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

        public AlarmIntentBuilder setSessionId(@NonNull String sessionId) {
            this.sessionId = sessionId;
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
            if (sessionId == null) {
                throw new BuilderException("Field 'sessionId' is not set.");
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
                return AlarmReceiver.getAddAlarmIntent(context, sessionId, day, title, startTime);
            } else {
                return AlarmReceiver.getDeleteAlarmIntent(context, sessionId, day, title, startTime);
            }
        }

    }

}
