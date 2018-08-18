package nerd.tuxmobil.fahrplan.congress.autoupdate;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.text.format.Time;

import org.ligi.tracedroid.logging.Log;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.MyApp.TASKS;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.extensions.Contexts;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;
import nerd.tuxmobil.fahrplan.congress.models.Meta;
import nerd.tuxmobil.fahrplan.congress.net.ConnectivityStateReceiver;
import nerd.tuxmobil.fahrplan.congress.net.CustomHttpClient;
import nerd.tuxmobil.fahrplan.congress.net.CustomHttpClient.HTTP_STATUS;
import nerd.tuxmobil.fahrplan.congress.net.FetchFahrplan;
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult;
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;
import nerd.tuxmobil.fahrplan.congress.schedule.MainActivity;
import nerd.tuxmobil.fahrplan.congress.serialization.FahrplanParser;
import nerd.tuxmobil.fahrplan.congress.serialization.ScheduleChanges;
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc;
import okhttp3.OkHttpClient;

public class UpdateService extends IntentService implements
        FetchFahrplan.OnDownloadCompleteListener,
        FahrplanParser.OnParseCompleteListener {

    public UpdateService() {
        super("UpdateService");
    }

    final String LOG_TAG = "UpdateService";

    private FetchFahrplan fetcher;

    private FahrplanParser parser;

    @Override
    public void onUpdateLectures(@NonNull List<Lecture> lectures) {
        List<Lecture> oldLectures = FahrplanMisc.loadLecturesForAllDays(this);
        boolean hasChanged = ScheduleChanges.hasScheduleChanged(lectures, oldLectures);
        AppRepository appRepository = AppRepository.Companion.getInstance(this);
        if (hasChanged) {
            appRepository.resetChangesSeenFlag();
        }
        appRepository.updateLectures(lectures);
    }

    @Override
    public void onUpdateMeta(@NonNull Meta meta) {
        AppRepository appRepository = AppRepository.Companion.getInstance(this);
        appRepository.updateMeta(meta);
    }

    @Override
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

    public void parseFahrplan() {
        MyApp.task_running = TASKS.PARSE;
        if (MyApp.parser == null) {
            parser = new FahrplanParser();
        } else {
            parser = MyApp.parser;
        }
        parser.setListener(this);
        parser.parse(MyApp.fahrplan_xml, MyApp.meta.getETag());
    }

    public void onGotResponse(@NonNull FetchScheduleResult fetchScheduleResult) {
        HTTP_STATUS status = fetchScheduleResult.getHttpStatus();
        MyApp.LogDebug(LOG_TAG, "Response... " + status);
        MyApp.task_running = TASKS.NONE;
        if ((status == HTTP_STATUS.HTTP_OK) || (status == HTTP_STATUS.HTTP_NOT_MODIFIED)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            Time now = new Time();
            now.setToNow();
            long millis = now.toMillis(true);
            Editor edit = prefs.edit();
            edit.putLong("last_fetch", millis);
            edit.commit();
        }
        if (status != HTTP_STATUS.HTTP_OK) {
            MyApp.LogDebug(LOG_TAG, "background update failed with " + status);
            stopSelf();
            return;
        }

        MyApp.fahrplan_xml = fetchScheduleResult.getScheduleXml();
        MyApp.meta.setETag(fetchScheduleResult.getETag());
        parseFahrplan();
    }

    private void fetchFahrplan(FetchFahrplan.OnDownloadCompleteListener completeListener) {
        if (MyApp.task_running == TASKS.NONE) {
            AppRepository appRepository = AppRepository.Companion.getInstance(this);
            String url = appRepository.readScheduleUrl();
            MyApp.task_running = TASKS.FETCH;
            fetcher.setListener(completeListener);
            fetcher.fetch(url, MyApp.meta.getETag());
        } else {
            MyApp.LogDebug(LOG_TAG, "fetch already in progress");
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        MyApp.LogDebug(LOG_TAG, "onHandleIntent");
        Log.d(getClass().getName(), "intent = " + intent);
        ConnectivityManager connectivityManager = Contexts.getConnectivityManager(this);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            MyApp.LogDebug(LOG_TAG, "not connected");
            ConnectivityStateReceiver.enableReceiver(this);
            stopSelf();
            return;
        }

        AppRepository appRepository = AppRepository.Companion.getInstance(getApplicationContext());
        MyApp.meta = appRepository.readMeta(); // to load eTag

        OkHttpClient okHttpClient = getOkHttpClient();
        if (okHttpClient == null) {
            Log.e(LOG_TAG, "OkHttpClient is null.");
            return;
        }
        if (MyApp.fetcher == null) {
            fetcher = new FetchFahrplan(okHttpClient);
        } else {
            fetcher = MyApp.fetcher;
        }
        MyApp.LogDebug(LOG_TAG, "going to fetch schedule");
        FahrplanMisc.setUpdateAlarm(this, false);
        fetchFahrplan(this);
    }

    @Nullable
    private OkHttpClient getOkHttpClient() {
        AppRepository appRepository = AppRepository.Companion.getInstance(this);
        String url = appRepository.readScheduleUrl();
        String host = Uri.parse(url).getHost();
        OkHttpClient okHttpClient = null;
        try {
            okHttpClient = CustomHttpClient.createHttpClient(host);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            MyApp.LogDebug(LOG_TAG, "background update failed with " + HTTP_STATUS.HTTP_SSL_SETUP_FAILURE);
            stopSelf();
        }
        return okHttpClient;
    }

    public static void start(@NonNull Context context) {
        Intent intent = new Intent(context, UpdateService.class);
        context.startService(intent);
    }

}
