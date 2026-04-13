package nerd.tuxmobil.fahrplan.congress.favorites

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.extensions.replaceFragment
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments
import nerd.tuxmobil.fahrplan.congress.sidepane.OnSidePaneCloseListener
import nerd.tuxmobil.fahrplan.congress.utils.ActivityHelper.navigateUp

class StarredListFragment : AbstractListFragment() {

    companion object {

        const val FRAGMENT_TAG = "starred"

        fun replaceAtBackStack(fragmentManager: FragmentManager, @IdRes containerViewId: Int, sidePane: Boolean) {
            val fragment = StarredListFragment().withArguments(
                BundleKeys.SIDEPANE to sidePane
            )
            fragmentManager.commit {
                fragmentManager.popBackStack(FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                fragmentManager.replaceFragment(containerViewId, fragment, FRAGMENT_TAG, FRAGMENT_TAG)
            }
        }

    }

    private var onSessionListClickListener: OnSessionListClick? = null
    private var sidePane = false
    private val viewModel: StarredListViewModel by viewModels { StarredListViewModelFactory(requireContext()) }

    @MainThread
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sidePane = it.getBoolean(BundleKeys.SIDEPANE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return content {
            EventFahrplanTheme {
                StarredListScreen(
                    viewModel = viewModel,
                    showInSidePane = sidePane,
                    onBack = ::navigateBack,
                    onNavigateToSession = ::navigateToSession,
                )
            }
        }
    }

    private fun navigateToSession(sessionId: String) {
        onSessionListClickListener?.onSessionListClick(sessionId)
    }

    private fun navigateBack() {
        val activity = requireActivity()
        when (val listener = activity as? OnSidePaneCloseListener) {
            null -> activity.navigateUp()
            else -> listener.onSidePaneClose(FRAGMENT_TAG)
        }
    }

    @MainThread
    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
        onSessionListClickListener = try {
            context as OnSessionListClick
        } catch (_: ClassCastException) {
            throw ClassCastException("$context must implement OnSessionListClick")
        }
    }

    @MainThread
    @CallSuper
    override fun onDetach() {
        super.onDetach()
        onSessionListClickListener = null
    }

}
