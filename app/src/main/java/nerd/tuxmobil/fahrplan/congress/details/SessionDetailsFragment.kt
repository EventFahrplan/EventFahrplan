package nerd.tuxmobil.fahrplan.congress.details

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.compose.runtime.collectAsState
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
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmTimePickerFragment
import nerd.tuxmobil.fahrplan.congress.calendar.CalendarSharing
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolver
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.replaceFragment
import nerd.tuxmobil.fahrplan.congress.extensions.startActivity
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.sharing.SessionSharer
import nerd.tuxmobil.fahrplan.congress.sidepane.OnSidePaneCloseListener
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatter
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatting

class SessionDetailsFragment : Fragment(), MenuProvider {

    companion object {

        const val FRAGMENT_TAG = "detail"
        private const val SESSION_DETAILS_FRAGMENT_REQUEST_KEY = "SESSION_DETAILS_FRAGMENT_REQUEST_KEY"

        fun replaceAtBackStack(fragmentManager: FragmentManager, @IdRes containerViewId: Int, sidePane: Boolean) {
            val fragment = SessionDetailsFragment().withArguments(
                BundleKeys.SIDEPANE to sidePane
            )
            fragmentManager.commit {
                fragmentManager.popBackStack(FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                fragmentManager.replaceFragment(containerViewId, fragment, FRAGMENT_TAG, FRAGMENT_TAG)
            }
        }

        fun replace(fragmentManager: FragmentManager, @IdRes containerViewId: Int) {
            val fragment = SessionDetailsFragment()
            fragmentManager.replaceFragment(containerViewId, fragment, FRAGMENT_TAG)
        }

    }
    private lateinit var postNotificationsPermissionRequestLauncher: ActivityResultLauncher<String>
    private lateinit var scheduleExactAlarmsPermissionRequestLauncher: ActivityResultLauncher<Intent>
    private lateinit var appRepository: AppRepository
    private lateinit var alarmServices: AlarmServices
    private lateinit var notificationHelper: NotificationHelper
    private val viewModel: SessionDetailsViewModel by viewModels {
        SessionDetailsViewModelFactory(
            appRepository = appRepository,
            resourceResolving = ResourceResolver(requireContext()),
            alarmServices = alarmServices,
            notificationHelper = notificationHelper,
            defaultEngelsystemRoomName = AppRepository.ENGELSYSTEM_ROOM_NAME,
            customEngelsystemRoomName = getString(R.string.engelsystem_shifts_alias)
        )
    }
    private lateinit var model: SelectedSessionParameter
    private lateinit var contentDescriptionFormatting: ContentDescriptionFormatting
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
    override fun onAttach(context: Context) {
        super.onAttach(context)
        appRepository = AppRepository
        alarmServices = AlarmServices.newInstance(context, appRepository)
        notificationHelper = NotificationHelper(context)
        contentDescriptionFormatting = ContentDescriptionFormatter(ResourceResolver(context))
    }

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = content {
        with(viewModel) {
            @Suppress("KotlinConstantConditions")
            SessionDetailsScreen(
                sessionDetailsState = sessionDetailsState.collectAsState().value,
                showRoomState = showRoomState,
                roomStateMessage = roomStateMessage.collectAsState().value,
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
        observeViewModel()
        val activity = requireActivity()
        if (hasArguments) {
            activity.invalidateOptionsMenu()
        }
        activity.setResult(Activity.RESULT_CANCELED)
    }

    @SuppressLint("InlinedApi")
    private fun observeViewModel() {
        viewModel.selectedSessionParameter.observe(this) { model ->
            this.model = model
            updateOptionsMenu()
        }
        viewModel.openFeedBack.observe(viewLifecycleOwner) { uri ->
            requireContext().startActivity(Intent(Intent.ACTION_VIEW, uri)) {
                Toast.makeText(context, R.string.share_error_activity_not_found, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.shareSimple.observe(viewLifecycleOwner) { formattedSession ->
            SessionSharer.shareSimple(requireContext(), formattedSession)
        }
        viewModel.shareJson.observe(viewLifecycleOwner) { formattedSession ->
            val context = requireContext()
            if (!SessionSharer.shareJson(context, formattedSession)) {
                Toast.makeText(context, R.string.share_error_activity_not_found, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.addToCalendar.observe(viewLifecycleOwner) { session ->
            CalendarSharing(requireContext()).addToCalendar(session)
        }
        viewModel.showAlarmTimePicker.observe(viewLifecycleOwner) {
            showAlarmTimePicker()
        }
        viewModel.navigateToRoom.observe(viewLifecycleOwner) { uri ->
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
        viewModel.closeDetails.observe(viewLifecycleOwner) {
            val activity = requireActivity()
            if (activity is OnSidePaneCloseListener) {
                (activity as OnSidePaneCloseListener).onSidePaneClose(FRAGMENT_TAG)
            }
        }
        viewModel.requestPostNotificationsPermission.observe(viewLifecycleOwner) {
            postNotificationsPermissionRequestLauncher.launch(POST_NOTIFICATIONS)
        }
        viewModel.notificationsDisabled.observe(viewLifecycleOwner) {
            showNotificationsDisabledError()
        }
        viewModel.requestScheduleExactAlarmsPermission.observe(viewLifecycleOwner) {
            val intent = Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                .setData("package:${BuildConfig.APPLICATION_ID}".toUri())
            scheduleExactAlarmsPermissionRequestLauncher.launch(intent)
        }
    }

    private fun showAlarmTimePicker() {
        AlarmTimePickerFragment.show(this, SESSION_DETAILS_FRAGMENT_REQUEST_KEY) { requestKey, result ->
            if (requestKey == SESSION_DETAILS_FRAGMENT_REQUEST_KEY &&
                result.containsKey(AlarmTimePickerFragment.ALARM_TIMES_INDEX_BUNDLE_KEY)
            ) {
                val alarmTimesIndex = result.getInt(AlarmTimePickerFragment.ALARM_TIMES_INDEX_BUNDLE_KEY)
                viewModel.addAlarm(alarmTimesIndex)
            }
        }
    }

    private fun showMissingPostNotificationsPermissionError() {
        Toast.makeText(requireContext(), R.string.alarms_disabled_notifications_permission_missing, Toast.LENGTH_LONG).show()
    }

    private fun showNotificationsDisabledError() {
        Toast.makeText(requireContext(), R.string.alarms_disabled_notifications_are_disabled, Toast.LENGTH_LONG).show()
    }

    private fun showMissingScheduleExactAlarmsPermissionError() {
        Toast.makeText(requireContext(), R.string.alarms_disabled_schedule_exact_alarm_permission_missing, Toast.LENGTH_LONG).show()
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
