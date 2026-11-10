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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.closeSidePaneOrNavigateUp
import nerd.tuxmobil.fahrplan.congress.base.forwardSessionItemClick
import nerd.tuxmobil.fahrplan.congress.base.requireOnSessionItemClickListener
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.extensions.replaceFragment
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments

/**
 * A fragment representing a list of Items.
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val arguments = requireArguments()
        val sidePane = arguments.getBoolean(BundleKeys.SIDEPANE)

        val contextThemeWrapper = ContextThemeWrapper(requireContext(), R.style.Theme_Congress)
        val localInflater = inflater.cloneInContext(contextThemeWrapper)
        val fragmentView = localInflater.inflate(R.layout.fragment_session_list, container, false)
        fragmentView.findViewById<ComposeView>(R.id.session_changes_view).apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                EventFahrplanTheme {
                    SessionChangesScreen(
                        showInSidePane = sidePane,
                        onBack = ::navigateBack,
                        onNavigateToSession = ::forwardSessionItemClick,
                    )
                }
            }
            isClickable = true
        }
        return fragmentView
    }

    private fun navigateBack() {
        closeSidePaneOrNavigateUp(FRAGMENT_TAG)
    }

    @MainThread
    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireOnSessionItemClickListener(context)
    }

}
