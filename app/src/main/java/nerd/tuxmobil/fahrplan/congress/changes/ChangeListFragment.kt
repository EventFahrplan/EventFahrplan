package nerd.tuxmobil.fahrplan.congress.changes

import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HeaderViewListAdapter
import android.widget.ListView
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import info.metadude.android.eventfahrplan.commons.flow.observe
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment.OnSessionListClick
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.replaceFragment
import nerd.tuxmobil.fahrplan.congress.extensions.requireViewByIdCompat
import nerd.tuxmobil.fahrplan.congress.extensions.withArguments
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatter
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatter

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

        const val FRAGMENT_TAG = "changes"

        fun newInstance(sidePane: Boolean): ChangeListFragment {
            return ChangeListFragment().withArguments(BundleKeys.SIDEPANE to sidePane)
        }

        @JvmStatic
        fun replace(fragmentManager: FragmentManager, @IdRes containerViewId: Int, sidePane: Boolean) {
            val fragment = ChangeListFragment().withArguments(
                BundleKeys.SIDEPANE to sidePane
            )
            fragmentManager.replaceFragment(containerViewId, fragment, FRAGMENT_TAG, FRAGMENT_TAG)
        }

    }

    private var onSessionListClickListener: OnSessionListClick? = null
    private lateinit var currentListView: ListView
    private val viewModelFactory by lazy { ChangeListViewModelFactory(appRepository) }
    private val viewModel: ChangeListViewModel by viewModels { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val arguments = requireArguments()
        val sidePane = arguments.getBoolean(BundleKeys.SIDEPANE)

        val contextThemeWrapper = ContextThemeWrapper(requireContext(), R.style.Theme_Congress_NoActionBar)
        val localInflater = inflater.cloneInContext(contextThemeWrapper)
        val (fragmentLayout, headerLayout) = when {
            sidePane -> R.layout.fragment_session_list_narrow to R.layout.changes_header
            else -> R.layout.fragment_session_list to R.layout.header_empty
        }

        val fragmentView = localInflater.inflate(fragmentLayout, container, false)
        val headerView = localInflater.inflate(headerLayout, null, false)
        currentListView = fragmentView.requireViewByIdCompat(android.R.id.list)
        currentListView.addHeaderView(headerView, null, false)
        currentListView.setHeaderDividersEnabled(false)
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.changeListParameter.observe(this) { (sessions, numDays, useDeviceTimeZone) ->
            val adapter = ChangeListAdapter(
                context = requireContext(),
                list = sessions,
                numDays = numDays,
                useDeviceTimeZone = useDeviceTimeZone,
                sessionPropertiesFormatter = SessionPropertiesFormatter(),
                contentDescriptionFormatter = ContentDescriptionFormatter(requireContext())
            )
            currentListView.adapter = adapter
        }
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

    override fun onListItemClick(listView: ListView, view: View, listPosition: Int, rowId: Long) {
        val headerViewListAdapter = currentListView.adapter as HeaderViewListAdapter
        val adapter = headerViewListAdapter.wrappedAdapter as ChangeListAdapter
        onSessionListClickListener?.let { listener ->
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            val clicked = adapter.getSession(listPosition - 1)
            if (!clicked.changedIsCanceled) {
                listener.onSessionListClick(clicked.sessionId)
            }
        }
    }

}
