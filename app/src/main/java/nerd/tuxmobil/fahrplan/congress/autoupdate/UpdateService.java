package nerd.tuxmobil.fahrplan.congress.autoupdate;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.text.format.Time;

import java.util.List;

import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.MyApp.TASKS;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;
import nerd.tuxmobil.fahrplan.congress.net.ConnectivityStateReceiver;
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult;
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus;
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;
import nerd.tuxmobil.fahrplan.congress.schedule.MainActivity;
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc;

import static nerd.tuxmobil.fahrplan.congress.net.Connectivity.networkIsAvailable;

public class UpdateService extends IntentService {

    public UpdateService() {
        super("UpdateService");
    }

    final String LOG_TAG = "UpdateService";

    public void onParseDone(Boolean result, String version) {
        MyApp.LogDebug(LOG_TAG, "parseDone: " + result + " , numDays=" + MyApp.meta.getNumDays());
        MyApp.task_running = TASKS.NONE;
        MyApp.fahrplan_xml = null;
        List<Lecture> changesList = FahrplanMisc.readChanges(this);
        if (!changesList.isEmpty()) {
            showScheduleUpdateNotification(version, changesList.size());
        }
        MyApp.LogDebug(LOG_TAG, "background update complete");
        stopSelf();
    }

    private void showScheduleUpdateNotification(String version, int changesCount) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent contentIntent = PendingIntent
                .getActivity(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);

        String contentText;
        if (TextUtils.isEmpty(version)) {
            contentText = getString(R.string.schedule_updated);
        } else {
            contentText = getString(R.string.schedule_updated_to, version);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String defaultReminderTone = getString(R.string.preferences_reminder_tone_default_value);
        String reminderTone = prefs.getString("reminder_tone", defaultReminderTone);
        Uri soundUri = Uri.parse(reminderTone);

        NotificationHelper notificationHelper = new NotificationHelper(this);
        NotificationCompat.Builder builder = notificationHelper.getScheduleUpdateNotificationBuilder(contentIntent, contentText, changesCount, soundUri);
        notificationHelper.notify(NotificationHelper.SCHEDULE_UPDATE_ID, builder);
    }

    public void onGotResponse(@NonNull FetchScheduleResult fetchScheduleResult) {
        HttpStatus status = fetchScheduleResult.getHttpStatus();
        MyApp.task_running = TASKS.NONE;
        if ((status == HttpStatus.HTTP_OK) || (status == HttpStatus.HTTP_NOT_MODIFIED)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            Time now = new Time();
            now.setToNow();
            long millis = now.toMillis(true);
            Editor edit = prefs.edit();
            edit.putLong("last_fetch", millis);
            edit.commit();
        }
        if (status != HttpStatus.HTTP_OK) {
            MyApp.LogDebug(LOG_TAG, "Background schedule update failed. HTTP status code: " + status);
            stopSelf();
            return;
        }

        MyApp.fahrplan_xml = fetchScheduleResult.getScheduleXml();
        MyApp.meta.setETag(fetchScheduleResult.getETag());
        // Parser is automatically invoked when response has been received.
        MyApp.task_running = TASKS.PARSE;
    }

    private void fetchFahrplan() {
        if (MyApp.task_running == TASKS.NONE) {
            MyApp.task_running = TASKS.FETCH;
            AppRepository appRepository = AppRepository.Companion.getInstance(this);
            String url = appRepository.readScheduleUrl();
            appRepository.loadSchedule(url, MyApp.meta.getETag(), fetchScheduleResult -> {
                onGotResponse(fetchScheduleResult);
                return null;
            }, (result, version) -> {
                onParseDone(result, version);
                return null;
            });
        } else {
            MyApp.LogDebug(LOG_TAG, "Fetching already in progress.");
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!networkIsAvailable(this)) {
            MyApp.LogDebug(LOG_TAG, "Network is not available");
            ConnectivityStateReceiver.enableReceiver(this);
            stopSelf();
            return;
        }

        AppRepository appRepository = AppRepository.Companion.getInstance(getApplicationContext());
        MyApp.meta = appRepository.readMeta(); // to load eTag
        MyApp.LogDebug(LOG_TAG, "Fetching schedule ...");
        FahrplanMisc.setUpdateAlarm(this, false);
        fetchFahrplan();
    }

    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, UpdateService.class);
        context.startService(intent);
    }

}
