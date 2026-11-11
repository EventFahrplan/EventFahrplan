package nerd.tuxmobil.fahrplan.congress.favorites

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import nerd.tuxmobil.fahrplan.congress.base.closeSidePaneOrNavigateUp
import nerd.tuxmobil.fahrplan.congress.base.forwardSessionItemClick
import nerd.tuxmobil.fahrplan.congress.base.requireOnSessionItemClickListener
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.extensions.replaceFragment
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments

class StarredListFragment : Fragment() {

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

    private var sidePane = false
    private val viewModel: FavoritesViewModel by viewModels { FavoritesViewModelFactory(requireContext()) }

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
                FavoritesScreen(
                    viewModel = viewModel,
                    showInSidePane = sidePane,
                    onBack = ::navigateBack,
                    onNavigateToSession = ::forwardSessionItemClick,
                )
            }
        }
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
