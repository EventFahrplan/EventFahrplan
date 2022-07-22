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
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.OnBackStackChangedListener
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.about.AboutDialog
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmList
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment.OnSessionListClick
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.changes.ChangeListActivity
import nerd.tuxmobil.fahrplan.congress.changes.ChangeListFragment
import nerd.tuxmobil.fahrplan.congress.changes.ChangeStatistic
import nerd.tuxmobil.fahrplan.congress.changes.ChangesDialog
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsActivity
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsFragment
import nerd.tuxmobil.fahrplan.congress.engagements.initUserEngagement
import nerd.tuxmobil.fahrplan.congress.extensions.withExtras
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListActivity
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListFragment
import nerd.tuxmobil.fahrplan.congress.models.Meta
import nerd.tuxmobil.fahrplan.congress.net.CertificateErrorFragment
import nerd.tuxmobil.fahrplan.congress.net.ErrorMessage
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus
import nerd.tuxmobil.fahrplan.congress.reporting.TraceDroidEmailSender
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.OnSessionClickListener
import nerd.tuxmobil.fahrplan.congress.schedule.observables.LoadScheduleUiState
import nerd.tuxmobil.fahrplan.congress.settings.SettingsActivity
import nerd.tuxmobil.fahrplan.congress.sidepane.OnSidePaneCloseListener
import nerd.tuxmobil.fahrplan.congress.utils.ConfirmationDialog.OnConfirmationDialogClicked
import nerd.tuxmobil.fahrplan.congress.utils.showWhenLockedCompat

class MainActivity : BaseActivity(),
    OnSidePaneCloseListener,
    OnSessionListClick,
    OnSessionClickListener,
    OnBackStackChangedListener,
    OnConfirmationDialogClicked {

    companion object {

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
                BundleKeys.SESSION_ALARM_SESSION_ID to sessionId,
                BundleKeys.SESSION_ALARM_DAY_INDEX to dayIndex,
                BundleKeys.SESSION_ALARM_NOTIFICATION_ID to notificationId
            )
    }

    private lateinit var keyguardManager: KeyguardManager
    private lateinit var errorMessageFactory: ErrorMessage.Factory
    private lateinit var progressBar: ContentLoadingProgressBar
    private var progressDialog: ProgressDialog? = null
    private val viewModel: MainViewModel by viewModels { MainViewModelFactory(AppRepository) }

    private var isScreenLocked = false
    private var isFavoritesInSidePane = false
    private var shouldScrollToCurrent = true

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        showWhenLockedCompat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        setContentView(R.layout.main_layout)

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

        supportFragmentManager.addOnBackStackChangedListener(this)
        if (findViewById<View>(R.id.schedule) != null && findFragment(FahrplanFragment.FRAGMENT_TAG) == null) {
            replaceFragment(R.id.schedule, FahrplanFragment(), FahrplanFragment.FRAGMENT_TAG)
        }
        if (findViewById<View>(R.id.detail) == null) {
            removeFragment(SessionDetailsFragment.FRAGMENT_TAG)
        }
        initUserEngagement()
        observeViewModel()
        onSessionAlarmNotificationTapped(intent)
    }

    private fun observeViewModel() {
        viewModel.loadScheduleUiState.observe(this) {
            updateUi(it)
        }
        viewModel.fetchFailure.observe(this) {
            it?.let {
                showErrorDialog(it.httpStatus, it.hostName, it.exceptionMessage)
            }
        }
        viewModel.parseFailure.observe(this) {
            it?.let {
                val errorMessage = errorMessageFactory.getMessageForParsingResult(it)
                errorMessage.show(this, shouldShowLong = true)
            }
        }
        viewModel.scheduleChangesParameter.observe(this) { (scheduleVersion, changeStatistic) ->
            showChangesDialog(scheduleVersion, changeStatistic)
        }
        viewModel.showAbout.observe(this) { meta ->
            showAboutDialog(meta)
        }
        viewModel.openSessionDetails.observe(this) {
            openSessionDetails()
        }
    }

    private fun updateUi(uiState: LoadScheduleUiState) {
        if (uiState is LoadScheduleUiState.Initializing) {
            showProgressDialog(uiState.progressInfo)
        } else {
            hideProgressDialog()
        }
        if (uiState is LoadScheduleUiState.Active) {
            progressBar.show()
        } else {
            progressBar.hide()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        onSessionAlarmNotificationTapped(intent)
    }

    private fun onSessionAlarmNotificationTapped(intent: Intent) {
        val notificationId = intent.getIntExtra(BundleKeys.SESSION_ALARM_NOTIFICATION_ID, INVALID_NOTIFICATION_ID)
        if (notificationId != INVALID_NOTIFICATION_ID) {
            viewModel.deleteSessionAlarmNotificationId(notificationId)
        }
    }

    private fun showErrorDialog(httpStatus: HttpStatus, hostName: String, exceptionMessage: String) {
        if (httpStatus == HttpStatus.HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE) {
            CertificateErrorFragment.showDialog(supportFragmentManager, exceptionMessage)
        } else {
            val errorMessage = errorMessageFactory.getMessageForHttpStatus(httpStatus, hostName)
            errorMessage.show(context = this, shouldShowLong = false)
        }
    }

    override fun onDestroy() {
        viewModel.cancelLoading()
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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.mainmenu, menu)
        return true
    }

    private fun showChangesDialog(scheduleVersion: String, changeStatistic: ChangeStatistic) {
        val fragment = findFragment(ChangesDialog.FRAGMENT_TAG)
        if (fragment == null) {
            ChangesDialog
                .newInstance(scheduleVersion, changeStatistic)
                .show(supportFragmentManager, ChangesDialog.FRAGMENT_TAG)
        }
    }

    private fun showAboutDialog(meta: Meta) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.addToBackStack(null)
        AboutDialog
            .newInstance(meta.version, meta.subtitle, meta.title)
            .show(transaction, AboutDialog.FRAGMENT_TAG)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val functionByOptionItemId = mapOf(
            R.id.menu_item_about to { viewModel.showAboutDialog() },
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

    private fun openSessionDetails() {
        val sidePaneView = findViewById<FragmentContainerView>(R.id.detail)
        if (sidePaneView == null) {
            SessionDetailsActivity.startForResult(this)
        } else {
            SessionDetailsFragment.replaceAtBackStack(supportFragmentManager, R.id.detail, true)
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
                        BundleKeys.ALTERNATIVE_HIGHLIGHTING_UPDATED, false)
                    val isUseDeviceTimeZoneUpdated = intent.getBooleanExtra(
                        BundleKeys.USE_DEVICE_TIME_ZONE_UPDATED, false)

                    if (isAlternativeHighlightingUpdated || isUseDeviceTimeZoneUpdated) {
                        if (findViewById<View>(R.id.schedule) != null && findFragment(FahrplanFragment.FRAGMENT_TAG) != null) {
                            replaceFragment(R.id.schedule, FahrplanFragment(), FahrplanFragment.FRAGMENT_TAG)
                        }
                    }
                    var shouldFetchFahrplan = false
                    val isScheduleUrlUpdated = resources.getBoolean(
                        R.bool.bundle_key_schedule_url_updated_default_value)
                    if (intent.getBooleanExtra(BundleKeys.SCHEDULE_URL_UPDATED, isScheduleUrlUpdated)) {
                        shouldFetchFahrplan = true
                    }
                    val isEngelsystemShiftsUrlUpdated = resources.getBoolean(
                        R.bool.bundle_key_engelsystem_shifts_url_updated_default_value)
                    if (intent.getBooleanExtra(BundleKeys.ENGELSYSTEM_SHIFTS_URL_UPDATED, isEngelsystemShiftsUrlUpdated)) {
                        shouldFetchFahrplan = true
                    }
                    if (shouldFetchFahrplan) {
                        // TODO Handle schedule update in AppRepository; above code becomes needless
                        viewModel.requestScheduleUpdate(isUserRequest = true)
                    }
                }
        }
    }

    override fun onSessionListClick(sessionId: String) {
        viewModel.openSessionDetails(sessionId)
    }

    override fun onSessionClick(sessionId: String) {
        viewModel.openSessionDetails(sessionId)
    }

    override fun onBackStackChanged() {
        toggleSidePaneViewVisibility(supportFragmentManager, R.id.detail)
        invalidateOptionsMenu()
    }

    private fun showProgressDialog(@StringRes message: Int) {
        hideProgressDialog()
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
            view.isVisible = (!isFavoritesInSidePane || !isScreenLocked) && hasFragment
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
