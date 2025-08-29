package nerd.tuxmobil.fahrplan.congress.schedule

import android.app.KeyguardManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.ContentLoadingProgressBar
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentManager.OnBackStackChangedListener
import androidx.lifecycle.Lifecycle.State.RESUMED
import info.metadude.android.eventfahrplan.commons.flow.observe
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.about.AboutDialog
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsActivity
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsFragment
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment.OnSessionListClick
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity
import nerd.tuxmobil.fahrplan.congress.base.OnSessionItemClickListener
import nerd.tuxmobil.fahrplan.congress.changes.ChangeListActivity
import nerd.tuxmobil.fahrplan.congress.changes.ChangeListFragment
import nerd.tuxmobil.fahrplan.congress.changes.ChangeStatistic
import nerd.tuxmobil.fahrplan.congress.changes.ChangesDialog
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsActivity
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsFragment
import nerd.tuxmobil.fahrplan.congress.engagements.initUserEngagement
import nerd.tuxmobil.fahrplan.congress.extensions.applyEdgeToEdgeInsets
import nerd.tuxmobil.fahrplan.congress.extensions.applyToolbar
import nerd.tuxmobil.fahrplan.congress.extensions.isLandscape
import nerd.tuxmobil.fahrplan.congress.extensions.withExtras
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListActivity
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListFragment
import nerd.tuxmobil.fahrplan.congress.net.CertificateErrorFragment
import nerd.tuxmobil.fahrplan.congress.net.ErrorMessage
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.reporting.TraceDroidEmailSender
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.OnSessionClickListener
import nerd.tuxmobil.fahrplan.congress.schedule.observables.LoadScheduleUiState
import nerd.tuxmobil.fahrplan.congress.search.SearchActivity
import nerd.tuxmobil.fahrplan.congress.search.SearchFragment
import nerd.tuxmobil.fahrplan.congress.settings.SettingsActivity
import nerd.tuxmobil.fahrplan.congress.sidepane.OnSidePaneCloseListener
import nerd.tuxmobil.fahrplan.congress.utils.ConfirmationDialog.OnConfirmationDialogClicked
import nerd.tuxmobil.fahrplan.congress.utils.showWhenLockedCompat

class MainActivity : BaseActivity(),
    MenuProvider,
    OnSidePaneCloseListener,
    OnSessionListClick,
    OnSessionClickListener,
    OnSessionItemClickListener,
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

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var keyguardManager: KeyguardManager
    private lateinit var errorMessageFactory: ErrorMessage.Factory
    private lateinit var progressBar: ContentLoadingProgressBar
    private var progressDialog: ProgressDialog? = null
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(AppRepository, notificationHelper)
    }

    private var isScreenLocked = false
    private var isAlarmsInSidePane = false
    private var isFavoritesInSidePane = false
    private var isSearchInSidePane = false
    private var shouldScrollToCurrent = true

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        showWhenLockedCompat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance = this
        setContentView(R.layout.main)
        addMenuProvider(this, this, RESUMED)

        notificationHelper = NotificationHelper(this)

        keyguardManager = getSystemService()!!
        errorMessageFactory = ErrorMessage.Factory(this)
        val toolbar = requireViewByIdCompat<Toolbar>(R.id.toolbar)
        applyToolbar(toolbar) {
            title = if (isLandscape()) getString(R.string.app_name) else ""
            setDisplayShowHomeEnabled(true)
            setDefaultDisplayHomeAsUpEnabled(true)
        }
        progressBar = requireViewByIdCompat(R.id.progress)

        val rootLayout = requireViewByIdCompat<View>(R.id.root_layout)
        rootLayout.applyEdgeToEdgeInsets()

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
        viewModel.checkPostNotificationsPermission()
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
        viewModel.showAbout.observe(this) {
            showAboutDialog()
        }
        viewModel.openSessionDetails.observe(this) {
            openSessionDetails()
        }
        viewModel.missingPostNotificationsPermission.observe(this) {
            Toast.makeText(this, R.string.alarms_disabled_notifications_permission_missing, Toast.LENGTH_LONG).show()
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
        if (sidePaneView != null && isAlarmsInSidePane) {
            sidePaneView.isVisible = !isScreenLocked
        }
        if (sidePaneView != null && isFavoritesInSidePane) {
            sidePaneView.isVisible = !isScreenLocked
        }
    }

    private fun showChangesDialog(scheduleVersion: String, changeStatistic: ChangeStatistic) {
        val fragment = findFragment(ChangesDialog.FRAGMENT_TAG)
        if (fragment == null) {
            ChangesDialog
                .newInstance(scheduleVersion, changeStatistic)
                .show(supportFragmentManager, ChangesDialog.FRAGMENT_TAG)
        }
    }

    private fun showAboutDialog() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.addToBackStack(null)
        AboutDialog().show(transaction, AboutDialog.FRAGMENT_TAG)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.mainmenu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_item_about -> viewModel.showAboutDialog()
            R.id.menu_item_alarms -> openAlarms()
            R.id.menu_item_settings -> SettingsActivity.startForResult(this)
            R.id.menu_item_search -> openSearch()
            R.id.menu_item_schedule_changes -> openSessionChanges()
            R.id.menu_item_favorites -> openFavorites()
            else -> return false
        }
        return true
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
        if (fragmentTag == AlarmsFragment.FRAGMENT_TAG) {
            isAlarmsInSidePane = false
        }
        if (fragmentTag == StarredListFragment.FRAGMENT_TAG) {
            isFavoritesInSidePane = false
        }
        if (fragmentTag == SearchFragment.FRAGMENT_TAG) {
            isSearchInSidePane = false
        }
        removeFragment(fragmentTag)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {

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

                    @Suppress("kotlin:S1066")
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
            isAlarmsInSidePane = hasFragment && fragment is AlarmsFragment
            isFavoritesInSidePane = hasFragment && fragment is StarredListFragment
            isSearchInSidePane = hasFragment && fragment is SearchFragment
            view.isVisible = (!isAlarmsInSidePane || !isFavoritesInSidePane || !isScreenLocked || !isSearchInSidePane) && hasFragment
        }
    }

    private fun openAlarms() {
        val sidePaneView = findViewById<FragmentContainerView>(R.id.detail)
        if (sidePaneView == null) {
            AlarmsActivity.start(this)
        } else if (!isScreenLocked) {
            sidePaneView.isVisible = true
            isAlarmsInSidePane = true
            AlarmsFragment.replaceAtBackStack(supportFragmentManager, R.id.detail, true)
        }
    }

    private fun openFavorites() {
        val sidePaneView = findViewById<FragmentContainerView>(R.id.detail)
        if (sidePaneView == null) {
            StarredListActivity.start(this)
        } else if (!isScreenLocked) {
            sidePaneView.isVisible = true
            isFavoritesInSidePane = true
            StarredListFragment.replaceAtBackStack(supportFragmentManager, R.id.detail, true)
        }
    }

    fun openSessionChanges() {
        val sidePaneView = findViewById<FragmentContainerView>(R.id.detail)
        if (sidePaneView == null) {
            ChangeListActivity.start(this)
        } else {
            sidePaneView.isVisible = true
            ChangeListFragment.replaceAtBackStack(supportFragmentManager, R.id.detail, true)
        }
    }

    private fun openSearch() {
        val sidePaneView = findViewById<FragmentContainerView>(R.id.detail)
        if (sidePaneView == null) {
            SearchActivity.start(this)
        } else {
            sidePaneView.isVisible = true
            isSearchInSidePane = true
            SearchFragment.replaceAtBackStack(supportFragmentManager, R.id.detail, true)
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

    override fun onSessionItemClick(sessionId: String) {
        viewModel.openSessionDetails(sessionId)
    }
}
