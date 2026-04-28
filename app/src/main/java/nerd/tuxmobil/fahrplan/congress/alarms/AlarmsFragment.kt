package nerd.tuxmobil.fahrplan.congress.alarms

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import nerd.tuxmobil.fahrplan.congress.base.OnSessionItemClickListener
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.extensions.replaceFragment
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments
import nerd.tuxmobil.fahrplan.congress.sidepane.OnSidePaneCloseListener
import nerd.tuxmobil.fahrplan.congress.utils.ActivityHelper.navigateUp

class AlarmsFragment : Fragment() {

    companion object {
        const val FRAGMENT_TAG = "ALARMS_FRAGMENT_TAG"

        fun replaceAtBackStack(
            fragmentManager: FragmentManager,
            @IdRes containerViewId: Int,
            sidePane: Boolean
        ) {
            val fragment = AlarmsFragment().withArguments(
                BundleKeys.SIDEPANE to sidePane
            )
            fragmentManager.commit {
                fragmentManager.popBackStack(FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                fragmentManager.replaceFragment(containerViewId, fragment, FRAGMENT_TAG, FRAGMENT_TAG)
            }
        }
    }

    private val viewModel: AlarmsViewModel by viewModels { AlarmsViewModelFactory(requireContext()) }
    private var sidePane = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        require(context is OnSessionItemClickListener) { "$context must implement OnSessionItemClickListener" }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sidePane = it.getBoolean(BundleKeys.SIDEPANE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        EventFahrplanTheme {
            AlarmsScreen(
                viewModel = viewModel,
                showInSidePane = sidePane,
                onBack = ::navigateBack,
                onNavigateToSession = ::navigateToSession,
            )
        }
    }.also { it.isClickable = true }

    private fun navigateBack() {
        val activity = requireActivity()
        when (val listener = activity as? OnSidePaneCloseListener) {
            null -> activity.navigateUp()
            else -> listener.onSidePaneClose(FRAGMENT_TAG)
        }
    }

    private fun navigateToSession(sessionId: String) {
        (requireContext() as OnSessionItemClickListener).onSessionItemClick(sessionId)
    }

}

