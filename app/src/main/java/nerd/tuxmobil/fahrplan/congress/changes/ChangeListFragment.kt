package nerd.tuxmobil.fahrplan.congress.changes

import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import info.metadude.android.eventfahrplan.commons.flow.observe
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment.OnSessionListClick
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolver
import nerd.tuxmobil.fahrplan.congress.commons.ScreenNavigation
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.replaceFragment
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatter
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatter

/**
 * A fragment representing a list of Items.
 *
 * Activities containing this fragment MUST implement the [OnSessionListClick] interface.
 *
 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
 * screen orientation changes).
 */
class ChangeListFragment : Fragment() {

    companion object {

        const val FRAGMENT_TAG = "changes"

        fun newInstance(sidePane: Boolean): ChangeListFragment {
            return ChangeListFragment().withArguments(BundleKeys.SIDEPANE to sidePane)
        }

        fun replaceAtBackStack(fragmentManager: FragmentManager, @IdRes containerViewId: Int, sidePane: Boolean) {
            val fragment = ChangeListFragment().withArguments(
                BundleKeys.SIDEPANE to sidePane
            )
            fragmentManager.commit {
                fragmentManager.popBackStack(FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                fragmentManager.replaceFragment(containerViewId, fragment, FRAGMENT_TAG, FRAGMENT_TAG)
            }
        }

    }

    private var onSessionListClickListener: OnSessionListClick? = null
    private val viewModelFactory by lazy {
        val resourceResolving = ResourceResolver(requireContext())
        ChangeListViewModelFactory(
            AppRepository,
            resourceResolving,
            SessionPropertiesFormatter(resourceResolving),
            ContentDescriptionFormatter(resourceResolving),
        )
    }
    private val viewModel: ChangeListViewModel by viewModels { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val arguments = requireArguments()
        val sidePane = arguments.getBoolean(BundleKeys.SIDEPANE)

        val contextThemeWrapper = ContextThemeWrapper(requireContext(), R.style.Theme_Congress)
        val localInflater = inflater.cloneInContext(contextThemeWrapper)
        val fragmentView = localInflater.inflate(R.layout.fragment_session_list, container, false)
        fragmentView.findViewById<ComposeView>(R.id.session_changes_view).apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SessionChangesScreen(
                    state = viewModel.sessionChangesState.collectAsState().value,
                    showInSidePane = sidePane,
                    onViewEvent = viewModel::onViewEvent,
                )
            }
            isClickable = true
        }
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.scheduleChangesSeen.observe(viewLifecycleOwner) {
            NotificationHelper(requireContext()).cancelScheduleUpdateNotification()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateScheduleChangesSeen(changesSeen = true)
    }

    @MainThread
    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.screenNavigation = ScreenNavigation { sessionId ->
            onSessionListClickListener?.onSessionListClick(sessionId)
        }
        if (context is OnSessionListClick) {
            onSessionListClickListener = context
        } else {
            error("$context must implement OnSessionListClick")
        }
    }

    @MainThread
    @CallSuper
    override fun onDetach() {
        super.onDetach()
        onSessionListClickListener = null
        viewModel.screenNavigation = null
    }

}
