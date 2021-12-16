package nerd.tuxmobil.fahrplan.congress.autoupdate;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.SafeJobIntentService;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import info.metadude.android.eventfahrplan.commons.logging.Logging;
import kotlin.Unit;
import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.MyApp.TASKS;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.models.Session;
import nerd.tuxmobil.fahrplan.congress.net.ConnectivityObserver;
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult;
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus;
import nerd.tuxmobil.fahrplan.congress.net.LoadShiftsResult;
import nerd.tuxmobil.fahrplan.congress.net.ParseResult;
import nerd.tuxmobil.fahrplan.congress.net.ParseScheduleResult;
import nerd.tuxmobil.fahrplan.congress.net.ParseShiftsResult;
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;
import nerd.tuxmobil.fahrplan.congress.schedule.MainActivity;
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc;

public class UpdateService extends SafeJobIntentService {

    private static final int JOB_ID = 2119;

    private static final String LOG_TAG = "UpdateService";

    @SuppressWarnings("squid:S1170")
    private final AppRepository appRepository = AppRepository.INSTANCE;
    @NonNull
    private final Logging logging = Logging.get();
    private CountDownLatch workLatch;

    public void onParseDone(@NonNull ParseResult result) {
        int numDays = appRepository.readMeta().getNumDays();
        logging.d(LOG_TAG, "onParseDone -> isSuccess=" + result.isSuccess() + ", numDays=" + numDays);
        MyApp.task_running = TASKS.NONE;
        List<Session> changesList = appRepository.loadChangedSessions();
        if (!changesList.isEmpty() && result instanceof ParseScheduleResult) {
            showScheduleUpdateNotification(((ParseScheduleResult) result).getVersion(), changesList.size());
        }
        finishWork();
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

        Uri soundUri = appRepository.readAlarmToneUri();

        NotificationHelper notificationHelper = new NotificationHelper(this);
        NotificationCompat.Builder builder = notificationHelper.getScheduleUpdateNotificationBuilder(contentIntent, contentText, changesCount, soundUri);
        notificationHelper.notify(NotificationHelper.SCHEDULE_UPDATE_ID, builder);
    }

    public void onGotResponse(@NonNull FetchScheduleResult fetchScheduleResult) {
        HttpStatus status = fetchScheduleResult.getHttpStatus();
        MyApp.task_running = TASKS.NONE;
        if (status != HttpStatus.HTTP_OK) {
            finishWork();
            return;
        }

        // Parser is automatically invoked when response has been received.
        MyApp.task_running = TASKS.PARSE;
    }

    private void onLoadShiftsDone(@NonNull LoadShiftsResult result) {
        onParseDone(ParseShiftsResult.of(result));
    }

    private void fetchFahrplan() {
        if (MyApp.task_running == TASKS.NONE) {
            MyApp.task_running = TASKS.FETCH;
            String url = appRepository.readScheduleUrl();
            appRepository.loadSchedule(url,
                    false,
                    fetchScheduleResult -> {
                        onGotResponse(fetchScheduleResult);
                        return Unit.INSTANCE;
                    },
                    parseScheduleResult -> {
                        onParseDone(parseScheduleResult);
                        return Unit.INSTANCE;
                    },
                    loadShiftsResult -> {
                        onLoadShiftsDone(loadShiftsResult);
                        return Unit.INSTANCE;
                    });
        } else {
            logging.d(LOG_TAG, "Fetching already in progress.");
        }
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        workLatch = new CountDownLatch(1);

        ConnectivityObserver connectivityObserver = new ConnectivityObserver(this, () -> {
            logging.d(LOG_TAG, "Network is available");
            fetchSchedule();
            return Unit.INSTANCE;
        }, () -> {
            logging.d(LOG_TAG, "Network is not available");
            finishWork();
            return Unit.INSTANCE;
        }, true);
        connectivityObserver.start();

        try {
            workLatch.await();
        } catch (InterruptedException e) {
            logging.report(LOG_TAG, "" + e.getMessage());
        }
    }

    private void finishWork() {
        workLatch.countDown();
    }

    private void fetchSchedule() {
        FahrplanMisc.setUpdateAlarm(this, false);
        fetchFahrplan();
    }

    public static void start(@NonNull Context context) {
        UpdateService.enqueueWork(context, UpdateService.class, JOB_ID, new Intent());
    }

}
