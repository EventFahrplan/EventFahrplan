package nerd.tuxmobil.fahrplan.congress.schedule

import android.app.KeyguardManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.OnBackStackChangedListener
import nerd.tuxmobil.fahrplan.congress.MyApp
import nerd.tuxmobil.fahrplan.congress.MyApp.TASKS
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.about.AboutDialog
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmList
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment.OnSessionListClick
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.changes.ChangeListActivity
import nerd.tuxmobil.fahrplan.congress.changes.ChangeListFragment
import nerd.tuxmobil.fahrplan.congress.changes.ChangeStatistic
import nerd.tuxmobil.fahrplan.congress.changes.ChangesDialog
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys.BUNDLE_KEY_ALTERNATIVE_HIGHLIGHTING_UPDATED
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys.BUNDLE_KEY_ENGELSYSTEM_SHIFTS_URL_UPDATED
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys.BUNDLE_KEY_SCHEDULE_URL_UPDATED
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys.BUNDLE_KEY_SESSION_ALARM_DAY_INDEX
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys.BUNDLE_KEY_SESSION_ALARM_NOTIFICATION_ID
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys.BUNDLE_KEY_SESSION_ALARM_SESSION_ID
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys.BUNDLE_KEY_USE_DEVICE_TIME_ZONE_UPDATED
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsActivity
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsFragment
import nerd.tuxmobil.fahrplan.congress.engagements.initUserEngagement
import nerd.tuxmobil.fahrplan.congress.extensions.withExtras
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListActivity
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListFragment
import nerd.tuxmobil.fahrplan.congress.net.CertificateErrorFragment
import nerd.tuxmobil.fahrplan.congress.net.ErrorMessage
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus
import nerd.tuxmobil.fahrplan.congress.net.LoadShiftsResult
import nerd.tuxmobil.fahrplan.congress.net.ParseResult
import nerd.tuxmobil.fahrplan.congress.net.ParseScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.ParseShiftsResult
import nerd.tuxmobil.fahrplan.congress.reporting.TraceDroidEmailSender
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.OnSessionClickListener
import nerd.tuxmobil.fahrplan.congress.settings.SettingsActivity
import nerd.tuxmobil.fahrplan.congress.sidepane.OnSidePaneCloseListener
import nerd.tuxmobil.fahrplan.congress.utils.ConfirmationDialog.OnConfirmationDialogClicked
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc
import nerd.tuxmobil.fahrplan.congress.utils.showWhenLockedCompat
import org.ligi.tracedroid.logging.Log

class MainActivity : BaseActivity(),
    OnSidePaneCloseListener,
    OnSessionListClick,
    OnSessionClickListener,
    OnBackStackChangedListener,
    OnConfirmationDialogClicked {

    companion object {

        private const val LOG_TAG = "MainActivity"
        private const val INVALID_NOTIFICATION_ID = -1

        lateinit var instance: MainActivity

        /**
         * Returns a unique intent to be used to launch this activity.
         * The given parameters [sessionId] and [dayIndex] are passed along as bundle extras.
         * The [sessionId] is also used to ensure this intent is unique by definition of
         * [Intent.filterEquals].
         */
        @JvmStatic
        fun createLaunchIntent(
            context: Context,
            sessionId: String,
            dayIndex: Int,
            notificationId: Int
        ) = Intent(context, MainActivity::class.java)
            .apply {
                data = "fake://$sessionId".toUri()
                flags = FLAG_ACTIVITY_CLEAR_TOP or FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            }
            .withExtras(
                BUNDLE_KEY_SESSION_ALARM_SESSION_ID to sessionId,
                BUNDLE_KEY_SESSION_ALARM_DAY_INDEX to dayIndex,
                BUNDLE_KEY_SESSION_ALARM_NOTIFICATION_ID to notificationId
            )
    }

    private lateinit var appRepository: AppRepository
    private lateinit var keyguardManager: KeyguardManager
    private lateinit var errorMessageFactory: ErrorMessage.Factory
    private lateinit var progressBar: ProgressBar
    private var progressDialog: ProgressDialog? = null

    private var isScreenLocked = false
    private var isFavoritesInSidePane = false
    private var shouldScrollToCurrent = true
    private var showUpdateAction = true

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        showWhenLockedCompat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        setContentView(R.layout.main_layout)

        appRepository = AppRepository
        keyguardManager = getSystemService()!!
        errorMessageFactory = ErrorMessage.Factory(this)
        val toolbar = requireViewByIdCompat<Toolbar>(R.id.toolbar)
        progressBar = requireViewByIdCompat(R.id.progress)

        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(R.string.fahrplan)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDefaultDisplayHomeAsUpEnabled(true)
        val actionBarColor = ContextCompat.getColor(this, R.color.colorActionBar)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(actionBarColor))

        TraceDroidEmailSender.sendStackTraces(this)
        resetProgressDialog()
        MyApp.meta = appRepository.readMeta()
        FahrplanMisc.loadDays(appRepository)

        MyApp.LogDebug(LOG_TAG, "task_running: ${MyApp.task_running}")
        when (MyApp.task_running) {
            TASKS.FETCH -> {
                MyApp.LogDebug(LOG_TAG, "fetch was pending, restart")
                showFetchingStatus()
            }
            TASKS.PARSE -> {
                MyApp.LogDebug(LOG_TAG, "parse was pending, restart")
                showParsingStatus()
            }
            TASKS.NONE -> if (MyApp.meta.numDays == 0 && savedInstanceState == null) {
                Log.d(LOG_TAG, "Fetching schedule in onCreate bc. numDays==0")
                fetchFahrplan()
            }
            else -> {
                // Nothing to do here.
            }
        }

        supportFragmentManager.addOnBackStackChangedListener(this)
        if (findViewById<View>(R.id.schedule) != null && findFragment(FahrplanFragment.FRAGMENT_TAG) == null) {
            replaceFragment(R.id.schedule, FahrplanFragment(), FahrplanFragment.FRAGMENT_TAG)
        }
        if (findViewById<View>(R.id.detail) == null) {
            removeFragment(SessionDetailsFragment.FRAGMENT_TAG)
        }
        initUserEngagement()
        onSessionAlarmNotificationTapped(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        onSessionAlarmNotificationTapped(intent)
    }

    private fun onSessionAlarmNotificationTapped(intent: Intent) {
        val notificationId = intent.getIntExtra(BUNDLE_KEY_SESSION_ALARM_NOTIFICATION_ID, INVALID_NOTIFICATION_ID)
        if (notificationId != INVALID_NOTIFICATION_ID) {
            appRepository.deleteSessionAlarmNotificationId(notificationId)
        }
    }

    private fun onGotResponse(fetchScheduleResult: FetchScheduleResult) {
        val status = fetchScheduleResult.httpStatus
        MyApp.LogDebug(LOG_TAG, "onGotResponse -> status = $status")
        MyApp.task_running = TASKS.NONE
        if (MyApp.meta.numDays == 0) {
            hideProgressDialog()
        }
        if (status != HttpStatus.HTTP_OK) {
            showErrorDialog(status, fetchScheduleResult.hostName, fetchScheduleResult.exceptionMessage)
            progressBar.isInvisible = true
            showUpdateAction = true
            invalidateOptionsMenu()
            return
        }
        progressBar.isInvisible = true
        showUpdateAction = true
        invalidateOptionsMenu()

        // Parser is automatically invoked when response has been received.
        showParsingStatus()
        MyApp.task_running = TASKS.PARSE
    }

    private fun showErrorDialog(httpStatus: HttpStatus, hostName: String, exceptionMessage: String) {
        if (httpStatus == HttpStatus.HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE) {
            CertificateErrorFragment.showDialog(supportFragmentManager, exceptionMessage)
        }
        val errorMessage = errorMessageFactory.getMessageForHttpStatus(httpStatus, hostName)
        errorMessage.show(context = this, shouldShowLong = false)
    }

    private fun onParseDone(result: ParseResult) {
        if (result is ParseScheduleResult) {
            MyApp.LogDebug(LOG_TAG, "Parsing schedule done successfully: ${result.isSuccess}, numDays: ${MyApp.meta.numDays}")
        }
        if (result is ParseShiftsResult) {
            MyApp.LogDebug(LOG_TAG, "Parsing Engelsystem shifts done successfully: ${result.isSuccess}")
        }
        MyApp.task_running = TASKS.NONE
        if (MyApp.meta.numDays == 0) {
            hideProgressDialog()
        }
        progressBar.isInvisible = true
        showUpdateAction = true
        invalidateOptionsMenu()
        findFragment(FahrplanFragment.FRAGMENT_TAG)?.let {
            (it as FahrplanFragment).onParseDone(result)
        }
        if (!appRepository.readScheduleChangesSeen()) {
            showChangesDialog()
        }
    }

    private fun onLoadShiftsDone(result: LoadShiftsResult) {
        findFragment(FahrplanFragment.FRAGMENT_TAG)?.let {
            (it as FahrplanFragment).onParseDone(ParseShiftsResult.of(result))
        }
    }

    private fun showFetchingStatus() {
        if (MyApp.meta.numDays == 0) {
            // Initial load
            MyApp.LogDebug(LOG_TAG, "fetchFahrplan with numDays == 0")
            showProgressDialog(R.string.progress_loading_data)
        } else {
            MyApp.LogDebug(LOG_TAG, "Show fetch status")
            progressBar.isInvisible = false
            showUpdateAction = false
            invalidateOptionsMenu()
        }
    }

    private fun showParsingStatus() {
        if (MyApp.meta.numDays == 0) {
            // Initial load
            showProgressDialog(R.string.progress_processing_data)
        } else {
            MyApp.LogDebug(LOG_TAG, "Show parse status")
            progressBar.isInvisible = false
            showUpdateAction = false
            invalidateOptionsMenu()
        }
    }

    fun fetchFahrplan() {
        if (MyApp.task_running == TASKS.NONE) {
            MyApp.task_running = TASKS.FETCH
            showFetchingStatus()
            val url = appRepository.readScheduleUrl()
            appRepository.loadSchedule(
                url = url,
                onFetchingDone = ::onGotResponse,
                onParsingDone = ::onParseDone,
                onLoadingShiftsDone = ::onLoadShiftsDone
            )
        } else {
            Log.d(LOG_TAG, "Fetching schedule already in progress.")
        }
    }

    override fun onDestroy() {
        appRepository.cancelLoading()
        hideProgressDialog()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        isScreenLocked = keyguardManager.isKeyguardLocked
        val sidePaneView = findViewById<FragmentContainerView>(R.id.detail)
        if (sidePaneView != null && isFavoritesInSidePane) {
            sidePaneView.isVisible = !isScreenLocked
        }
        if (!appRepository.readScheduleChangesSeen()) {
            showChangesDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.mainmenu, menu)
        return true
    }

    private fun showChangesDialog() {
        val fragment = findFragment(ChangesDialog.FRAGMENT_TAG)
        if (fragment == null) {
            val sessions = appRepository.loadChangedSessions()
            val meta = appRepository.readMeta()
            val statistic = ChangeStatistic.of(sessions)
            ChangesDialog
                .newInstance(meta.version, statistic)
                .show(supportFragmentManager, ChangesDialog.FRAGMENT_TAG)
        }
    }

    private fun showAboutDialog() {
        val meta = appRepository.readMeta()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.addToBackStack(null)
        AboutDialog
            .newInstance(meta.version, meta.subtitle, meta.title)
            .show(transaction, AboutDialog.FRAGMENT_TAG)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val functionByOptionItemId = mapOf(
            R.id.menu_item_about to { showAboutDialog() },
            R.id.menu_item_alarms to { AlarmList.startForResult(this) },
            R.id.menu_item_settings to { SettingsActivity.startForResult(this) },
            R.id.menu_item_schedule_changes to { openSessionChanges() },
            R.id.menu_item_favorites to { openFavorites() }
        )
        return when (val function = functionByOptionItemId[item.itemId]) {
            null -> {
                super.onOptionsItemSelected(item)
            }
            else -> {
                function.invoke()
                true
            }
        }
    }

    private fun openSessionDetails(sessionId: String) {
        if (appRepository.updateSelectedSessionId(sessionId)) {
            val sidePaneView = findViewById<FragmentContainerView>(R.id.detail)
            if (sidePaneView == null) {
                SessionDetailsActivity.startForResult(this)
            } else {
                SessionDetailsFragment.replaceAtBackStack(supportFragmentManager, R.id.detail, true)
            }
        }
    }

    override fun onSidePaneClose(fragmentTag: String) {
        findViewById<View>(R.id.detail)?.let {
            it.isVisible = false
        }
        if (fragmentTag == StarredListFragment.FRAGMENT_TAG) {
            isFavoritesInSidePane = false
        }
        removeFragment(fragmentTag)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {

            AlarmList.REQUEST_CODE,
            SessionDetailsActivity.REQUEST_CODE ->
                if (resultCode == RESULT_CANCELED) {
                    shouldScrollToCurrent = false
                }

            SettingsActivity.REQUEST_CODE ->
                if (resultCode == RESULT_OK && intent != null) {
                    val isAlternativeHighlightingUpdated = intent.getBooleanExtra(
                        BUNDLE_KEY_ALTERNATIVE_HIGHLIGHTING_UPDATED, false)
                    val isUseDeviceTimeZoneUpdated = intent.getBooleanExtra(
                        BUNDLE_KEY_USE_DEVICE_TIME_ZONE_UPDATED, false)

                    if (isAlternativeHighlightingUpdated || isUseDeviceTimeZoneUpdated) {
                        if (findViewById<View>(R.id.schedule) != null && findFragment(FahrplanFragment.FRAGMENT_TAG) == null) {
                            replaceFragment(R.id.schedule, FahrplanFragment(), FahrplanFragment.FRAGMENT_TAG)
                        }
                    }
                    var shouldFetchFahrplan = false
                    val isScheduleUrlUpdated = resources.getBoolean(
                        R.bool.bundle_key_schedule_url_updated_default_value)
                    if (intent.getBooleanExtra(BUNDLE_KEY_SCHEDULE_URL_UPDATED, isScheduleUrlUpdated)) {
                        shouldFetchFahrplan = true
                    }
                    val isEngelsystemShiftsUrlUpdated = resources.getBoolean(
                        R.bool.bundle_key_engelsystem_shifts_url_updated_default_value)
                    if (intent.getBooleanExtra(BUNDLE_KEY_ENGELSYSTEM_SHIFTS_URL_UPDATED, isEngelsystemShiftsUrlUpdated)) {
                        shouldFetchFahrplan = true
                    }
                    if (shouldFetchFahrplan) {
                        fetchFahrplan()
                    }
                }
        }
    }

    override fun onSessionListClick(sessionId: String) {
        openSessionDetails(sessionId)
    }

    override fun onSessionClick(sessionId: String) {
        openSessionDetails(sessionId)
    }

    override fun onBackStackChanged() {
        toggleSidePaneViewVisibility(supportFragmentManager, R.id.detail)
        invalidateOptionsMenu()
    }

    private fun showProgressDialog(@StringRes message: Int) {
        progressDialog = ProgressDialog.show(this, "", resources.getString(message), true)
    }

    private fun hideProgressDialog() {
        progressDialog?.let {
            it.dismiss()
            resetProgressDialog()
        }
    }

    private fun resetProgressDialog() {
        progressDialog = null
    }

    private fun toggleSidePaneViewVisibility(fragmentManager: FragmentManager, @IdRes detailView: Int) {
        val fragment = fragmentManager.findFragmentById(detailView)
        val hasFragment = fragment != null
        findViewById<View>(detailView)?.let { view ->
            isFavoritesInSidePane = hasFragment && fragment is StarredListFragment
            view.isVisible = !(isFavoritesInSidePane && isScreenLocked || !hasFragment)
        }
    }

    private fun openFavorites() {
        val sidePaneView = findViewById<FragmentContainerView>(R.id.detail)
        if (sidePaneView == null) {
            StarredListActivity.start(this)
        } else if (!isScreenLocked) {
            sidePaneView.isVisible = true
            isFavoritesInSidePane = true
            StarredListFragment.replace(supportFragmentManager, R.id.detail, true)
        }
    }

    fun openSessionChanges() {
        val sidePaneView = findViewById<FragmentContainerView>(R.id.detail)
        if (sidePaneView == null) {
            ChangeListActivity.start(this)
        } else {
            sidePaneView.isVisible = true
            ChangeListFragment.replace(supportFragmentManager, R.id.detail, true)
        }
    }

    override fun onAccepted(requestCode: Int) {
        if (requestCode == StarredListFragment.DELETE_ALL_FAVORITES_REQUEST_CODE) {
            findFragment(StarredListFragment.FRAGMENT_TAG)?.let {
                (it as StarredListFragment).deleteAllFavorites()
            }
        }
    }

    fun shouldScheduleScrollToCurrentTimeSlot(onShouldScrollToCurrent: () -> Unit) {
        if (shouldScrollToCurrent) {
            onShouldScrollToCurrent()
        }
        shouldScrollToCurrent = true
    }

}
