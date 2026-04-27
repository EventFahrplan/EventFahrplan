package nerd.tuxmobil.fahrplan.congress.details

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsViewEvent.OnAddAlarmWithChecks
import nerd.tuxmobil.fahrplan.congress.extensions.replaceFragment
import nerd.tuxmobil.fahrplan.congress.extensions.showToast
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments
import nerd.tuxmobil.fahrplan.congress.sidepane.OnSidePaneCloseListener
import nerd.tuxmobil.fahrplan.congress.utils.ActivityHelper.navigateUp

class SessionDetailsFragment : Fragment() {

    companion object {

        const val FRAGMENT_TAG = "detail"

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
    private val viewModel by viewModels<SessionDetailsViewModel> { SessionDetailsViewModelFactory(requireContext()) }
    private var sidePane = false

    @MainThread
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        postNotificationsPermissionRequestLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.onViewEvent(OnAddAlarmWithChecks)
            } else {
                showMissingPostNotificationsPermissionError()
            }
        }

        scheduleExactAlarmsPermissionRequestLauncher =
            registerForActivityResult(StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    // User granted the permission earlier.
                    viewModel.onViewEvent(OnAddAlarmWithChecks)
                } else {
                    // User granted the permission for the first time.
                    // Screen is resumed with RESULT_CANCELED, no indication
                    // of whether the permission was granted or not.
                    // Hence the following ugly view model bypass.
                    if (viewModel.canAddAlarms()) {
                        viewModel.onViewEvent(OnAddAlarmWithChecks)
                    } else {
                        showMissingScheduleExactAlarmsPermissionError()
                    }
                }
            }
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
                showInSidePane = sidePane,
                onBack = ::navigateBack,
                onRequestPostNotificationsPermission = {
                    postNotificationsPermissionRequestLauncher.launch(POST_NOTIFICATIONS)
                },
                onRequestScheduleExactAlarmsPermission = {
                    val intent = Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        .setData("package:${BuildConfig.APPLICATION_ID}".toUri())
                    scheduleExactAlarmsPermissionRequestLauncher.launch(intent)
                },
            )
        }
    }.also { it.isClickable = true }

    override fun setArguments(args: Bundle?) {
        super.setArguments(args)
        if (args != null) {
            sidePane = args.getBoolean(BundleKeys.SIDEPANE, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().setResult(Activity.RESULT_CANCELED)
    }

    private fun navigateBack() {
        val activity = requireActivity()
        when (val listener = activity as? OnSidePaneCloseListener) {
            null -> activity.navigateUp()
            else -> listener.onSidePaneClose(FRAGMENT_TAG)
        }
    }

    private fun showMissingPostNotificationsPermissionError() {
        requireContext().showToast(R.string.alarms_disabled_notifications_permission_missing, showShort = false)
    }

    private fun showMissingScheduleExactAlarmsPermissionError() {
        requireContext().showToast(R.string.alarms_disabled_schedule_exact_alarm_permission_missing, showShort = false)
    }

}
