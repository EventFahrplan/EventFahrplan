package nerd.tuxmobil.fahrplan.congress.schedule

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.TextUtils.TruncateAt
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.Gravity.CENTER
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar.NAVIGATION_MODE_LIST
import androidx.appcompat.app.ActionBar.OnNavigationListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.core.widget.NestedScrollView.OnScrollChangeListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.MyApp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmTimePickerFragment
import nerd.tuxmobil.fahrplan.congress.calendar.CalendarSharing
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.getLayoutInflater
import nerd.tuxmobil.fahrplan.congress.extensions.isLandscape
import nerd.tuxmobil.fahrplan.congress.extensions.requireViewByIdCompat
import nerd.tuxmobil.fahrplan.congress.models.DateInfos
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.net.ConnectivityObserver
import nerd.tuxmobil.fahrplan.congress.net.ErrorMessage
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.schedule.observables.TimeTextViewParameter
import nerd.tuxmobil.fahrplan.congress.sharing.SessionSharer
import nerd.tuxmobil.fahrplan.congress.utils.Font
import nerd.tuxmobil.fahrplan.congress.utils.TypefaceFactory
import org.ligi.tracedroid.logging.Log

class FahrplanFragment : Fragment(), SessionViewEventsHandler {

    /**
     * Interface definition for a callback to be invoked when a session view is clicked.
     */
    internal interface OnSessionClickListener {
        /**
         * Called when the session view has been clicked.
         */
        fun onSessionClick(sessionId: String)
    }

    companion object {

        private const val LOG_TAG = "FahrplanFragment"
        const val FRAGMENT_TAG = "schedule"
        private const val FAHRPLAN_FRAGMENT_REQUEST_KEY = "FAHRPLAN_FRAGMENT_REQUEST_KEY"

        private const val CONTEXT_MENU_ITEM_ID_FAVORITES = 0
        private const val CONTEXT_MENU_ITEM_ID_SET_ALARM = 1
        private const val CONTEXT_MENU_ITEM_ID_DELETE_ALARM = 2
        private const val CONTEXT_MENU_ITEM_ID_ADD_TO_CALENDAR = 3
        private const val CONTEXT_MENU_ITEM_ID_SHARE = 4
        private const val CONTEXT_MENU_ITEM_ID_SHARE_TEXT = 5
        private const val CONTEXT_MENU_ITEM_ID_SHARE_JSON = 6

        const val FIFTEEN_MINUTES = 15
        const val BOX_HEIGHT_MULTIPLIER = 3

    }

    private lateinit var inflater: LayoutInflater
    private lateinit var sessionViewDrawer: SessionViewDrawer
    private lateinit var errorMessageFactory: ErrorMessage.Factory
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var roomTitleTypeFace: Typeface
    private lateinit var contextMenuView: View
    private lateinit var viewModel: FahrplanViewModel

    private var onSessionClickListener: OnSessionClickListener? = null
    private var lastSelectedSession: Session? = null
    private var displayDensityScale = 0f

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val appRepository = AppRepository
        val alarmServices = AlarmServices.newInstance(context, appRepository)
        val menuEntriesGenerator = NavigationMenuEntriesGenerator(dayString = getString(R.string.day), todayString = getString(R.string.today))
        val viewModelFactory = FahrplanViewModelFactory(appRepository, alarmServices, menuEntriesGenerator)
        viewModel = ViewModelProvider(this, viewModelFactory).get(FahrplanViewModel::class.java)
        onSessionClickListener = if (context is OnSessionClickListener) {
            context
        } else {
            error("$context must implement OnSessionClickListener")
        }
    }

    override fun onDetach() {
        onSessionClickListener = null
        super.onDetach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        val context = requireContext()
        roomTitleTypeFace = TypefaceFactory.getNewInstance(context).getTypeface(Font.Roboto.Light)
        sessionViewDrawer = SessionViewDrawer(context, { sessionPadding })
        errorMessageFactory = ErrorMessage.Factory(context)
        connectivityObserver = ConnectivityObserver(context, onConnectionAvailable = {
            Log.d(LOG_TAG, "Network is available.")
            viewModel.requestScheduleAutoUpdate()
        })
        connectivityObserver.start()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layoutRootView = inflater.inflate(R.layout.schedule, container, false)
        val verticalScrollView = layoutRootView.requireViewByIdCompat<NestedScrollView>(R.id.verticalScrollView)
        verticalScrollView.setOnScrollChangeListener(
            OnScrollChangeListener { _, _, _, _, _ -> viewModel.preserveVerticalScrollPosition = true }
        )
        return layoutRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        displayDensityScale = resources.displayMetrics.density

        val roomScroller = view.requireViewByIdCompat<HorizontalScrollView>(R.id.roomScroller)
        val snapScroller = view.requireViewByIdCompat<HorizontalSnapScrollView>(R.id.horizScroller)
        snapScroller.setChildScroller(roomScroller)
        roomScroller.setOnTouchListener { _, _ -> true }

        inflater = view.context.getLayoutInflater()
    }

    private fun observeViewModel() {
        viewModel.fahrplanParameter.observe(viewLifecycleOwner) { (scheduleData, numDays, dayIndex, menuEntries) ->
            menuEntries?.let { buildNavigationMenu(it, numDays) }
            viewModel.fillTimes(Moment.now(), getNormalizedBoxHeight())
            viewDay(scheduleData, numDays, dayIndex)
        }
        viewModel.fahrplanEmptyParameter.observe(viewLifecycleOwner) { (scheduleVersion) ->
            val errorMessage = errorMessageFactory.getMessageForEmptySchedule(scheduleVersion)
            errorMessage.show(requireContext(), shouldShowLong = false)
        }
        viewModel.shareSimple.observe(viewLifecycleOwner) { formattedSession ->
            SessionSharer.shareSimple(requireContext(), formattedSession)
        }
        viewModel.shareJson.observe(viewLifecycleOwner) { jsonFormattedSession ->
            val context = requireContext()
            if (!SessionSharer.shareJson(context, jsonFormattedSession)) {
                Toast.makeText(context, R.string.share_error_activity_not_found, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.timeTextViewParameters.observe(viewLifecycleOwner) { timeTextViewParameters ->
            fillTimes(timeTextViewParameters)
        }
        viewModel.scrollToCurrentSessionParameter.observe(viewLifecycleOwner) { (scheduleData, dateInfos) ->
            val boxHeight = getNormalizedBoxHeight()
            scrollToCurrent(boxHeight, scheduleData, dateInfos)
        }
        viewModel.scrollToSessionParameter.observe(viewLifecycleOwner) { (sessionId, verticalPosition, roomIndex) ->
            scrollTo(sessionId, verticalPosition, roomIndex)
        }
    }

    override fun onResume() {
        Log.d(LOG_TAG, "FahrplanFragment#onResume")
        super.onResume()
        val activity = requireActivity()
        activity.invalidateOptionsMenu()
        val intent = activity.intent
        // TODO Consume session alarm in MainActivity and let it orchestrate its fragments
        val sessionId = intent.getStringExtra(BundleKeys.SESSION_ALARM_SESSION_ID)
        // TODO Analyzing https://github.com/EventFahrplan/EventFahrplan/issues/402
        Log.d(LOG_TAG, "sessionId = $sessionId (after loading from bundle)")
        if (sessionId != null) {
            Log.d(LOG_TAG, "Open with sessionId '$sessionId'.")
            val sessionAlarmDayIndex = intent.getIntExtra(BundleKeys.SESSION_ALARM_DAY_INDEX, -1)
            viewModel.saveSelectedDayIndex(sessionAlarmDayIndex)
            viewModel.scrollToSession(sessionId, getNormalizedBoxHeight())
            intent.removeExtra(BundleKeys.SESSION_ALARM_SESSION_ID) // jump to given sessionId only once
        }
        Logging.get().d(LOG_TAG, "onResume")
    }

    override fun onDestroy() {
        connectivityObserver.stop()
        super.onDestroy()
    }

    /**
     * Updates the session data in the schedule view.
     */
    private fun viewDay(scheduleData: ScheduleData, numDays: Int, dayIndex: Int) {
        val layoutRoot = requireView()
        val horizontalScroller = layoutRoot.requireViewByIdCompat<HorizontalSnapScrollView>(R.id.horizScroller)
        horizontalScroller.scrollTo(0, 0)
        val roomCount = scheduleData.roomCount
        horizontalScroller.setRoomsCount(roomCount)

        val roomScroller = layoutRoot.requireViewByIdCompat<HorizontalScrollView>(R.id.roomScroller)
        val roomTitlesRowLayout = roomScroller.getChildAt(0) as LinearLayout
        val columnWidth = horizontalScroller.columnWidth
        addRoomTitleViews(roomTitlesRowLayout, columnWidth, scheduleData.roomNames)
        addRoomColumns(horizontalScroller, columnWidth, scheduleData)

        MainActivity.instance.shouldScheduleScrollToCurrentTimeSlot {
            if (!viewModel.preserveVerticalScrollPosition) {
                viewModel.scrollToCurrentSession()
                viewModel.preserveVerticalScrollPosition = false
            }
        }
        updateNavigationMenuSelection(numDays, dayIndex)
    }

    private fun updateNavigationMenuSelection(numDays: Int, dayIndex: Int) {
        val activity = requireActivity() as AppCompatActivity
        val actionbar = activity.supportActionBar
        if (actionbar != null && numDays > 1) {
            actionbar.setSelectedNavigationItem(dayIndex - 1)
        }
    }

    /**
     * Adds `roomCount` room column views as child views to the first child
     * (which is a row layout) of the given [horizontalScroller] layout.
     * Previously added child views are removed.
     */
    private fun addRoomColumns(
        horizontalScroller: HorizontalSnapScrollView,
        columnWidth: Int,
        scheduleData: ScheduleData
    ) {
        val columnsLayout = horizontalScroller.getChildAt(0) as LinearLayout
        // TODO Optimization: Track room names and check if they can be re-used with the updated scheduleData
        columnsLayout.removeAllViews()
        val boxHeight = getNormalizedBoxHeight()
        val layoutCalculator = LayoutCalculator(boxHeight)
        val context = horizontalScroller.context
        val roomDataList = scheduleData.roomDataList
        val conference = Conference.ofSessions(scheduleData.allSessions)
        for (roomIndex in roomDataList.indices) {
            val roomData = roomDataList[roomIndex]
            val layoutParamsBySession = layoutCalculator.calculateLayoutParams(roomData, conference)
            val columnRecyclerView = RecyclerView(context).apply {
                setHasFixedSize(true)
                setFadingEdgeLength(0)
                isNestedScrollingEnabled = false // enables flinging
                layoutManager = LinearLayoutManager(context)
                layoutParams = LayoutParams(columnWidth, WRAP_CONTENT)
            }
            val roomSessions = roomData.sessions
            val adapter = SessionViewColumnAdapter(
                sessions = roomSessions,
                layoutParamsBySession = layoutParamsBySession,
                drawer = sessionViewDrawer,
                eventsHandler = this
            )
            columnRecyclerView.adapter = adapter
            columnsLayout.addView(columnRecyclerView)
        }
    }

    /**
     * Adds room title views as child views to the given [roomTitlesRowLayout].
     * Previously added child views are removed.
     */
    private fun addRoomTitleViews(
        roomTitlesRowLayout: LinearLayout,
        columnWidth: Int,
        roomNames: List<String>
    ) {
        roomTitlesRowLayout.removeAllViews()
        val titleTextSize = resources.getInteger(R.integer.room_title_size).toFloat()
        val params = LinearLayout.LayoutParams(columnWidth, WRAP_CONTENT, 1f).apply {
            gravity = CENTER
        }
        val paddingRight = sessionPadding
        val context = roomTitlesRowLayout.context
        val titleTextColor = ContextCompat.getColor(context, android.R.color.white)
        for (roomName in roomNames) {
            val roomTitle = TextView(context).apply {
                layoutParams = params
                maxLines = 1
                ellipsize = TruncateAt.END
                updatePadding(right = paddingRight)
                gravity = CENTER
                typeface = roomTitleTypeFace
                text = roomName
                setTextColor(titleTextColor)
                contentDescription = getString(R.string.session_list_item_room_content_description, roomName)
                textSize = titleTextSize
            }
            roomTitlesRowLayout.addView(roomTitle)
        }
    }

    /**
     * Jump to current time or session, if we are on today's session list
     * Scrolling to a session as an reaction to tapping a session alarm notification
     * is handled in [scrollTo].
     */
    private fun scrollToCurrent(boxHeight: Int, scheduleData: ScheduleData, dateInfos: DateInfos) {
        var columnIndex = -1
        val layoutRootView = requireView()
        if (!layoutRootView.context.isLandscape()) {
            val horizontalSnapScrollView = layoutRootView.findViewById<HorizontalSnapScrollView>(R.id.horizScroller)
            columnIndex = horizontalSnapScrollView.columnIndex
            MyApp.LogDebug(LOG_TAG, "y pos = $columnIndex")
        }
        val nowMoment = Moment.now()
        val conference = Conference.ofSessions(scheduleData.allSessions)
        val scrollAmount = ScrollAmountCalculator(Logging.get()).calculateScrollAmount(
            conference = conference,
            dateInfos = dateInfos,
            scheduleData = scheduleData,
            nowMoment = nowMoment,
            currentDayIndex = scheduleData.dayIndex,
            boxHeight = boxHeight,
            columnIndex = columnIndex
        )
        layoutRootView.requireViewByIdCompat<NestedScrollView>(R.id.verticalScrollView).apply {
            scrollTo(0, scrollAmount)
            post { scrollTo(0, scrollAmount) }
        }
    }

    private fun setBell(session: Session) {
        val verticalScrollView = requireView().findViewById<NestedScrollView?>(R.id.verticalScrollView)
            ?: return
        val sessionView = verticalScrollView.findViewWithTag<View?>(session)
            ?: return
        val bellView = sessionView.findViewById<ImageView?>(R.id.session_bell_view)
            ?: return
        bellView.isVisible = session.hasAlarm
    }

    private fun scrollTo(sessionId: String, verticalPosition: Int, roomIndex: Int) {
        val layoutRootView = requireView()
        layoutRootView.requireViewByIdCompat<NestedScrollView>(R.id.verticalScrollView).apply {
            post { scrollTo(0, verticalPosition) }
        }
        val horizontalSnapScrollView = layoutRootView.findViewById<HorizontalSnapScrollView?>(R.id.horizScroller)
        horizontalSnapScrollView?.post { horizontalSnapScrollView.scrollToColumn(roomIndex, false) }
        val activity = requireActivity()
        val sidePaneView = activity.findViewById<FragmentContainerView?>(R.id.detail)
        if (sidePaneView != null && onSessionClickListener != null) {
            onSessionClickListener!!.onSessionClick(sessionId)
        }
    }

    private fun fillTimes(parameters: List<TimeTextViewParameter>) {
        val timeTextColumn = requireView().requireViewByIdCompat<LinearLayout>(R.id.times_layout)
        timeTextColumn.removeAllViews()
        var timeTextView: View
        for ((layout, height, titleText) in parameters) {
            timeTextView = inflater.inflate(layout, null)
            timeTextColumn.addView(timeTextView, MATCH_PARENT, height)
            timeTextView.requireViewByIdCompat<TextView>(R.id.time).apply {
                text = titleText
            }
        }
    }

    private val sessionPadding: Int
        get() {
            val factor = if (requireContext().isLandscape()) 8 else 10
            return (factor * displayDensityScale).toInt()
        }

    private fun getNormalizedBoxHeight(): Int {
        val orientationText = if (requireContext().isLandscape()) "landscape" else "other orientation"
        MyApp.LogDebug(LOG_TAG, orientationText)
        return (resources.getInteger(R.integer.box_height) * displayDensityScale).toInt()
    }

    override fun onClick(view: View) {
        val session = checkNotNull(view.tag) {
            "A session must be assigned to the 'tag' attribute of the session view."
        } as Session
        MyApp.LogDebug(LOG_TAG, "Click on ${session.title}")
        onSessionClickListener?.onSessionClick(session.sessionId)
    }

    private fun buildNavigationMenu(dayMenuEntries: List<String?>, numDays: Int) {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar!!.navigationMode = NAVIGATION_MODE_LIST
        val arrayAdapter = ArrayAdapter(
            actionBar.themedContext,
            R.layout.support_simple_spinner_dropdown_item_large,
            dayMenuEntries
        )
        arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_list_item)
        actionBar.setListNavigationCallbacks(arrayAdapter, OnDaySelectedListener(numDays))
    }

    private fun showAlarmTimePicker() {
        AlarmTimePickerFragment.show(this, FAHRPLAN_FRAGMENT_REQUEST_KEY) { requestKey, result ->
            if (requestKey == FAHRPLAN_FRAGMENT_REQUEST_KEY &&
                result.containsKey(AlarmTimePickerFragment.ALARM_TIMES_INDEX_BUNDLE_KEY)
            ) {
                val alarmTimesIndex = result.getInt(AlarmTimePickerFragment.ALARM_TIMES_INDEX_BUNDLE_KEY)
                onAlarmTimesIndexPicked(alarmTimesIndex)
            }
        }
    }

    private fun onAlarmTimesIndexPicked(alarmTimesIndex: Int) {
        if (lastSelectedSession == null) {
            Log.e(javaClass.simpleName, "onAlarmTimesIndexPicked: session: null. alarmTimesIndex: $alarmTimesIndex")
            throw NullPointerException("Session is null.")
        } else {
            viewModel.addAlarm(lastSelectedSession!!, alarmTimesIndex)
            setBell(lastSelectedSession!!)
            updateMenuItems()
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val menuItemIndex = item.itemId
        val session = contextMenuView.tag as Session
        lastSelectedSession = session // FIXME NPE on rotation while alarm time picker is opened
        MyApp.LogDebug(LOG_TAG, "clicked on ${(contextMenuView.tag as Session).sessionId}")
        val context = requireContext()
        when (menuItemIndex) {
            CONTEXT_MENU_ITEM_ID_FAVORITES -> {
                session.highlight = !session.highlight
                viewModel.updateFavorStatus(session)
                sessionViewDrawer.setSessionBackground(session, contextMenuView)
                SessionViewDrawer.setSessionTextColor(session, contextMenuView)
                updateMenuItems()
            }
            CONTEXT_MENU_ITEM_ID_SET_ALARM -> {
                showAlarmTimePicker()
            }
            CONTEXT_MENU_ITEM_ID_DELETE_ALARM -> {
                viewModel.deleteAlarm(session)
                setBell(session)
                updateMenuItems()
            }
            CONTEXT_MENU_ITEM_ID_ADD_TO_CALENDAR -> {
                CalendarSharing(context).addToCalendar(session)
            }
            CONTEXT_MENU_ITEM_ID_SHARE -> {
                if (!BuildConfig.ENABLE_CHAOSFLIX_EXPORT) {
                    viewModel.share(session)
                }
            }
            CONTEXT_MENU_ITEM_ID_SHARE_TEXT -> {
                viewModel.share(session)
            }
            CONTEXT_MENU_ITEM_ID_SHARE_JSON -> {
                viewModel.shareToChaosflix(session)
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fahrplan_menu, menu)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        return if (menuItem.itemId == R.id.menu_item_refresh) {
            viewModel.requestScheduleUpdate(isUserRequest = true)
            true
        } else {
            super.onOptionsItemSelected(menuItem)
        }
    }

    private fun updateMenuItems() {
        // Toggles the icon for "add/delete favorite" or "add/delete alarm".
        // Triggers SessionDetailsFragment.onPrepareOptionsMenu to be called
        requireActivity().invalidateOptionsMenu()
    }

    override fun onCreateContextMenu(menu: ContextMenu, view: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, view, menuInfo)
        contextMenuView = view
        val session = view.tag as Session
        if (session.highlight) {
            menu.add(0, CONTEXT_MENU_ITEM_ID_FAVORITES, 0, getString(R.string.menu_item_title_unflag_as_favorite))
        } else {
            menu.add(0, CONTEXT_MENU_ITEM_ID_FAVORITES, 0, getString(R.string.menu_item_title_flag_as_favorite))
        }
        if (session.hasAlarm) {
            menu.add(0, CONTEXT_MENU_ITEM_ID_DELETE_ALARM, 2, getString(R.string.menu_item_title_delete_alarm))
        } else {
            menu.add(0, CONTEXT_MENU_ITEM_ID_SET_ALARM, 1, getString(R.string.menu_item_title_set_alarm))
        }
        menu.add(0, CONTEXT_MENU_ITEM_ID_ADD_TO_CALENDAR, 3, getString(R.string.menu_item_title_add_to_calendar))
        if (BuildConfig.ENABLE_CHAOSFLIX_EXPORT) {
            val share = menu.addSubMenu(0, CONTEXT_MENU_ITEM_ID_SHARE, 4, getString(R.string.menu_item_title_share_session))
            share.add(0, CONTEXT_MENU_ITEM_ID_SHARE_TEXT, 5, getString(R.string.menu_item_title_share_session_text))
            share.add(0, CONTEXT_MENU_ITEM_ID_SHARE_JSON, 6, getString(R.string.menu_item_title_share_session_json))
        } else {
            menu.add(0, CONTEXT_MENU_ITEM_ID_SHARE, 4, getString(R.string.menu_item_title_share_session))
        }
    }

    private inner class OnDaySelectedListener(private val numDays: Int) : OnNavigationListener {

        private var isSynthetic = true

        override fun onNavigationItemSelected(itemPosition: Int, itemId: Long): Boolean {
            if (runsAtLeastOnAndroidNougat() && isSynthetic) {
                isSynthetic = false
                return true
            }
            if (itemPosition < numDays) {
                viewModel.selectDay(itemPosition)
                return true
            }
            return false
        }

        private fun runsAtLeastOnAndroidNougat() = Build.VERSION.SDK_INT > Build.VERSION_CODES.M
    }

}
