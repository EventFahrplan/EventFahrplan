package nerd.tuxmobil.fahrplan.congress.schedule

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.text.TextUtils.TruncateAt
import android.view.Gravity.CENTER
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO
import android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnScrollChangedListener
import android.widget.ArrayAdapter
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.ActionBar.NAVIGATION_MODE_LIST
import androidx.appcompat.app.ActionBar.OnNavigationListener
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.size
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.core.widget.NestedScrollView.OnScrollChangeListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import info.metadude.android.eventfahrplan.commons.flow.observe
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmServices
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmTimePickerFragment
import nerd.tuxmobil.fahrplan.congress.calendar.CalendarSharing
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolver
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.getLayoutInflater
import nerd.tuxmobil.fahrplan.congress.extensions.isLandscape
import nerd.tuxmobil.fahrplan.congress.extensions.requireViewByIdCompat
import nerd.tuxmobil.fahrplan.congress.models.DateInfos
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.net.ConnectivityObserver
import nerd.tuxmobil.fahrplan.congress.net.ErrorMessage
import nerd.tuxmobil.fahrplan.congress.notifications.NotificationHelper
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.schedule.SessionInteractionType.ADD_TO_CALENDAR
import nerd.tuxmobil.fahrplan.congress.schedule.SessionInteractionType.SHARE
import nerd.tuxmobil.fahrplan.congress.schedule.SessionInteractionType.SHARE_JSON
import nerd.tuxmobil.fahrplan.congress.schedule.SessionInteractionType.SHARE_TEXT
import nerd.tuxmobil.fahrplan.congress.schedule.SessionInteractionType.TOGGLE_ALARM
import nerd.tuxmobil.fahrplan.congress.schedule.SessionInteractionType.TOGGLE_FAVORITE
import nerd.tuxmobil.fahrplan.congress.schedule.observables.TimeTextViewParameter
import nerd.tuxmobil.fahrplan.congress.sharing.SessionSharer
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatter
import nerd.tuxmobil.fahrplan.congress.utils.ContentDescriptionFormatting
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc
import nerd.tuxmobil.fahrplan.congress.utils.Font
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatter
import nerd.tuxmobil.fahrplan.congress.utils.SessionPropertiesFormatting
import nerd.tuxmobil.fahrplan.congress.utils.TypefaceFactory

class FahrplanFragment : Fragment(), MenuProvider {

    /**
     * Interface definition for a callback to be invoked when a session view is clicked.
     */
    internal fun interface OnSessionClickListener {
        /**
         * Called when the session view has been clicked.
         */
        fun onSessionClick(sessionId: String)
    }

    companion object {

        private const val LOG_TAG = "FahrplanFragment"
        const val FRAGMENT_TAG = "schedule"
        private const val FAHRPLAN_FRAGMENT_REQUEST_KEY = "FAHRPLAN_FRAGMENT_REQUEST_KEY"

        const val FIFTEEN_MINUTES = 15
        const val BOX_HEIGHT_MULTIPLIER = 3

        // Constants from LayoutCalculator
        private const val LAYOUT_CALCULATOR_DIVISOR = 5
    }

    private lateinit var postNotificationsPermissionRequestLauncher: ActivityResultLauncher<String>
    private lateinit var scheduleExactAlarmsPermissionRequestLauncher: ActivityResultLauncher<Intent>
    private lateinit var inflater: LayoutInflater
    private lateinit var errorMessageFactory: ErrorMessage.Factory
    private lateinit var connectivityObserver: ConnectivityObserver
    private lateinit var roomTitleTypeFace: Typeface
    private lateinit var viewModel: FahrplanViewModel

    private val logging = Logging.get()
    private var onHorizontalScrollChangeListener: OnScrollChangedListener? = null
    private var onSessionClickListener: OnSessionClickListener? = null
    private var lastSelectedSession: Session? = null
    private var displayDensityScale = 0f

    /**
     * Cache of the already rendered RoomData
     * Used to redraw only the rooms that visually change (e.g. because a session is favored)
     */
    private val renderedRoomHashByRoomName = mutableMapOf<String, Int>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val notificationHelper = NotificationHelper(context)
        val appRepository = AppRepository
        val alarmServices = AlarmServices.newInstance(context, appRepository)
        val menuEntriesGenerator = NavigationMenuEntriesGenerator(dayString = getString(R.string.day), todayString = getString(R.string.today))
        val defaultEngelsystemRoomName = AppRepository.ENGELSYSTEM_ROOM_NAME
        val customEngelsystemRoomName = getString(R.string.engelsystem_alias)
        val viewModelFactory = FahrplanViewModelFactory(
            repository = appRepository,
            alarmServices = alarmServices,
            notificationHelper = notificationHelper,
            navigationMenuEntriesGenerator = menuEntriesGenerator,
            defaultEngelsystemRoomName = defaultEngelsystemRoomName,
            customEngelsystemRoomName = customEngelsystemRoomName
        )
        viewModel = ViewModelProvider(this, viewModelFactory)[FahrplanViewModel::class.java]
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

        postNotificationsPermissionRequestLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.addAlarmWithChecks()
            } else {
                showMissingPostNotificationsPermissionError()
            }
        }

        scheduleExactAlarmsPermissionRequestLauncher =
            registerForActivityResult(StartActivityForResult()) { result ->
                // User granted the permission earlier.
                if (result.resultCode == RESULT_OK) {
                    viewModel.addAlarmWithChecks()
                } else {
                    // User granted the permission for the first time.
                    // Screen is resumed with RESULT_CANCELED, no indication
                    // of whether the permission was granted or not.
                    // Hence the following ugly view model bypass.
                    if (viewModel.canAddAlarms()) {
                        viewModel.addAlarmWithChecks()
                    } else {
                        showMissingScheduleExactAlarmsPermissionError()
                    }
                }
            }

        requireActivity().addMenuProvider(this, this, RESUMED)
        val context = requireContext()
        roomTitleTypeFace = TypefaceFactory.getNewInstance(context).getTypeface(Font.Roboto.Light)
        errorMessageFactory = ErrorMessage.Factory(context)
        connectivityObserver = ConnectivityObserver(context, onConnectionAvailable = {
            logging.d(LOG_TAG, "Network is available.")
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

        onHorizontalScrollChangeListener = OnScrollChangedListener {
            if (getView() != null) {
                updateHorizontalScrollingProgressLine(snapScroller.scrollX)
            }
        }
        snapScroller.viewTreeObserver.addOnScrollChangedListener(onHorizontalScrollChangeListener)

        inflater = view.context.getLayoutInflater()
    }

    override fun onDestroyView() {
        val snapScroller = requireView().requireViewByIdCompat<HorizontalSnapScrollView>(R.id.horizScroller)
        snapScroller.viewTreeObserver.removeOnScrollChangedListener(onHorizontalScrollChangeListener)
        onHorizontalScrollChangeListener = null
        super.onDestroyView()
    }

    @SuppressLint("InlinedApi")
    private fun observeViewModel() {
        viewModel.fahrplanParameter
            .observe(this) { (scheduleData, useDeviceTimeZone) ->
                hideNoScheduleView()
                viewModel.fillTimes(Moment.now(), getNormalizedBoxHeight())
                viewDay(scheduleData, useDeviceTimeZone)
                updateHorizontalScrollingProgressLine(0)
            }
        viewModel.dayMenuParameter.observe(this) { parameter ->
            buildNavigationMenu(parameter.dayMenuEntries)
            updateNavigationMenuSelection(numDays = parameter.dayMenuEntries.size, dayIndex = parameter.displayDayIndex)
        }
        viewModel.showHorizontalScrollingProgressLine.observe(this) { shouldShow ->
            updateHorizontalScrollingProgressLine(shouldShow)
        }
        viewModel.fahrplanEmptyParameter.observe(viewLifecycleOwner) { (scheduleVersion) ->
            val errorMessage = errorMessageFactory.getMessageForEmptySchedule(scheduleVersion)
            errorMessage.show(requireContext(), shouldShowLong = false)
        }
        viewModel.activateScheduleUpdateAlarm.observe(viewLifecycleOwner) { conferenceTimeFrame ->
            FahrplanMisc.setUpdateAlarm(
                context = requireContext(),
                conferenceTimeFrame = conferenceTimeFrame,
                isInitial = false,
                logging = logging,
                onCancelScheduleNextFetch = AppRepository::deleteScheduleNextFetch,
                onUpdateScheduleNextFetch = AppRepository::updateScheduleNextFetch,
            )
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
        viewModel.timeTextViewParameters.observe(this) { timeTextViewParameters ->
            fillTimes(timeTextViewParameters)
        }
        viewModel.scrollToCurrentSessionParameter.observe(viewLifecycleOwner) { (scheduleData, dateInfos) ->
            val boxHeight = getNormalizedBoxHeight()
            scrollToCurrent(boxHeight, scheduleData, dateInfos)
        }
        viewModel.scrollToSessionParameter.observe(viewLifecycleOwner) { (sessionId, verticalPosition, roomIndex) ->
            scrollTo(sessionId, verticalPosition, roomIndex)
        }
        viewModel.requestPostNotificationsPermission.observe(viewLifecycleOwner) {
            postNotificationsPermissionRequestLauncher.launch(POST_NOTIFICATIONS)
        }
        viewModel.notificationsDisabled.observe(viewLifecycleOwner) {
            showNotificationsDisabledError()
        }
        viewModel.requestScheduleExactAlarmsPermission.observe(viewLifecycleOwner) {
            val intent = Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                .setData("package:${BuildConfig.APPLICATION_ID}".toUri())
            scheduleExactAlarmsPermissionRequestLauncher.launch(intent)
        }
        viewModel.showAlarmTimePicker.observe(viewLifecycleOwner) {
            showAlarmTimePicker()
        }
    }

    override fun onResume() {
        super.onResume()
        val activity = requireActivity()
        activity.invalidateOptionsMenu()
        val intent = activity.intent
        // TODO Consume session alarm in MainActivity and let it orchestrate its fragments
        val sessionId = intent.getStringExtra(BundleKeys.SESSION_ALARM_SESSION_ID)
        if (sessionId != null) {
            val sessionAlarmDayIndex = intent.getIntExtra(BundleKeys.SESSION_ALARM_DAY_INDEX, -1)
            viewModel.saveSelectedDayIndex(sessionAlarmDayIndex)
            viewModel.scrollToSession(sessionId, getNormalizedBoxHeight())
            intent.removeExtra(BundleKeys.SESSION_ALARM_SESSION_ID) // jump to given sessionId only once
        }
    }

    override fun onDestroy() {
        connectivityObserver.stop()
        super.onDestroy()
    }

    private fun updateHorizontalScrollingProgressLine(scrollX: Int) {
        val roomNameView = requireView().requireViewByIdCompat<LinearLayout>(R.id.roomNameLandscape)
        val horizontalScrollView = requireView().requireViewByIdCompat<HorizontalSnapScrollView>(R.id.horizScroller)
        val lineView = requireView().requireViewByIdCompat<View>(R.id.horizontalScrollingProgressLine)

        // Get the width of the content inside the scrollView and the width of the scrollView itself
        val contentWidth = horizontalScrollView.getChildAt(0).width
        val roomNameViewWidth = roomNameView.width

        // Calculate the scrollable width (total content width minus the visible part of the scrollView)
        val scrollableWidth = contentWidth - roomNameViewWidth

        if (scrollableWidth > 0) {
            val maxTranslationX = roomNameViewWidth - lineView.width
            val scrollRatio = scrollX.toFloat() / scrollableWidth
            val newPosition = scrollRatio * maxTranslationX
            lineView.translationX = newPosition
        } else {
            lineView.translationX = 0f
        }
    }

    private fun updateHorizontalScrollingProgressLine(visible: Boolean) {
        val lineView = requireView().requireViewByIdCompat<View>(R.id.horizontalScrollingProgressLine)
        lineView.isInvisible = !visible
    }

    /**
     * Updates the session data in the schedule view.
     */
    private fun viewDay(scheduleData: ScheduleData, useDeviceTimeZone: Boolean) {
        val layoutRoot = requireView()
        val horizontalScroller = layoutRoot.requireViewByIdCompat<HorizontalSnapScrollView>(R.id.horizScroller)
        horizontalScroller.scrollTo(0, 0)
        val roomCount = scheduleData.roomCount
        horizontalScroller.setRoomsCount(roomCount)

        // Clear the room hash cache when the day changes to ensure fresh data is loaded
        // fixes https://github.com/EventFahrplan/EventFahrplan/issues/767
        renderedRoomHashByRoomName.clear()

        val roomScroller = layoutRoot.requireViewByIdCompat<HorizontalScrollView>(R.id.roomScroller)
        roomScroller.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        val roomTitlesRowLayout = roomScroller.getChildAt(0) as LinearLayout
        val columnWidth = horizontalScroller.columnWidth
        addRoomTitleViews(roomTitlesRowLayout, columnWidth, scheduleData.roomNames)
        addRoomColumns(horizontalScroller, columnWidth, scheduleData, useDeviceTimeZone)

        MainActivity.instance.shouldScheduleScrollToCurrentTimeSlot {
            if (!viewModel.preserveVerticalScrollPosition) {
                viewModel.scrollToCurrentSession()
                viewModel.preserveVerticalScrollPosition = false
            }
        }
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
        scheduleData: ScheduleData,
        useDeviceTimeZone: Boolean,
    ) {
        val columnsLayout = horizontalScroller.getChildAt(0) as LinearLayout
        val context = horizontalScroller.context
        val roomDataList = scheduleData.roomDataList

        // Calculate layout parameters for all sessions using LayoutCalculator
        val conference = Conference.ofSessions(scheduleData.allSessions)
        val layoutCalculator = LayoutCalculator(standardHeight = getNormalizedBoxHeight())

        val contentDescriptionFormatter = ContentDescriptionFormatter(ResourceResolver(context))
        val sessionPropertiesFormatter = SessionPropertiesFormatter(ResourceResolver(context))
        val isAlternativeHighlightingEnabled = AppRepository.readAlternativeHighlightingEnabled()

        for (roomIndex in roomDataList.indices) {
            val roomData = roomDataList[roomIndex]
            if (renderedRoomHashByRoomName[roomData.roomName] == roomData.hashCode()) continue

            val layoutParamsBySession = layoutCalculator.calculateLayoutParams(roomData, conference)

            // Prepare the room column data with all necessary conversions done up front
            val roomColumnData = prepareRoomColumnData(
                context = context,
                sessions = roomData.sessions,
                layoutParamsBySession = layoutParamsBySession,
                useDeviceTimeZone = useDeviceTimeZone,
                isAlternativeHighlightingEnabled = isAlternativeHighlightingEnabled,
                contentDescriptionFormatter = contentDescriptionFormatter,
                sessionPropertiesFormatter = sessionPropertiesFormatter,
                defaultBackgroundColorByTrackName = TrackBackgrounds.getTrackNameBackgroundColorDefaultPairs(context),
                highlightBackgroundColorByTrackName = TrackBackgrounds.getTrackNameBackgroundColorHighlightPairs(context),
            )

            val roomColumnView = ComposeView(context).apply {
                layoutParams = LayoutParams(columnWidth, WRAP_CONTENT)
                setContent {
                    RoomColumn(
                        columnData = roomColumnData,
                        onSessionClick = { sessionId ->
                            val session = roomData.sessions.first { it.sessionId == sessionId }
                            logging.d(LOG_TAG, """Click on: "${session.title}"""")
                            onSessionClickListener?.onSessionClick(session.sessionId)
                        },
                        onSessionInteraction = { sessionId, interactionType ->
                            val session = roomData.sessions.first { it.sessionId == sessionId }
                            handleSessionInteraction(session, interactionType)
                        }
                    )
                }
            }

            if (columnsLayout.size > roomIndex) {
                columnsLayout.removeViewAt(roomIndex)
            }
            columnsLayout.addView(roomColumnView, roomIndex)
            renderedRoomHashByRoomName[roomData.roomName] = roomData.hashCode()
        }
    }

    private fun handleSessionInteraction(session: Session, sessionInteractionType: SessionInteractionType) =
        when (sessionInteractionType) {
            TOGGLE_FAVORITE -> {
                val updatedSession = session.copy(isHighlight = !session.isHighlight)
                viewModel.updateFavorStatus(updatedSession)
                updateMenuItems()
            }

            TOGGLE_ALARM -> {
                lastSelectedSession = session // FIXME NPE on rotation while alarm time picker is opened
                when (session.hasAlarm) {
                    true -> viewModel.deleteAlarm(session)
                    false -> viewModel.addAlarmWithChecks()
                }
                updateMenuItems()
            }

            ADD_TO_CALENDAR -> {
                CalendarSharing(requireContext()).addToCalendar(session)
            }

            SHARE -> {
                if (!BuildConfig.ENABLE_CHAOSFLIX_EXPORT) {
                    viewModel.share(session)
                } else {
                    // Handled by submenu
                }
            }

            SHARE_TEXT -> {
                viewModel.share(session)
            }

            SHARE_JSON -> {
                viewModel.shareToChaosflix(session)
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
        val titleTextColor = ContextCompat.getColor(context, R.color.schedule_room_name_header_text)
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
        }
        val nowMoment = Moment.now()
        val conference = Conference.ofSessions(scheduleData.allSessions)
        val scrollAmount = ScrollAmountCalculator(logging).calculateScrollAmount(
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

    private fun scrollTo(sessionId: String, verticalPosition: Int, roomIndex: Int) {
        val layoutRootView = requireView()
        layoutRootView.requireViewByIdCompat<NestedScrollView>(R.id.verticalScrollView).apply {
            post { scrollTo(0, verticalPosition) }
        }
        val horizontalSnapScrollView = layoutRootView.findViewById<HorizontalSnapScrollView?>(R.id.horizScroller)
        horizontalSnapScrollView?.post { horizontalSnapScrollView.scrollToColumn(roomIndex, fast = false) }
        val activity = requireActivity()
        val sidePaneView = activity.findViewById<FragmentContainerView?>(R.id.detail)
        if (sidePaneView != null && onSessionClickListener != null) {
            onSessionClickListener!!.onSessionClick(sessionId)
        }
    }

    private fun fillTimes(parameters: List<TimeTextViewParameter>) {
        val timeTextColumn = requireView().requireViewByIdCompat<LinearLayout>(R.id.times_layout)
        timeTextColumn.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        timeTextColumn.removeAllViews()
        val timeLinesLayout = requireView().requireViewByIdCompat<LinearLayout>(R.id.schedule_horizontal_times_lines_layout)
        timeLinesLayout.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        timeLinesLayout.removeAllViews()
        var timeTextView: View
        var timeLineView: View
        for ((height, titleText, isNow) in parameters) {
            timeTextView = inflater.inflate(R.layout.schedule_time_column_time_text, null)
            timeTextColumn.addView(timeTextView, MATCH_PARENT, height)
            timeLineView = inflater.inflate(R.layout.schedule_horizontal_time_line, null)
            timeLinesLayout.addView(timeLineView, MATCH_PARENT, height)
            val textColorRes = if (isNow) R.color.schedule_time_column_item_text_emphasized else R.color.schedule_time_column_item_text_normal
            val textColor = ContextCompat.getColor(timeTextView.context, textColorRes)
            timeTextView.requireViewByIdCompat<TextView>(R.id.schedule_time_column_time_text_view).apply {
                text = titleText
                setTextColor(textColor)
                if (isNow) {
                    setBackgroundColor(ContextCompat.getColor(timeTextView.context, R.color.schedule_time_column_item_background_emphasized))
                } else {
                    setBackgroundResource(R.drawable.schedule_time_column_time_text_background_normal)
                }
            }
        }
    }

    private val sessionPadding: Int
        get() {
            val factor = if (requireContext().isLandscape()) 8 else 10
            return (factor * displayDensityScale).toInt()
        }

    private fun getNormalizedBoxHeight(): Int {
        return resources.getDimension(R.dimen.schedule_time_column_box_height).toInt()
    }

    private fun calculateSessionHeight(session: Session): Int {
        // This uses the same formula as LayoutCalculator.calculateDisplayDistance
        return standardHeight * session.duration.toWholeMinutes().toInt() / LAYOUT_CALCULATOR_DIVISOR
    }

    private val standardHeight: Int
        get() = getNormalizedBoxHeight()

    /**
     * Builds the navigation menu for switching between days.
     * The [dayMenuEntries] can be passed both as an empty list or a list with entries.
     * The empty list is important for [updateNavigationMenuSelection] to work correctly.
     */
    private fun buildNavigationMenu(dayMenuEntries: List<String>) {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar!!.navigationMode = NAVIGATION_MODE_LIST
        val arrayAdapter = ArrayAdapter(
            actionBar.themedContext,
            R.layout.support_simple_spinner_dropdown_item_large,
            dayMenuEntries
        )
        arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_list_item)
        actionBar.setListNavigationCallbacks(arrayAdapter, OnDaySelectedListener(dayMenuEntries.size))
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
            logging.e(LOG_TAG, "onAlarmTimesIndexPicked: session: null. alarmTimesIndex: $alarmTimesIndex")
            throw MissingLastSelectedSessionException(alarmTimesIndex)
        } else {
            viewModel.addAlarm(lastSelectedSession!!, alarmTimesIndex)
            updateMenuItems()
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.fahrplan_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_item_refresh -> viewModel.requestScheduleUpdate(isUserRequest = true)
            else -> return false
        }
        return true
    }

    private fun updateMenuItems() {
        // Toggles the icon for "add/delete favorite" or "add/delete alarm".
        // Triggers SessionDetailsFragment.onPrepareOptionsMenu to be called
        requireActivity().invalidateOptionsMenu()
    }

    private fun showMissingPostNotificationsPermissionError() {
        Toast.makeText(requireContext(), R.string.alarms_disabled_notifications_permission_missing, Toast.LENGTH_LONG).show()
    }

    private fun showNotificationsDisabledError() {
        Toast.makeText(requireContext(), R.string.alarms_disabled_notifications_are_disabled, Toast.LENGTH_LONG).show()
    }

    private fun showMissingScheduleExactAlarmsPermissionError() {
        Toast.makeText(requireContext(), R.string.alarms_disabled_schedule_exact_alarm_permission_missing, Toast.LENGTH_LONG).show()
    }

    private fun hideNoScheduleView() {
        requireView()
            .requireViewByIdCompat<View>(R.id.schedule_no_content_view)
            .isVisible = false
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

    private fun prepareRoomColumnData(
        context: Context,
        sessions: List<Session>,
        layoutParamsBySession: Map<String, LinearLayout.LayoutParams>,
        useDeviceTimeZone: Boolean,
        isAlternativeHighlightingEnabled: Boolean,
        contentDescriptionFormatter: ContentDescriptionFormatting,
        sessionPropertiesFormatter: SessionPropertiesFormatting,
        defaultBackgroundColorByTrackName: Map<String?, Int>,
        highlightBackgroundColorByTrackName: Map<String?, Int>,
    ): RoomColumnData {
        // Prepare session data and spacings
        val sessionDataList = mutableListOf<SessionCardData>()
        val spacings = mutableListOf<Int>()

        // Calculate initial spacing
        val firstSession = sessions.firstOrNull()
        if (firstSession != null) {
            val firstParams = layoutParamsBySession[firstSession.sessionId]
            val topMargin = firstParams?.topMargin ?: 0
            spacings.add((topMargin / context.resources.displayMetrics.density).toInt())
        }

        // Process each session
        sessions.forEachIndexed { index, session ->
            val layoutParams = layoutParamsBySession[session.sessionId]

            val backgroundColorResId = if (session.isHighlight) {
                highlightBackgroundColorByTrackName[session.track]
                    ?: R.color.track_background_highlight
            } else {
                defaultBackgroundColorByTrackName[session.track] ?: R.color.track_background_default
            }

            val textColorResId = if (session.isHighlight)
                R.color.session_item_text_on_highlight_background
            else
                R.color.session_item_text_on_default_background

            val horizontalPadding = sessionPadding
            val verticalPadding = (horizontalPadding * 0.3).toInt()

            val heightPx = layoutParams?.height ?: calculateSessionHeight(session)
            val heightDp = (heightPx / context.resources.displayMetrics.density).toInt()
            val showBorder = session.isHighlight && isAlternativeHighlightingEnabled
            val shortSession = session.duration.toWholeMinutes() <= Duration.ofMinutes(15).toWholeMinutes()

            val titleContentDescription = contentDescriptionFormatter
                .getTitleContentDescription(session.title)
            val subtitleContentDescription = contentDescriptionFormatter
                .getSubtitleContentDescription(session.subtitle)
            val speakerNamesText = sessionPropertiesFormatter.getFormattedSpeakers(session)
            val speakerNamesContentDescription = contentDescriptionFormatter
                .getSpeakersContentDescription(session.speakers.size, speakerNamesText)
            val languagesText = sessionPropertiesFormatter.getLanguageText(session)
            val languagesContentDescription = contentDescriptionFormatter
                .getLanguageContentDescription(session.language)
            val trackNameContentDescription = contentDescriptionFormatter
                .getTrackNameContentDescription(session.track)
            val stateContentDescription = contentDescriptionFormatter
                .getStateContentDescription(session, useDeviceTimeZone)

            @Suppress("KotlinConstantConditions")
            val sessionData = SessionCardData(
                sessionId = session.sessionId,
                title = SessionProperty(
                    value = session.title,
                    contentDescription = titleContentDescription,
                    maxLines = if (shortSession) 1 else 2,
                ),
                subtitle = SessionProperty(
                    value = session.subtitle,
                    contentDescription = subtitleContentDescription,
                ),
                speakerNames = SessionProperty(
                    value = speakerNamesText,
                    contentDescription = speakerNamesContentDescription,
                ),
                languages = SessionProperty(
                    value = languagesText,
                    contentDescription = languagesContentDescription,
                ),
                trackName = SessionProperty(
                    value = session.track,
                    contentDescription = trackNameContentDescription,
                ),
                recordingOptOut = SessionProperty(
                    value = session.recordingOptOut,
                    contentDescription = context.getString(R.string.session_item_no_video_content_description),
                ),
                stateContentDescription = stateContentDescription,
                innerHorizontalPadding = horizontalPadding / context.resources.displayMetrics.density,
                innerVerticalPadding = verticalPadding / context.resources.displayMetrics.density,
                cardHeight = heightDp,
                isFavored = session.isHighlight,
                hasAlarm = session.hasAlarm,
                showBorder = showBorder,
                shouldShowShareSubMenu = BuildConfig.ENABLE_CHAOSFLIX_EXPORT,
                backgroundColor = backgroundColorResId,
                textColor = textColorResId,
            )
            sessionDataList.add(sessionData)

            // Append spacing to the session except for the last one
            if (index < sessions.size - 1) {
                val bottomMargin = layoutParams?.bottomMargin ?: 0
                spacings.add((bottomMargin / context.resources.displayMetrics.density).toInt())
            }
        }

        return RoomColumnData(
            sessionData = sessionDataList,
            spacings = spacings,
        )
    }

}

private class MissingLastSelectedSessionException(alarmTimesIndex: Int) : NullPointerException(
    "Last selected session is null for alarm times index = $alarmTimesIndex."
)
