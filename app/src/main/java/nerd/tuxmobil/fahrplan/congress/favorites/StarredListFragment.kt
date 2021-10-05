package nerd.tuxmobil.fahrplan.congress.favorites

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.ActionMode
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.ListView
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment.OnSessionListClick
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.requireViewByIdCompat
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SessionSharer
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import nerd.tuxmobil.fahrplan.congress.utils.ActivityHelper
import nerd.tuxmobil.fahrplan.congress.utils.ConfirmationDialog

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
    AbstractListFragment(),
    MultiChoiceModeListener,
    AbsListView.OnScrollListener {

    companion object {

        const val FRAGMENT_TAG = "starred"
        const val DELETE_ALL_FAVORITES_REQUEST_CODE = 19126

        @JvmStatic
        fun newInstance(sidePane: Boolean): StarredListFragment {
            return StarredListFragment().withArguments(BundleKeys.SIDEPANE to sidePane)
        }

    }

    private var onSessionListClickListener: OnSessionListClick? = null
    private lateinit var starredList: MutableList<Session>
    private var sidePane = false

    /**
     * The fragment's ListView/GridView.
     */
    private lateinit var currentListView: ListView

    /**
     * The adapter which will be used to populate the ListView/GridView with Views.
     */
    private lateinit var adapter: StarredListAdapter
    private var preserveScrollPosition = false

    @MainThread
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            sidePane = it.getBoolean(BundleKeys.SIDEPANE)
        }
        setHasOptionsMenu(true)
        val context = requireContext()
        starredList = appRepository.loadStarredSessions().toMutableList()
        val meta = appRepository.readMeta()
        val useDeviceTimeZone = appRepository.readUseDeviceTimeZoneEnabled()
        adapter = StarredListAdapter(context, starredList, meta.numDays, useDeviceTimeZone)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contextThemeWrapper = ContextThemeWrapper(requireContext(), R.style.Theme_AppCompat_Light)
        val localInflater = inflater.cloneInContext(contextThemeWrapper)
        val view: View
        val header: View
        if (sidePane) {
            view = localInflater.inflate(R.layout.fragment_favorites_list_narrow, container, false)
            currentListView = view.requireViewByIdCompat(android.R.id.list)
            header = localInflater.inflate(R.layout.starred_header, null, false)
        } else {
            view = localInflater.inflate(R.layout.fragment_favorites_list, container, false)
            currentListView = view.requireViewByIdCompat(android.R.id.list)
            header = localInflater.inflate(R.layout.header_empty, null, false)
        }
        currentListView.addHeaderView(header, null, false)
        currentListView.setHeaderDividersEnabled(false)
        currentListView.choiceMode = AbsListView.CHOICE_MODE_MULTIPLE_MODAL
        currentListView.setMultiChoiceModeListener(this)
        currentListView.adapter = adapter
        currentListView.setOnScrollListener(this)
        return view
    }

    @MainThread
    @CallSuper
    override fun onResume() {
        super.onResume()
        if (!preserveScrollPosition) {
            jumpOverPastSessions()
        }
    }

    private fun jumpOverPastSessions() {
        if (!::starredList.isInitialized) {
            return
        }
        val nowMillis = Moment.now().toMilliseconds()
        var numSeparators = 0
        var i = 0
        while (i < starredList.size) {
            val session = starredList[i]
            if (session.endsAtDateUtc > nowMillis) {
                numSeparators = session.day
                break
            }
            i++
        }
        if (i > 0 && i < starredList.size) {
            currentListView.setSelection(i + 1 + numSeparators)
        }
    }

    @MainThread
    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
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
    }

    fun onRefresh() {
        val starred = appRepository.loadStarredSessions()
        if (::starredList.isInitialized) {
            starredList.clear()
            starredList.addAll(starred)
        }
        adapter.notifyDataSetChanged()
    }

    override fun onListItemClick(listView: ListView, view: View, listPosition: Int, rowId: Long) {
        var currentPosition = listPosition
        if (onSessionListClickListener != null) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            currentPosition--
            val clicked = adapter.getSession(currentPosition)
            onSessionListClickListener?.onSessionListClick(clicked.sessionId)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.starred_list_menu, menu)
        var item = menu.findItem(R.id.menu_item_delete_all_favorites)
        if (item != null && (!::starredList.isInitialized || starredList.isEmpty())) {
            item.isVisible = false
        }
        val shareFavoritesItemRes = if (BuildConfig.ENABLE_CHAOSFLIX_EXPORT)
            R.id.menu_item_share_favorites_menu else
            R.id.menu_item_share_favorites
        item = menu.findItem(shareFavoritesItemRes)
        if (item != null) {
            item.isVisible = ::starredList.isInitialized && starredList.isNotEmpty()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_share_favorites,
            R.id.menu_item_share_favorites_text -> {
                shareSessions()
                return true
            }
            R.id.menu_item_share_favorites_json -> {
                shareSessionsToChaosflix()
                return true
            }
            R.id.menu_item_delete_all_favorites -> {
                askToDeleteAllFavorites()
                return true
            }
            android.R.id.home -> {
                return ActivityHelper.navigateUp(requireActivity())
            }
        }
        return super.onOptionsItemSelected(item)
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
                deleteItems(currentListView.checkedItemPositions)
                val activity = requireActivity()
                activity.invalidateOptionsMenu()
                refreshViews(activity)
                mode.finish()
                true
            }
            else -> false
        }
    }

    private fun deleteSession(session: Session) {
        session.highlight = false
        appRepository.updateHighlight(session)
        appRepository.notifyHighlightsChanged()
    }

    private fun deleteItems(checkedItemPositions: SparseBooleanArray) {
        val itemsCount = currentListView.adapter.count
        for (itemId in itemsCount - 1 downTo 0) {
            if (checkedItemPositions[itemId]) {
                val index = itemId - 1
                val session = adapter.getSession(index)
                deleteSession(session)
                starredList.removeAt(index)
            }
        }
    }

    private fun refreshViews(activity: Activity) {
        adapter.notifyDataSetChanged()
        activity.setResult(Activity.RESULT_OK)
        activity.invalidateOptionsMenu()
    }

    override fun onDestroyActionMode(mode: ActionMode) = Unit

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
        appRepository.deleteAllHighlights()
        appRepository.notifyHighlightsChanged()
        for (starredSession in starredList) {
            starredSession.highlight = false
        }
        starredList.clear()
        val activity = requireActivity()
        activity.invalidateOptionsMenu()
        refreshViews(activity)
    }

    private fun shareSessions() {
        val timeZoneId = appRepository.readMeta().timeZoneId
        SimpleSessionFormat.format(starredList, timeZoneId)?.let { formattedSession ->
            SessionSharer.shareSimple(requireContext(), formattedSession)
        }
    }

    private fun shareSessionsToChaosflix() {
        JsonSessionFormat().format(starredList)?.let { formattedSession ->
            val context = requireContext()
            if (!SessionSharer.shareJson(context, formattedSession)) {
                Toast.makeText(context, R.string.share_error_activity_not_found, Toast.LENGTH_SHORT).show()
            }
        }
    }

}
