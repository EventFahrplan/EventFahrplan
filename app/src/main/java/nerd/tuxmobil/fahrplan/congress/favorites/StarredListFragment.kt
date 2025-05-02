package nerd.tuxmobil.fahrplan.congress.favorites

import android.content.Context
import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.ActionMode
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.lifecycleScope
import info.metadude.android.eventfahrplan.commons.flow.observe
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment.OnSessionListClick
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolver
import nerd.tuxmobil.fahrplan.congress.commons.ScreenNavigation
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.applyHorizontalInsets
import nerd.tuxmobil.fahrplan.congress.extensions.replaceFragment
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.sharing.SessionSharer
import nerd.tuxmobil.fahrplan.congress.sidepane.OnSidePaneCloseListener
import nerd.tuxmobil.fahrplan.congress.utils.ActivityHelper.navigateUp
import nerd.tuxmobil.fahrplan.congress.utils.ConfirmationDialog
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatter
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatter
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 *
 * Large screen devices (such as tablets) are supported by replacing the ListView with a GridView.
 *
 * Activities containing this fragment MUST implement the [OnSessionListClick] interface.
 *
 * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon
 * screen orientation changes).
 */
class StarredListFragment :
    Fragment(),
    MenuProvider,
    MultiChoiceModeListener,
    AbsListView.OnScrollListener {

    companion object {

        const val FRAGMENT_TAG = "starred"
        const val DELETE_ALL_FAVORITES_REQUEST_CODE = 19126

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
    private lateinit var starredList: List<Session>
    private var sidePane = false
    private var actionMode: ActionMode? = null

    /**
     * The fragment's ListView/GridView.
     */
//    private lateinit var currentListView: ListView

    private var headerView: TextView? = null

//    private lateinit var loadingSpinnerView: View

//    private val starredListAdapter: StarredListAdapter
//        get() {
//            val headerViewListAdapter = currentListView.adapter as HeaderViewListAdapter
//            return headerViewListAdapter.wrappedAdapter as StarredListAdapter
//        }

    private var preserveScrollPosition = false

    private val viewModelFactory by lazy {
        val resourceResolving = ResourceResolver(requireContext())
        StarredListViewModelFactory(
            appRepository = AppRepository,
            resourceResolving = resourceResolving,
            sessionPropertiesFormatting = SessionPropertiesFormatter(resourceResolving),
            contentDescriptionFormatting = ContentDescriptionFormatter(resourceResolving),
        )
    }
    private val viewModel: StarredListViewModel by viewModels { viewModelFactory }

    @MainThread
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sidePane = it.getBoolean(BundleKeys.SIDEPANE)
        }
        requireActivity().addMenuProvider(this, this, RESUMED)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setContent {
                // Collect all state needed for the UI
                val searchResultState by viewModel.searchResultsState.collectAsState()
                val isMultiSelectMode by viewModel.isMultiSelectMode.collectAsState()
                val checkedStates by viewModel.checkedStates.collectAsState()
                
                // Create the UI state
                val uiState = FavoredSessionsUiState(
                    searchResultState = searchResultState,
                    isMultiSelectMode = isMultiSelectMode,
                    checkedSessionIds = checkedStates
                )
                
                // Render the UI with state and event handler
                FavoredSessionsScreen(
                    uiState = uiState,
                    onViewEvent = { event -> handleViewEvent(event) }
                )
            }
            isClickable = true
            applyHorizontalInsets()
            //currentListView.applyBottomPadding()
        }
    }
    
    private fun handleViewEvent(event: FavoredSessionsViewEvent) {
        when (event) {
            is FavoredSessionsViewEvent.OnBackClick -> {
                if (viewModel.isMultiSelectMode.value) {
                    viewModel.exitMultiSelectMode()
                } else {
                    navigateBack()
                }
            }
            is FavoredSessionsViewEvent.OnItemClick -> {
                viewModel.onViewEvent(event)
            }
            is FavoredSessionsViewEvent.OnCheckedStateChange -> {
                viewModel.toggleCheckedState(event.sessionId)
            }
            is FavoredSessionsViewEvent.OnCheckedSessionsChange -> {
                viewModel.onViewEvent(event)
            }
            is FavoredSessionsViewEvent.OnShareClick -> {
                viewModel.onViewEvent(event)
            }
            is FavoredSessionsViewEvent.OnDeleteClick -> {
                viewModel.onViewEvent(event)
            }
            is FavoredSessionsViewEvent.OnMultiSelectToggle -> {
                viewModel.onViewEvent(event)
            }
        }
    }
    
    private fun navigateBack() {
        val activity = requireActivity()
        if (activity is OnSidePaneCloseListener) {
            // Handle specialized side pane behavior
            activity.onSidePaneClose(FRAGMENT_TAG)
        } else {
            // Let the system handle standard back navigation
            activity.onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.starredListParameter.observe(this) { (sessions, numDays, useDeviceTimeZone) ->
            starredList = sessions
            val activity = requireActivity()
            val resourceResolving = ResourceResolver(activity)
            val adapter = StarredListAdapter(
                context = activity,
                list = sessions,
                numDays = numDays,
                useDeviceTimeZone = useDeviceTimeZone,
                sessionPropertiesFormatting = SessionPropertiesFormatter(resourceResolving),
                contentDescriptionFormatting = ContentDescriptionFormatter(resourceResolving),
            )
            updateHeaderOrTitleText(sessions.size)
//            currentListView.adapter = adapter
            activity.invalidateOptionsMenu()

//            loadingSpinnerView.isVisible = false
            jumpOverPastSessions()
        }
        viewModel.navigateBack.observe(viewLifecycleOwner) {
            // Always make sure action mode is destroyed before navigating back
            ensureActionModeDestroyed("navigateBack event")
            navigateBack()
        }
        viewModel.multiSelectToggled.observe(viewLifecycleOwner) {
            Logging.get().d("StarredListFragment", "multiSelectToggled observed")
            
            // Get the current checked session count
            val hasCheckedSessions = viewModel.getCheckedSessionCount() > 0
            
            // Only toggle action mode if there are checked sessions or if we need to exit existing action mode
            if (actionMode == null) {
                // Only start action mode if there are checked sessions
                if (hasCheckedSessions) {
                    actionMode = requireActivity().startActionMode(this)
                    Logging.get().d("StarredListFragment", "Starting action mode: $actionMode")
                } else {
                    Logging.get().d("StarredListFragment", "Not starting action mode because there are no checked sessions")
                }
            } else {
                // Always finish action mode if it exists
                actionMode?.finish()
                actionMode = null
                Logging.get().d("StarredListFragment", "Finishing existing action mode")
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
        // Observe multiselect state to keep action mode in sync
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isMultiSelectMode.collect { isMultiSelectMode ->
                if (isMultiSelectMode && actionMode == null) {
                    // If we should be in multiselect but action mode is null, start it
                    if (viewModel.hasCheckedSessions()) {
                        actionMode = requireActivity().startActionMode(this@StarredListFragment)
                        Logging.get().d("StarredListFragment", "Starting action mode from state collection")
                    }
                } else if (!isMultiSelectMode && actionMode != null) {
                    // If we shouldn't be in multiselect but action mode exists, finish it
                    actionMode?.finish()
                    actionMode = null
                    Logging.get().d("StarredListFragment", "Finishing action mode from state collection")
                }
            }
        }
    }
    
    /**
     * Helper method to ensure action mode is destroyed with proper ViewModel cleanup
     */
    private fun ensureActionModeDestroyed(reason: String) {
        if (actionMode != null) {
            Logging.get().d("StarredListFragment", "Finishing action mode due to: $reason")
            actionMode?.finish()
            actionMode = null
            viewModel.onActionModeDestroyed()
        }
    }

    private fun updateHeaderOrTitleText(sessionsSize: Int) {
        val headerOrTitleText = when (sessionsSize == 0) {
            true -> getString(R.string.favorites_screen_default_title)
            false -> resources.getQuantityString(R.plurals.favorites_screen_title, sessionsSize, sessionsSize)
        }
        headerView?.text = headerOrTitleText
        requireActivity().title = headerOrTitleText
    }

    @MainThread
    @CallSuper
    override fun onResume() {
        super.onResume()
        if (!preserveScrollPosition) {
            jumpOverPastSessions()
        }
        // Notify ViewModel to ensure clean state
        viewModel.onFragmentResume()
    }

    @MainThread
    @CallSuper
    override fun onPause() {
        super.onPause()
        // Ensure action mode is always destroyed when leaving the fragment
        if (actionMode != null) {
            Logging.get().d("StarredListFragment", "Finishing action mode on pause")
            actionMode?.finish()
            actionMode = null
            viewModel.onActionModeDestroyed()
        }
        // Notify ViewModel about lifecycle
        viewModel.onFragmentPause()
    }

    @MainThread
    @CallSuper
    override fun onStop() {
        super.onStop()
        // Extra guarantee to ensure action mode doesn't persist
        if (actionMode != null) {
            Logging.get().d("StarredListFragment", "Finishing action mode on stop")
            actionMode?.finish()
            actionMode = null
            viewModel.onActionModeDestroyed()
        }
    }

    private fun jumpOverPastSessions() {
        if (!::starredList.isInitialized) {
            return
        }
        val now = Moment.now()
        var numSeparators = 0
        var i = 0
        while (i < starredList.size) {
            val session = starredList[i]
            if (session.endsAt.isAfter(now)) {
                numSeparators = session.dayIndex
                break
            }
            i++
        }
//        if (i > 0 && i < starredList.size) {
//            currentListView.setSelection(i + 1 + numSeparators)
//        }
    }

    @MainThread
    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.screenNavigation = ScreenNavigation { sessionId ->
            onSessionListClickListener?.onSessionListClick(sessionId)
        }
        onSessionListClickListener = try {
            context as OnSessionListClick
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnSessionListClick")
        }
    }

    @MainThread
    @CallSuper
    override fun onDetach() {
        super.onDetach()
        onSessionListClickListener = null
        viewModel.screenNavigation = null
    }

//    override fun onListItemClick(listView: ListView, view: View, listPosition: Int, rowId: Long) {
//        var currentPosition = listPosition
//        if (onSessionListClickListener != null) {
//            // Notify the active callbacks interface (the activity, if the
//            // fragment is attached to one) that an item has been selected.
//            currentPosition--
//            val clicked = starredListAdapter.getSession(currentPosition)
//            onSessionListClickListener?.onSessionListClick(clicked.sessionId)
//        }
//    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.starred_list_menu, menu)
        var item = menu.findItem(R.id.menu_item_delete_all_favorites)
        if (item != null && (!::starredList.isInitialized || starredList.isEmpty())) {
            item.isVisible = false
        }
        val shareFavoritesItemRes = if (viewModel.isChaosflixExportEnabled())
            R.id.menu_item_share_favorites_menu else
            R.id.menu_item_share_favorites
        item = menu.findItem(shareFavoritesItemRes)
        if (item != null) {
            item.isVisible = ::starredList.isInitialized && starredList.isNotEmpty()
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
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
                askToDeleteAllFavorites()
                return true
            }

            android.R.id.home -> {
                // Ensure action mode is destroyed when navigating up via toolbar
                if (actionMode != null) {
                    Logging.get().d("StarredListFragment", "Finishing action mode on up navigation")
                    actionMode?.finish()
                    actionMode = null
                    viewModel.onActionModeDestroyed()
                }
                return requireActivity().navigateUp()
            }
        }
        return false
    }

    override fun onItemCheckedStateChanged(
        mode: ActionMode,
        position: Int,
        id: Long,
        checked: Boolean
    ) = Unit

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) = Unit

    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
        if (firstVisibleItem > 0) {
            preserveScrollPosition = true
        }
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        val inflater = mode.menuInflater
        inflater.inflate(R.menu.starred_list_context_menu, menu)
        mode.title = getString(R.string.choose_to_delete)
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_delete_favorite -> {
                Logging.get().d("StarredListFragment", "menu_item_delete_favorite")
                viewModel.unfavorCheckedSessions()
                mode.finish()
                actionMode = null
                true
            }

            else -> false
        }
    }

    private fun deleteItems(checkedItemPositions: SparseBooleanArray) {
//        val itemsCount = currentListView.adapter.count
//        for (itemId in itemsCount - 1 downTo 0) {
//            if (checkedItemPositions[itemId]) {
//                val session = starredListAdapter.getSession(itemId - 1)
//                viewModel.unfavorSession(session)
//            }
//        }
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        actionMode = null
        viewModel.onActionModeDestroyed()
    }

    private fun askToDeleteAllFavorites() {
        val fragmentManager = parentFragmentManager
        val fragment = fragmentManager.findFragmentByTag(ConfirmationDialog.FRAGMENT_TAG)
        if (fragment == null) {
            val confirm = ConfirmationDialog.newInstance(
                R.string.dlg_delete_all_favorites,
                DELETE_ALL_FAVORITES_REQUEST_CODE)
            confirm.show(fragmentManager, ConfirmationDialog.FRAGMENT_TAG)
        }
    }

    fun deleteAllFavorites() {
        if (!::starredList.isInitialized || starredList.isEmpty()) {
            return
        }
        viewModel.unfavorAllSessions()
    }

}
