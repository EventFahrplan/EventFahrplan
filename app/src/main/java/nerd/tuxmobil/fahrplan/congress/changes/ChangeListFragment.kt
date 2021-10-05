package nerd.tuxmobil.fahrplan.congress.changes

import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import nerd.tuxmobil.fahrplan.congress.MyApp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment.OnSessionListClick
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.requireViewByIdCompat
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper

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
class ChangeListFragment : AbstractListFragment() {

    companion object {

        private const val LOG_TAG = "ChangeListFragment"
        const val FRAGMENT_TAG = "changes"

        @JvmStatic
        fun newInstance(sidePane: Boolean): ChangeListFragment {
            return ChangeListFragment().withArguments(BundleKeys.SIDEPANE to sidePane)
        }

    }

    private var onSessionListClickListener: OnSessionListClick? = null
    private lateinit var changesList: MutableList<Session>
    private var sidePane = false

    /**
     * The Adapter which will be used to populate the ListView/GridView with Views.
     */
    private lateinit var changeListAdapter: ChangeListAdapter

    @MainThread
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val arguments = requireArguments()
        sidePane = arguments.getBoolean(BundleKeys.SIDEPANE)
        changesList = appRepository.loadChangedSessions().toMutableList()
        val meta = appRepository.readMeta()
        val useDeviceTimeZone = appRepository.readUseDeviceTimeZoneEnabled()
        changeListAdapter = ChangeListAdapter(requireContext(), changesList, meta.numDays, useDeviceTimeZone)
        MyApp.LogDebug(LOG_TAG, "onCreate, ${changesList.size} changes")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contextThemeWrapper: Context = ContextThemeWrapper(requireContext(), R.style.Theme_AppCompat_Light)
        val localInflater = inflater.cloneInContext(contextThemeWrapper)
        val (fragmentLayout, headerLayout) = when {
            sidePane -> R.layout.fragment_session_list_narrow to R.layout.changes_header
            else -> R.layout.fragment_session_list to R.layout.header_empty
        }
        val fragmentView = localInflater.inflate(fragmentLayout, container, false)
        val headerView = localInflater.inflate(headerLayout, null, false)
        val listView = fragmentView.requireViewByIdCompat<ListView>(android.R.id.list)
        listView.addHeaderView(headerView, null, false)
        listView.setHeaderDividersEnabled(false)
        listView.adapter = changeListAdapter
        return fragmentView
    }

    override fun onResume() {
        super.onResume()
        NotificationHelper(requireContext()).cancelScheduleUpdateNotification()
        appRepository.updateScheduleChangesSeen(true)
    }

    @MainThread
    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
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
    }

    fun onRefresh() {
        val updatedChanges = appRepository.loadChangedSessions()
        if (::changesList.isInitialized) {
            changesList.clear()
            changesList.addAll(updatedChanges)
        }
        changeListAdapter.notifyDataSetChanged()
    }

    override fun onListItemClick(listView: ListView, view: View, listPosition: Int, rowId: Long) {
        var currentPosition = listPosition
        onSessionListClickListener?.let { listener ->
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            currentPosition--
            val clicked = changeListAdapter.getSession(currentPosition)
            if (!clicked.changedIsCanceled) {
                listener.onSessionListClick(clicked.sessionId)
            }
        }
    }

}
