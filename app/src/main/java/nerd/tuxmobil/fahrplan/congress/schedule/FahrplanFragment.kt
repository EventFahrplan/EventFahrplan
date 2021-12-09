package nerd.tuxmobil.fahrplan.congress.schedule

import android.content.Context
import android.content.Intent
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.MyApp
import nerd.tuxmobil.fahrplan.congress.MyApp.TASKS
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmTimePickerFragment
import nerd.tuxmobil.fahrplan.congress.calendar.CalendarSharing
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys.BUNDLE_KEY_SESSION_ALARM_DAY_INDEX
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys.BUNDLE_KEY_SESSION_ALARM_SESSION_ID
import nerd.tuxmobil.fahrplan.congress.extensions.getLayoutInflater
import nerd.tuxmobil.fahrplan.congress.extensions.isLandscape
import nerd.tuxmobil.fahrplan.congress.extensions.requireViewByIdCompat
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.net.ErrorMessage
import nerd.tuxmobil.fahrplan.congress.net.ParseResult
import nerd.tuxmobil.fahrplan.congress.net.ParseScheduleResult
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.repositories.OnSessionsChangeListener
import nerd.tuxmobil.fahrplan.congress.repositories.SessionsTransformer
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat
import nerd.tuxmobil.fahrplan.congress.sharing.SessionSharer
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc
import nerd.tuxmobil.fahrplan.congress.utils.Font
import nerd.tuxmobil.fahrplan.congress.utils.TypefaceFactory
import org.ligi.tracedroid.logging.Log
import kotlin.collections.set

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
        const val FAHRPLAN_FRAGMENT_REQUEST_CODE = 6166

        private const val CONTEXT_MENU_ITEM_ID_FAVORITES = 0
        private const val CONTEXT_MENU_ITEM_ID_SET_ALARM = 1
        private const val CONTEXT_MENU_ITEM_ID_DELETE_ALARM = 2
        private const val CONTEXT_MENU_ITEM_ID_ADD_TO_CALENDAR = 3
        private const val CONTEXT_MENU_ITEM_ID_SHARE = 4
        private const val CONTEXT_MENU_ITEM_ID_SHARE_TEXT = 5
        private const val CONTEXT_MENU_ITEM_ID_SHARE_JSON = 6

        const val FIFTEEN_MINUTES = 15
        const val BOX_HEIGHT_MULTIPLIER = 3

        private val sessionsTransformer = SessionsTransformer.createSessionsTransformer()

    }

    private lateinit var appRepository: AppRepository
    private lateinit var inflater: LayoutInflater
    private lateinit var alarmServices: AlarmServices
    private lateinit var sessionViewDrawer: SessionViewDrawer
    private lateinit var navigationMenuEntriesGenerator: NavigationMenuEntriesGenerator
    private lateinit var errorMessageFactory: ErrorMessage.Factory
    private lateinit var roomTitleTypeFace: Typeface
    private lateinit var contextMenuView: View
    private var conference: Conference? = null
    private var scrollAmountCalculator: ScrollAmountCalculator? = null
    private var onSessionClickListener: OnSessionClickListener? = null

    private var dayIndex = 1 // XML values start with 1
    private var sessionId: String? = null
    private var lastSelectedSession: Session? = null
    private var scheduleData: ScheduleData? = null

    private var displayDensityScale = 0f
    private val adapterByRoomIndex = mutableMapOf</* roomIndex */ Int, SessionViewColumnAdapter>()
    private var preserveVerticalScrollPosition = false

    private val onSessionsChangeListener: OnSessionsChangeListener = object : OnSessionsChangeListener {
        override fun onAlarmsChanged() {
            requireActivity().runOnUiThread { reloadAlarms() }
        }

        override fun onHighlightsChanged() {
            requireActivity().runOnUiThread { reloadHighlights() }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        appRepository = AppRepository
        alarmServices = AlarmServices.newInstance(context, appRepository)
        navigationMenuEntriesGenerator = NavigationMenuEntriesGenerator(dayString = getString(R.string.day), todayString = getString(R.string.today))
        onSessionClickListener = if (context is OnSessionClickListener) {
            context
        } else {
            throw IllegalStateException("$context must implement OnSessionClickListener")
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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layoutRootView = inflater.inflate(R.layout.schedule, container, false)
        val verticalScrollView = layoutRootView.requireViewByIdCompat<NestedScrollView>(R.id.verticalScrollView)
        verticalScrollView.setOnScrollChangeListener(
            OnScrollChangeListener { _, _, _, _, _ -> preserveVerticalScrollPosition = true }
        )
        return layoutRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayDensityScale = resources.displayMetrics.density

        val roomScroller = view.requireViewByIdCompat<HorizontalScrollView>(R.id.roomScroller)
        val snapScroller = view.requireViewByIdCompat<HorizontalSnapScrollView>(R.id.horizScroller)
        snapScroller.setChildScroller(roomScroller)
        roomScroller.setOnTouchListener { _, _ -> true }

        dayIndex = appRepository.readDisplayDayIndex()
        inflater = view.context.getLayoutInflater()

        val intent = requireActivity().intent
        sessionId = intent.getStringExtra(BUNDLE_KEY_SESSION_ALARM_SESSION_ID)
        if (sessionId != null) {
            MyApp.LogDebug(LOG_TAG, "Open with sessionId '$sessionId'.")
            dayIndex = intent.getIntExtra(BUNDLE_KEY_SESSION_ALARM_DAY_INDEX, dayIndex)
            MyApp.LogDebug(LOG_TAG, "dayIndex = $dayIndex")
        }
        if (MyApp.meta.numDays > 1) {
            buildNavigationMenu()
        }
    }

    private fun saveCurrentDay(day: Int) {
        appRepository.updateDisplayDayIndex(day)
    }

    override fun onResume() {
        Log.d(LOG_TAG, "FahrplanFragment#onResume")
        super.onResume()
        val activity = requireActivity()
        activity.invalidateOptionsMenu()
        val intent = activity.intent
        Log.d(LOG_TAG, "sessionId = $sessionId")
        sessionId = intent.getStringExtra(BUNDLE_KEY_SESSION_ALARM_SESSION_ID)
        // TODO Analyzing https://github.com/EventFahrplan/EventFahrplan/issues/402
        Log.d(LOG_TAG, "sessionId = $sessionId (after loading from bundle)")
        if (sessionId != null) {
            Log.d(LOG_TAG, "Open with sessionId '$sessionId'.")
            dayIndex = intent.getIntExtra(BUNDLE_KEY_SESSION_ALARM_DAY_INDEX, dayIndex)
            Log.d(LOG_TAG, "day index = $dayIndex")
            saveCurrentDay(dayIndex)
        }
        Log.d(LOG_TAG, "MyApp.task_running = ${MyApp.task_running}")
        when (MyApp.task_running) {
            TASKS.FETCH -> {
                Log.d(LOG_TAG, "fetch was pending, restart")
                if (MyApp.meta.numDays != 0) {
                    viewDay(false)
                }
            }
            TASKS.PARSE -> {
                Log.d(LOG_TAG, "parse was pending, restart")
            }
            TASKS.NONE -> {
                Log.d(LOG_TAG, "meta.getNumDays() = ${MyApp.meta.numDays}")
                if (MyApp.meta.numDays != 0) {
                    // auf jeden Fall reload, wenn mit Session ID gestartet
                    viewDay(sessionId != null)
                }
            }
            else -> {
                // Nothing to do here.
            }
        }
        if (sessionId != null && scheduleData != null) {
            val session = scheduleData!!.findSession(sessionId!!)
            if (session != null) {
                scrollTo(session)
                val sidePaneView = activity.findViewById<FragmentContainerView?>(R.id.detail)
                if (sidePaneView != null && onSessionClickListener != null) {
                    onSessionClickListener!!.onSessionClick(sessionId!!)
                }
            }
            intent.removeExtra(BUNDLE_KEY_SESSION_ALARM_SESSION_ID) // jump to given sessionId only once
        }
        if (conference != null) {
            fillTimes()
        }
        appRepository.setOnSessionsChangeListener(onSessionsChangeListener)
    }

    override fun onPause() {
        appRepository.removeOnSessionsChangeListener(onSessionsChangeListener)
        super.onPause()
    }

    /**
     * Updates the session data in the schedule view.
     *
     * The [forceReload] parameter is used to avoid unneeded data loading (see [loadSessions]) and
     * unneeded rendering of the RecyclerView columns for each room (see [addRoomColumns]).
     *
     * Use cases of [forceReload]:
     * - false: a schedule is present and re-fetching it is in progress (see [onResume])
     * - true: a [sessionId] is present in session alarm notification Intent (see [onResume])
     * - true: user chooses another day (see [chooseDay])
     * - true: parsing finished successfully AND
     *     - no schedule is present OR
     *     - the schedule version changed OR
     *     - Engelsystem shifts changed
     *   (see [onParseDone])
     * - false: when parsing finished successfully (see [onParseDone])
     */
    private fun viewDay(forceReload: Boolean) {
        Log.d(LOG_TAG, "viewDay($forceReload)")
        val layoutRoot = requireView()
        val boxHeight = getNormalizedBoxHeight()
        val horizontalScroller = layoutRoot.requireViewByIdCompat<HorizontalSnapScrollView>(R.id.horizScroller)
        horizontalScroller.scrollTo(0, 0)
        loadSessions(appRepository, dayIndex, forceReload)

        val sessionsOfDay = scheduleData!!.allSessions
        if (sessionsOfDay.isEmpty()) {
            val scheduleVersion = appRepository.readMeta().version
            val errorMessage = errorMessageFactory.getMessageForEmptySchedule(scheduleVersion)
            errorMessage.show(requireContext(), shouldShowLong = false)
        } else {
            // TODO: Move this to AppRepository and include the result in ScheduleData
            conference = Conference.ofSessions(sessionsOfDay)
            MyApp.LogDebug(LOG_TAG, "Conference = $conference")
        }

        val roomCount = scheduleData!!.roomCount
        horizontalScroller.setRoomsCount(roomCount)
        val roomScroller = layoutRoot.requireViewByIdCompat<HorizontalScrollView>(R.id.roomScroller)
        val roomTitlesRowLayout = roomScroller.getChildAt(0) as LinearLayout
        val columnWidth = horizontalScroller.columnWidth
        addRoomTitleViews(roomTitlesRowLayout, columnWidth, scheduleData!!.roomNames)
        addRoomColumns(horizontalScroller, columnWidth, scheduleData!!, forceReload)

        MainActivity.instance.shouldScheduleScrollToCurrentTimeSlot {
            if (!preserveVerticalScrollPosition) {
                scrollToCurrent(boxHeight)
                preserveVerticalScrollPosition = false
            }
            Unit
        }
        updateNavigationMenuSelection()
    }

    private fun updateNavigationMenuSelection() {
        val activity = requireActivity() as AppCompatActivity
        val actionbar = activity.supportActionBar
        Log.d(LOG_TAG, "MyApp.meta = ${MyApp.meta}")
        if (actionbar != null && MyApp.meta.numDays > 1) {
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
        scheduleData: ScheduleData,
        forceReload: Boolean
    ) {
        val columnIndexLeft = horizontalScroller.columnIndex
        val columnIndexRight = horizontalScroller.lastVisibleColumnIndex

        // whenever possible, just update recycler views
        if (!forceReload && adapterByRoomIndex.isNotEmpty()) {
            for (roomIndex in columnIndexLeft..columnIndexRight) {
                try {
                    adapterByRoomIndex[roomIndex]!!.notifyDataSetChanged()
                } catch (e: NullPointerException) {
                    // TODO Analyzing https://github.com/EventFahrplan/EventFahrplan/issues/402
                    Log.e(LOG_TAG, "adapterByRoomIndex keys = ${adapterByRoomIndex.keys}")
                    throw e
                }
            }
            return
        }
        val columnsLayout = horizontalScroller.getChildAt(0) as LinearLayout
        columnsLayout.removeAllViews()
        adapterByRoomIndex.clear()
        val boxHeight = getNormalizedBoxHeight()
        val layoutCalculator = LayoutCalculator(boxHeight)
        val context = horizontalScroller.context
        val roomDataList = scheduleData.roomDataList
        for (roomIndex in roomDataList.indices) {
            val roomData = roomDataList[roomIndex]
            val layoutParamsBySession = layoutCalculator.calculateLayoutParams(roomData, conference!!)
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
            adapterByRoomIndex[roomIndex] = adapter
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
     */
    private fun scrollToCurrent(boxHeight: Int) {
        val currentDayIndex = scheduleData!!.dayIndex
        if (sessionId != null || currentDayIndex != MyApp.dateInfos.indexOfToday) {
            return
        }
        var columnIndex = -1
        val layoutRootView = requireView()
        if (!layoutRootView.context.isLandscape()) {
            val horizontalSnapScrollView = layoutRootView.findViewById<HorizontalSnapScrollView>(R.id.horizScroller)
            columnIndex = horizontalSnapScrollView.columnIndex
            MyApp.LogDebug(LOG_TAG, "y pos = $columnIndex")
        }
        val nowMoment = Moment.now()
        val scrollAmount = scrollAmountCalculator!!.calculateScrollAmount(
            conference = conference!!,
            dateInfos = MyApp.dateInfos,
            scheduleData = scheduleData!!,
            nowMoment = nowMoment,
            currentDayIndex = currentDayIndex,
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

    private fun scrollTo(session: Session) {
        val height = getNormalizedBoxHeight()
        val pos = scrollAmountCalculator!!.calculateScrollAmount(conference!!, session, height)
        MyApp.LogDebug(LOG_TAG, "Position is $pos")
        val layoutRootView = requireView()
        layoutRootView.requireViewByIdCompat<NestedScrollView>(R.id.verticalScrollView).apply {
            post { scrollTo(0, pos) }
        }
        val horizontalSnapScrollView = layoutRootView.findViewById<HorizontalSnapScrollView?>(R.id.horizScroller)
        if (horizontalSnapScrollView != null) {
            val horizontalPos = scheduleData!!.findRoomIndex(session)
            MyApp.LogDebug(LOG_TAG, "Scroll horizontal to $horizontalPos")
            horizontalSnapScrollView.post { horizontalSnapScrollView.scrollToColumn(horizontalPos, false) }
        }
    }

    private fun chooseDay(chosenDay: Int) {
        if (chosenDay + 1 != dayIndex) {
            dayIndex = chosenDay + 1
            saveCurrentDay(dayIndex)
            preserveVerticalScrollPosition = false
            viewDay(forceReload = true)
            fillTimes()
        }
    }

    private fun fillTimes() {
        val normalizedBoxHeight = getNormalizedBoxHeight()
        val earliestSession = appRepository.loadEarliestSession()
        val firstDayStartDay = earliestSession.startTimeMoment.monthDay
        val useDeviceTimeZone = appRepository.readUseDeviceTimeZoneEnabled()
        val parameters = TimeTextViewParameter.parametersOf(
            nowMoment = Moment.now(),
            conference = conference!!,
            firstDayStartDay = firstDayStartDay,
            dayIndex = dayIndex,
            normalizedBoxHeight = normalizedBoxHeight,
            useDeviceTimeZone = useDeviceTimeZone
        )
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

    private fun loadSessions(appRepository: AppRepository, dayIndex: Int, forceReload: Boolean) {
        MyApp.LogDebug(LOG_TAG, "load sessions of dayIndex $dayIndex")
        if (!forceReload && scheduleData != null && scheduleData!!.dayIndex == dayIndex) {
            return
        }
        val sessions = appRepository.loadUncanceledSessionsForDayIndex(dayIndex)
        scheduleData = sessionsTransformer.transformSessions(dayIndex, sessions)
        scrollAmountCalculator = ScrollAmountCalculator(Logging.get())
    }

    private fun reloadAlarms() {
        if (scheduleData == null) {
            return
        }
        val alarmSessionIds = appRepository.readAlarmSessionIds()
        for (session in scheduleData!!.allSessions) {
            session.hasAlarm = alarmSessionIds.contains(session.sessionId)
        }
        refreshViews()
    }

    private fun reloadHighlights() {
        if (scheduleData == null) {
            return
        }
        val highlightSessionIds = appRepository.readHighlightSessionIds()
        for (session in scheduleData!!.allSessions) {
            session.highlight = highlightSessionIds.contains(session.sessionId)
        }
        refreshViews()
    }

    override fun onClick(view: View) {
        val session = checkNotNull(view.tag) {
            "A session must be assigned to the 'tag' attribute of the session view."
        } as Session
        MyApp.LogDebug(LOG_TAG, "Click on ${session.title}")
        onSessionClickListener?.onSessionClick(session.sessionId)
    }

    private fun buildNavigationMenu() {
        val numDays = MyApp.meta.numDays
        val dayMenuEntries = navigationMenuEntriesGenerator.getDayMenuEntries(numDays, MyApp.dateInfos).toTypedArray()
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

    fun onParseDone(result: ParseResult) {
        val activity = requireActivity()
        val lastShiftsHash = appRepository.readLastEngelsystemShiftsHash()
        val currentShiftsHash = appRepository.readEngelsystemShiftsHash()
        MyApp.LogDebug(LOG_TAG, "Shifts hash (OLD) = $lastShiftsHash")
        MyApp.LogDebug(LOG_TAG, "Shifts hash (NEW) = $currentShiftsHash")
        val shiftsChanged = currentShiftsHash != lastShiftsHash
        if (shiftsChanged) {
            appRepository.updateLastEngelsystemShiftsHash(currentShiftsHash)
        }
        if (result.isSuccess) {
            if (MyApp.meta.numDays == 0 || (result is ParseScheduleResult && result.version != MyApp.meta.version) || shiftsChanged) {
                MyApp.meta = appRepository.readMeta()
                FahrplanMisc.loadDays(appRepository)
                if (MyApp.meta.numDays > 1) {
                    buildNavigationMenu()
                }
                dayIndex = appRepository.readDisplayDayIndex()
                if (dayIndex > MyApp.meta.numDays) {
                    dayIndex = 1
                }
                viewDay(true)
                if (conference == null) {
                    Log.e(javaClass.simpleName, "Error displaying schedule. Conference is null.")
                } else {
                    fillTimes()
                }
            } else {
                viewDay(false)
            }
        } else {
            val errorMessage = errorMessageFactory.getMessageForParsingResult(result)
            errorMessage.show(requireContext(), shouldShowLong = true)
        }
        activity.invalidateOptionsMenu()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FAHRPLAN_FRAGMENT_REQUEST_CODE && resultCode == AlarmTimePickerFragment.ALERT_TIME_PICKED_RESULT_CODE && data != null) {
            val alarmTimesIndex = data.getIntExtra(AlarmTimePickerFragment.ALARM_PICKED_INTENT_KEY, 0)
            onAlarmTimesIndexPicked(alarmTimesIndex)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showAlarmTimePicker() {
        AlarmTimePickerFragment.show(this, FAHRPLAN_FRAGMENT_REQUEST_CODE)
    }

    private fun onAlarmTimesIndexPicked(alarmTimesIndex: Int) {
        if (lastSelectedSession == null) {
            Log.e(javaClass.simpleName, "onAlarmTimesIndexPicked: session: null. alarmTimesIndex: $alarmTimesIndex")
            throw NullPointerException("Session is null.")
        } else {
            alarmServices.addSessionAlarm(lastSelectedSession!!, alarmTimesIndex)
            setBell(lastSelectedSession!!)
            updateMenuItems()
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val menuItemIndex = item.itemId
        val session = contextMenuView.tag as Session
        lastSelectedSession = session
        MyApp.LogDebug(LOG_TAG, "clicked on ${(contextMenuView.tag as Session).sessionId}")
        val context = requireContext()
        when (menuItemIndex) {
            CONTEXT_MENU_ITEM_ID_FAVORITES -> {
                session.highlight = !session.highlight
                appRepository.updateHighlight(session)
                sessionViewDrawer.setSessionBackground(session, contextMenuView)
                SessionViewDrawer.setSessionTextColor(session, contextMenuView)
                updateMenuItems()
            }
            CONTEXT_MENU_ITEM_ID_SET_ALARM -> {
                showAlarmTimePicker()
            }
            CONTEXT_MENU_ITEM_ID_DELETE_ALARM -> {
                alarmServices.deleteSessionAlarm(session)
                setBell(session)
                updateMenuItems()
            }
            CONTEXT_MENU_ITEM_ID_ADD_TO_CALENDAR -> {
                CalendarSharing(context).addToCalendar(session)
            }
            CONTEXT_MENU_ITEM_ID_SHARE -> {
                if (!BuildConfig.ENABLE_CHAOSFLIX_EXPORT) {
                    val timeZoneId = appRepository.readMeta().timeZoneId
                    val formattedSession = SimpleSessionFormat().format(session, timeZoneId)
                    SessionSharer.shareSimple(context, formattedSession)
                }
            }
            CONTEXT_MENU_ITEM_ID_SHARE_TEXT -> {
                val timeZoneId = appRepository.readMeta().timeZoneId
                val formattedSession = SimpleSessionFormat().format(session, timeZoneId)
                SessionSharer.shareSimple(context, formattedSession)
            }
            CONTEXT_MENU_ITEM_ID_SHARE_JSON -> {
                val jsonFormattedSession = JsonSessionFormat().format(session)
                if (!SessionSharer.shareJson(context, jsonFormattedSession)) {
                    Toast.makeText(context, R.string.share_error_activity_not_found, Toast.LENGTH_SHORT).show()
                }
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
            // TODO Replace MainActivity reference with viewModel.
            val activity = requireActivity() as MainActivity
            activity.fetchFahrplan()
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

    private fun getSessionView(session: Session): View? {
        val verticalScrollView = requireView().findViewById<NestedScrollView?>(R.id.verticalScrollView)
            ?: return null
        return verticalScrollView.findViewWithTag(session)
    }

    private fun refreshViews() {
        if (scheduleData == null) {
            return
        }
        for (session in scheduleData!!.allSessions) {
            setBell(session)
            val sessionView = getSessionView(session)
            if (sessionView != null) {
                sessionViewDrawer.setSessionBackground(session, sessionView)
                SessionViewDrawer.setSessionTextColor(session, sessionView)
            }
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
                chooseDay(itemPosition)
                return true
            }
            return false
        }

        private fun runsAtLeastOnAndroidNougat() = Build.VERSION.SDK_INT > Build.VERSION_CODES.M
    }

}
