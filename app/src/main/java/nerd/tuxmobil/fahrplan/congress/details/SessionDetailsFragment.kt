package nerd.tuxmobil.fahrplan.congress.details

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.Lifecycle.State.RESUMED
import info.metadude.android.eventfahrplan.commons.flow.observe
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmTimePickerFragment
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.extensions.replaceFragment
import nerd.tuxmobil.fahrplan.congress.extensions.showToast
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments
import nerd.tuxmobil.fahrplan.congress.sidepane.OnSidePaneCloseListener

class SessionDetailsFragment : Fragment(), MenuProvider {

    companion object {

        const val FRAGMENT_TAG = "detail"
        private const val SESSION_DETAILS_FRAGMENT_REQUEST_KEY = "SESSION_DETAILS_FRAGMENT_REQUEST_KEY"

        fun newInstance(sidePane: Boolean): SessionDetailsFragment {
            return SessionDetailsFragment().withArguments(BundleKeys.SIDEPANE to sidePane)
        }

        fun replaceAtBackStack(fragmentManager: FragmentManager, @IdRes containerViewId: Int, sidePane: Boolean) {
            val fragment = SessionDetailsFragment().withArguments(
                BundleKeys.SIDEPANE to sidePane
            )
            fragmentManager.commit {
                fragmentManager.popBackStack(FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                fragmentManager.replaceFragment(containerViewId, fragment, FRAGMENT_TAG, FRAGMENT_TAG)
            }
        }

    }

    private lateinit var postNotificationsPermissionRequestLauncher: ActivityResultLauncher<String>
    private lateinit var scheduleExactAlarmsPermissionRequestLauncher: ActivityResultLauncher<Intent>
    private val viewModel: SessionDetailsViewModel by viewModels {
        SessionDetailsViewModelFactory(requireContext())
    }
    private lateinit var model: SelectedSessionParameter
    private var sidePane = false
    private var hasArguments = false

    private val viewModelFunctionByMenuItemId = mapOf<Int, SessionDetailsViewModel.() -> Unit>(
        R.id.menu_item_feedback to { openFeedback() },
        R.id.menu_item_share_session to { share() },
        R.id.menu_item_share_session_text to { share() },
        R.id.menu_item_share_session_json to { shareToChaosflix() },
        R.id.menu_item_add_to_calendar to { addToCalendar() },
        R.id.menu_item_flag_as_favorite to { favorSession() },
        R.id.menu_item_unflag_as_favorite to { unfavorSession() },
        R.id.menu_item_set_alarm to { addAlarmWithChecks() },
        R.id.menu_item_delete_alarm to { deleteAlarm() },
        R.id.menu_item_close_session_details to { closeDetails() },
        R.id.menu_item_navigate to { navigateToRoom() },
    )

    @MainThread
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        postNotificationsPermissionRequestLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.addAlarmWithChecks()
            } else {
                showMissingPostNotificationsPermissionError()
            }
        }

        scheduleExactAlarmsPermissionRequestLauncher =
            registerForActivityResult(StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // User granted the permission earlier.
                    viewModel.addAlarmWithChecks()
                } else {
                    // User granted the permission for the first time.
                    // Screen is resumed with RESULT_CANCELED, no indication
                    // of whether the permission was granted or not.
                    // Hence the following ugly view model bypass.
                    if (viewModel.canAddAlarms()) {
                        viewModel.addAlarmWithChecks()
                    } else {
                        showMissingScheduleExactAlarmsPermissionError()
                    }
                }
            }

        requireActivity().addMenuProvider(this, this, RESUMED)
    }

    @SuppressLint("InlinedApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = content {
        EventFahrplanTheme {
            SessionDetailsScreen(
                viewModel = viewModel,
                onBack = {
                    (requireActivity() as? OnSidePaneCloseListener)?.onSidePaneClose(FRAGMENT_TAG)
                },
                onRequestPostNotificationsPermission = {
                    postNotificationsPermissionRequestLauncher.launch(POST_NOTIFICATIONS)
                },
                onRequestScheduleExactAlarmsPermission = {
                    val intent = Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        .setData("package:${BuildConfig.APPLICATION_ID}".toUri())
                    scheduleExactAlarmsPermissionRequestLauncher.launch(intent)
                },
                onShowAlarmTimePicker = ::showAlarmTimePicker,
            )
        }
    }.also { it.isClickable = true }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        if (args != null) {
            sidePane = args.getBoolean(BundleKeys.SIDEPANE, false)
            hasArguments = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeSelectedSession()
        val activity = requireActivity()
        if (hasArguments) {
            activity.invalidateOptionsMenu()
        }
        activity.setResult(Activity.RESULT_CANCELED)
    }

    private fun observeSelectedSession() {
        viewModel.selectedSessionParameter.observe(this) { model ->
            this.model = model
            updateOptionsMenu()
        }
    }

    private fun showAlarmTimePicker() {
        AlarmTimePickerFragment.show(this, SESSION_DETAILS_FRAGMENT_REQUEST_KEY) { requestKey, result ->
            if (requestKey == SESSION_DETAILS_FRAGMENT_REQUEST_KEY &&
                result.containsKey(AlarmTimePickerFragment.ALARM_TIME_BUNDLE_KEY)
            ) {
                val alarmTime = result.getInt(AlarmTimePickerFragment.ALARM_TIME_BUNDLE_KEY)
                viewModel.addAlarm(alarmTime)
            }
        }
    }

    private fun showMissingPostNotificationsPermissionError() {
        requireContext().showToast(R.string.alarms_disabled_notifications_permission_missing, showShort = false)
    }

    private fun showMissingScheduleExactAlarmsPermissionError() {
        requireContext().showToast(R.string.alarms_disabled_schedule_exact_alarm_permission_missing, showShort = false)
    }

    private fun updateOptionsMenu() {
        requireActivity().invalidateOptionsMenu()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        if (!::model.isInitialized) {
            // Skip if lifecycle is faster than ViewModel.
            return
        }
        menuInflater.inflate(R.menu.detailmenu, menu)
        if (model.isFlaggedAsFavorite) {
            menu.setMenuItemVisibility(R.id.menu_item_flag_as_favorite, false)
            menu.setMenuItemVisibility(R.id.menu_item_unflag_as_favorite, true)
        }
        if (model.hasAlarm) {
            menu.setMenuItemVisibility(R.id.menu_item_set_alarm, false)
            menu.setMenuItemVisibility(R.id.menu_item_delete_alarm, true)
        }
        menu.setMenuItemVisibility(R.id.menu_item_feedback, model.supportsFeedback)
        if (sidePane) {
            menu.setMenuItemVisibility(R.id.menu_item_close_session_details, true)
        }
        menu.setMenuItemVisibility(R.id.menu_item_navigate, model.supportsIndoorNavigation)
        @Suppress("ConstantConditionIf")
        val item = if (BuildConfig.ENABLE_CHAOSFLIX_EXPORT) {
            menu.findItem(R.id.menu_item_share_session_menu)
        } else {
            menu.findItem(R.id.menu_item_share_session)
        }
        item?.let { it.isVisible = true }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (val menuFunction = viewModelFunctionByMenuItemId[menuItem.itemId]) {
            null -> return false
            else -> menuFunction(viewModel)
        }
        return true
    }

    private fun Menu.setMenuItemVisibility(itemId: Int, isVisible: Boolean) {
        findItem(itemId)?.let { it.isVisible = isVisible }
    }

}
