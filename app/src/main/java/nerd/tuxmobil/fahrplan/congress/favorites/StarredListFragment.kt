package nerd.tuxmobil.fahrplan.congress.favorites

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.core.view.MenuProvider
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.Lifecycle.State.RESUMED
import info.metadude.android.eventfahrplan.commons.flow.observe
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.extensions.replaceFragment
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.Multiselect.OnDeleteSelectedClick
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.Multiselect.OnSelectionModeDismiss
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListViewEvent.OnDeleteAllWithConfirmationClick
import nerd.tuxmobil.fahrplan.congress.utils.ActivityHelper.navigateUp

class StarredListFragment : AbstractListFragment(), MenuProvider {

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
    private var hasStarredSessions = false
    private var multiSelectEnabled = false

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            viewModel.onViewEvent(OnSelectionModeDismiss)
        }
    }

    private val viewModel: StarredListViewModel by viewModels { StarredListViewModelFactory(requireContext()) }

    @MainThread
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sidePane = it.getBoolean(BundleKeys.SIDEPANE)
        }
        val activity = requireActivity()
        activity.addMenuProvider(this, this, RESUMED)
        activity.onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return content {
            EventFahrplanTheme {
                StarredListScreen(
                    viewModel = viewModel,
                    showInSidePane = sidePane,
                    onTitleTextChanged = ::updateScreenTitle,
                    onMultiSelectChanged = { enabled ->
                        multiSelectEnabled = enabled
                        backPressedCallback.isEnabled = enabled
                        requireActivity().invalidateOptionsMenu()
                    },
                    onNavigateToSession = ::navigateToSession,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.hasStarredSessions.observe(this) {
            hasStarredSessions = it
            requireActivity().invalidateOptionsMenu()
        }
    }

    private fun navigateToSession(sessionId: String) {
        onSessionListClickListener?.onSessionListClick(sessionId)
    }

    private fun updateScreenTitle(text: String) {
        if (requireActivity().title?.toString() != text) {
            requireActivity().title = text
        }
    }

    fun onToolbarBackPressed(): Boolean {
        if (!multiSelectEnabled) {
            return false
        }
        viewModel.onViewEvent(OnSelectionModeDismiss)
        return true
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

    @Suppress("KotlinConstantConditions")
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        if (multiSelectEnabled) {
            menuInflater.inflate(R.menu.starred_list_context_menu, menu)
        } else {
            menuInflater.inflate(R.menu.starred_list_menu, menu)
            var item = menu.findItem(R.id.menu_item_delete_all_favorites)
            if (item != null && !hasStarredSessions) {
                item.isVisible = false
            }
            val shareFavoritesItemRes = if (BuildConfig.ENABLE_CHAOSFLIX_EXPORT)
                R.id.menu_item_share_favorites_menu else
                R.id.menu_item_share_favorites
            item = menu.findItem(shareFavoritesItemRes)
            if (item != null) {
                item.isVisible = hasStarredSessions
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_item_delete_favorite -> {
                viewModel.onViewEvent(OnDeleteSelectedClick)
                return true
            }

            R.id.menu_item_share_favorites,
            R.id.menu_item_share_favorites_text -> {
                viewModel.share()
                return true
            }

            R.id.menu_item_share_favorites_json -> {
                viewModel.shareToChaosflix()
                return true
            }

            R.id.menu_item_delete_all_favorites -> {
                viewModel.onViewEvent(OnDeleteAllWithConfirmationClick)
                return true
            }

            android.R.id.home -> {
                if (multiSelectEnabled) {
                    viewModel.onViewEvent(OnSelectionModeDismiss)
                    return true
                }
                return requireActivity().navigateUp()
            }
        }
        return false
    }

}
