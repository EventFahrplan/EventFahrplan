package nerd.tuxmobil.fahrplan.congress.autoupdate

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
import androidx.core.app.SafeJobIntentService
import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.MyApp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.net.ConnectivityObserver
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus
import nerd.tuxmobil.fahrplan.congress.net.LoadShiftsResult
import nerd.tuxmobil.fahrplan.congress.net.ParseResult
import nerd.tuxmobil.fahrplan.congress.net.ParseScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.ParseShiftsResult
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.schedule.MainActivity
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc
import nerd.tuxmobil.fahrplan.congress.utils.PendingIntentCompat.FLAG_IMMUTABLE
import java.util.concurrent.CountDownLatch

class UpdateService : SafeJobIntentService() {

    companion object {

        private const val JOB_ID = 2119
        private const val LOG_TAG = "UpdateService"

        fun start(context: Context) {
            enqueueWork(context, UpdateService::class.java, JOB_ID, Intent())
        }
    }

    private val appRepository = AppRepository
    private val logging = Logging.get()
    private var workLatch: CountDownLatch? = null

    private fun onParseDone(result: ParseResult) {
        val numDays = appRepository.readMeta().numDays
        logging.d(LOG_TAG, "onParseDone -> isSuccess=${result.isSuccess}, numDays=$numDays")
        MyApp.taskRunning = MyApp.TASKS.NONE
        val changesList = appRepository.loadChangedSessions()
        if (changesList.isNotEmpty() && result is ParseScheduleResult) {
            showScheduleUpdateNotification(result.version, changesList.size)
        }
        finishWork()
    }

    private fun showScheduleUpdateNotification(version: String, changesCount: Int) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.flags = FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        val contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, FLAG_ONE_SHOT or FLAG_IMMUTABLE)

        val contentText = if (version.isEmpty()) {
            getString(R.string.schedule_updated)
        } else {
            getString(R.string.schedule_updated_to, version)
        }

        val soundUri = appRepository.readAlarmToneUri()
        val notificationHelper = NotificationHelper(this)
        val builder = notificationHelper.getScheduleUpdateNotificationBuilder(
            contentIntent,
            contentText,
            changesCount,
            soundUri
        )
        notificationHelper.notify(NotificationHelper.SCHEDULE_UPDATE_ID, builder)
    }

    private fun onGotResponse(fetchScheduleResult: FetchScheduleResult) {
        val status = fetchScheduleResult.httpStatus
        MyApp.taskRunning = MyApp.TASKS.NONE
        if (status !== HttpStatus.HTTP_OK) {
            finishWork()
            return
        }
        // Parser is automatically invoked when response has been received.
        MyApp.taskRunning = MyApp.TASKS.PARSE
    }

    private fun onLoadShiftsDone(result: LoadShiftsResult) {
        onParseDone(ParseShiftsResult.of(result))
    }

    private fun fetchFahrplan() {
        if (MyApp.taskRunning == MyApp.TASKS.NONE) {
            MyApp.taskRunning = MyApp.TASKS.FETCH

            val url = appRepository.readScheduleUrl()

            appRepository.loadSchedule(
                url = url,
                isUserRequest = false,
                onFetchingDone = ::onGotResponse,
                onParsingDone = ::onParseDone,
                onLoadingShiftsDone = ::onLoadShiftsDone
            )
        } else {
            logging.d(LOG_TAG, "Fetching already in progress.")
        }
    }

    override fun onHandleWork(intent: Intent) {
        workLatch = CountDownLatch(1)
        val connectivityObserver = ConnectivityObserver(
            context = this,
            onConnectionAvailable = {
                logging.d(LOG_TAG, "Network is available")
                fetchSchedule()
            },
            onConnectionLost = {
                logging.d(LOG_TAG, "Network is not available")
                finishWork()
            },
            shouldStopAfterFirstResponse = true
        )
        connectivityObserver.start()
        try {
            workLatch!!.await()
        } catch (e: InterruptedException) {
            logging.report(LOG_TAG, "${e.message}")
        }
    }

    private fun finishWork() {
        workLatch!!.countDown()
    }

    private fun fetchSchedule() {
        FahrplanMisc.setUpdateAlarm(this, isInitial = false, logging)
        fetchFahrplan()
    }

}
