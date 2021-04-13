package nerd.tuxmobil.fahrplan.congress.schedule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.core.widget.NestedScrollView.OnScrollChangeListener;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.ligi.tracedroid.logging.Log;
import org.threeten.bp.ZoneId;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.metadude.android.eventfahrplan.commons.logging.Logging;
import info.metadude.android.eventfahrplan.commons.temporal.Moment;
import kotlin.Unit;
import nerd.tuxmobil.fahrplan.congress.BuildConfig;
import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmTimePickerFragment;
import nerd.tuxmobil.fahrplan.congress.calendar.CalendarSharing;
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys;
import nerd.tuxmobil.fahrplan.congress.extensions.Contexts;
import nerd.tuxmobil.fahrplan.congress.models.RoomData;
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData;
import nerd.tuxmobil.fahrplan.congress.models.Session;
import nerd.tuxmobil.fahrplan.congress.net.ParseResult;
import nerd.tuxmobil.fahrplan.congress.net.ParseScheduleResult;
import nerd.tuxmobil.fahrplan.congress.net.ParseShiftsResult;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;
import nerd.tuxmobil.fahrplan.congress.repositories.OnSessionsChangeListener;
import nerd.tuxmobil.fahrplan.congress.repositories.SessionsTransformer;
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat;
import nerd.tuxmobil.fahrplan.congress.sharing.SessionSharer;
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat;
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc;
import nerd.tuxmobil.fahrplan.congress.utils.TypefaceFactory;

import static nerd.tuxmobil.fahrplan.congress.extensions.Contexts.isLandscape;
import static nerd.tuxmobil.fahrplan.congress.extensions.ViewExtensions.requireViewByIdCompat;

public class FahrplanFragment extends Fragment implements SessionViewEventsHandler {

    private static final String LOG_TAG = "Fahrplan";

    public static final String FRAGMENT_TAG = "schedule";


    public static final int FAHRPLAN_FRAGMENT_REQUEST_CODE = 6166;

    private static final int CONTEXT_MENU_ITEM_ID_FAVORITES = 0;
    private static final int CONTEXT_MENU_ITEM_ID_SET_ALARM = 1;
    private static final int CONTEXT_MENU_ITEM_ID_DELETE_ALARM = 2;
    private static final int CONTEXT_MENU_ITEM_ID_ADD_TO_CALENDAR = 3;
    private static final int CONTEXT_MENU_ITEM_ID_SHARE = 4;
    private static final int CONTEXT_MENU_ITEM_ID_SHARE_TEXT = 5;
    private static final int CONTEXT_MENU_ITEM_ID_SHARE_JSON = 6;

    public static final int FIFTEEN_MINUTES = 15;
    public static final int BOX_HEIGHT_MULTIPLIER = 3;

    private float displayDensityScale;

    private LayoutInflater inflater;

    private Conference conference;

    private AppRepository appRepository;

    private int mDay = 1;

    private static final String[] rooms = {
            "Saal 1",
            "Saal 2",
            "Saal G",
            "Saal 6",
            "Saal 17",
            "Lounge"
    };

    private static final SessionsTransformer sessionsTransformer =
            new SessionsTransformer(() -> Arrays.asList(rooms));

    private Typeface light;

    private View contextMenuView;

    private ScheduleData scheduleData;

    private String sessionId;

    private Session lastSelectedSession;

    private SessionViewDrawer sessionViewDrawer;

    private final Map<Integer, SessionViewColumnAdapter> adapterByRoomIndex = new HashMap<>();

    private ScrollAmountCalculator scrollAmountCalculator;

    private boolean preserveVerticalScrollPosition = false;

    private final OnSessionsChangeListener onSessionsChangeListener = new OnSessionsChangeListener() {
        @Override
        public void onAlarmsChanged() {
            requireActivity().runOnUiThread(() ->
                    reloadAlarms()
            );
        }

        @Override
        public void onHighlightsChanged() {
            requireActivity().runOnUiThread(() ->
                    reloadHighlights()
            );
        }
    };

    @MainThread
    @CallSuper
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appRepository = AppRepository.INSTANCE;
    }

    @MainThread
    @CallSuper
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Context context = requireContext();
        light = TypefaceFactory.getNewInstance(context).getRobotoLight();
        sessionViewDrawer = new SessionViewDrawer(context, this::getSessionPadding);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layoutRootView = inflater.inflate(R.layout.schedule, container, false);
        NestedScrollView verticalScrollView = requireViewByIdCompat(layoutRootView, R.id.verticalScrollView);
        verticalScrollView.setOnScrollChangeListener((OnScrollChangeListener)
                (view, scrollX, scrollY, oldScrollX, oldScrollY) -> preserveVerticalScrollPosition = true);
        return layoutRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = view.getContext();
        displayDensityScale = getResources().getDisplayMetrics().density;
        HorizontalScrollView roomScroller = requireViewByIdCompat(view, R.id.roomScroller);
        HorizontalSnapScrollView snapScroller = requireViewByIdCompat(view, R.id.horizScroller);
        snapScroller.setChildScroller(roomScroller);
        roomScroller.setOnTouchListener((v, session) -> true);

        mDay = appRepository.readDisplayDayIndex();

        inflater = Contexts.getLayoutInflater(context);

        Intent intent = requireActivity().getIntent();
        sessionId = intent.getStringExtra(BundleKeys.BUNDLE_KEY_SESSION_ALARM_SESSION_ID);

        if (sessionId != null) {
            MyApp.LogDebug(LOG_TAG, "Open with sessionId '" + sessionId + "'.");
            mDay = intent.getIntExtra(BundleKeys.BUNDLE_KEY_SESSION_ALARM_DAY_INDEX, mDay);
            MyApp.LogDebug(LOG_TAG, "day " + mDay);
        }

        if (MyApp.meta.getNumDays() > 1) {
            buildNavigationMenu();
        }
    }

    private void saveCurrentDay(int day) {
        appRepository.updateDisplayDayIndex(day);
    }

    @MainThread
    @CallSuper
    @Override
    public void onResume() {
        Log.d(LOG_TAG, "onResume");
        super.onResume();
        Activity activity = requireActivity();
        activity.invalidateOptionsMenu();

        Intent intent = activity.getIntent();

        Log.d(LOG_TAG, "sessionId = " + sessionId);
        sessionId = intent.getStringExtra(BundleKeys.BUNDLE_KEY_SESSION_ALARM_SESSION_ID);

        if (sessionId != null) {
            Log.d(LOG_TAG, "Open with sessionId '" + sessionId + "'.");
            mDay = intent.getIntExtra(BundleKeys.BUNDLE_KEY_SESSION_ALARM_DAY_INDEX, mDay);
            Log.d(LOG_TAG, "day " + mDay);
            saveCurrentDay(mDay);
        }

        Log.d(LOG_TAG, "MyApp.task_running = " + MyApp.task_running);
        switch (MyApp.task_running) {
            case FETCH:
                Log.d(LOG_TAG, "fetch was pending, restart");
                if (MyApp.meta.getNumDays() != 0) {
                    viewDay(false);
                }
                break;
            case PARSE:
                Log.d(LOG_TAG, "parse was pending, restart");
                break;
            case NONE:
                Log.d(LOG_TAG, "meta.getNumDays() = " + MyApp.meta.getNumDays());
                if (MyApp.meta.getNumDays() != 0) {
                    // auf jeden Fall reload, wenn mit Session ID gestartet
                    viewDay(sessionId != null);
                }
                break;
        }

        if (sessionId != null && scheduleData != null) {
            Session session = scheduleData.findSession(sessionId);
            if (session != null) {
                scrollTo(session);
                FrameLayout sidePane = activity.findViewById(R.id.detail);
                if (sidePane != null) {
                    ((MainActivity) activity).openSessionDetails(session);
                }
            }
            intent.removeExtra(BundleKeys.BUNDLE_KEY_SESSION_ALARM_SESSION_ID); // jump to given sessionId only once
        }
        if (conference != null) {
            fillTimes();
        }

        appRepository.setOnSessionsChangeListener(onSessionsChangeListener);
    }

    @MainThread
    @CallSuper
    @Override
    public void onPause() {
        appRepository.removeOnSessionsChangeListener(onSessionsChangeListener);
        super.onPause();
    }

    private void viewDay(boolean forceReload) {
        Log.d(LOG_TAG, "viewDay(" + forceReload + ")");
        View layoutRoot = requireView();
        int boxHeight = getNormalizedBoxHeight(displayDensityScale);

        HorizontalSnapScrollView horizontalScroller = requireViewByIdCompat(layoutRoot, R.id.horizScroller);
        horizontalScroller.scrollTo(0, 0);

        loadSessions(appRepository, mDay, forceReload);
        List<Session> sessionsOfDay = scheduleData.getAllSessions();

        if (!sessionsOfDay.isEmpty()) {
            // TODO: Move this to AppRepository and include the result in ScheduleData
            conference = Conference.ofSessions(sessionsOfDay);
            MyApp.LogDebug(LOG_TAG, "Conference = " + conference);
        }

        int roomCount = scheduleData.getRoomCount();
        horizontalScroller.setRoomsCount(roomCount);

        HorizontalScrollView roomScroller = requireViewByIdCompat(layoutRoot, R.id.roomScroller);
        LinearLayout roomTitlesRowLayout = (LinearLayout) roomScroller.getChildAt(0);
        int columnWidth = horizontalScroller.getColumnWidth();
        addRoomTitleViews(roomTitlesRowLayout, columnWidth, scheduleData.getRoomNames());
        addRoomColumns(horizontalScroller, columnWidth, scheduleData, forceReload);

        MainActivity.getInstance().shouldScheduleScrollToCurrentTimeSlot(() -> {
            if (!preserveVerticalScrollPosition) {
                scrollToCurrent(boxHeight);
                preserveVerticalScrollPosition = false;
            }
            return Unit.INSTANCE;
        });

        updateNavigationMenuSelection();
    }

    private void updateNavigationMenuSelection() {
        AppCompatActivity activity = (AppCompatActivity) requireActivity();
        ActionBar actionbar = activity.getSupportActionBar();
        Log.d(LOG_TAG, "MyApp.meta = " + MyApp.meta);
        if (actionbar != null && MyApp.meta.getNumDays() > 1) {
            actionbar.setSelectedNavigationItem(mDay - 1);
        }
    }

    /**
     * Adds {@code roomCount} room column views as child views to the first child
     * (which is a row layout) of the given {@code horizontalScroller} layout.
     * Previously added child views are removed.
     */
    private void addRoomColumns(
            @NonNull HorizontalSnapScrollView horizontalScroller,
            int columnWidth,
            @NonNull ScheduleData scheduleData,
            boolean forceReload
    ) {
        int columnIndexLeft = horizontalScroller.getColumnIndex();
        int columnIndexRight = horizontalScroller.getLastVisibleColumnIndex();

        // whenever possible, just update recycler views
        if (!forceReload && !adapterByRoomIndex.isEmpty()) {
            for (int roomIndex = columnIndexLeft; roomIndex <= columnIndexRight; roomIndex++) {
                //noinspection ConstantConditions
                adapterByRoomIndex.get(roomIndex).notifyDataSetChanged();
            }
            return;
        }

        LinearLayout columnsLayout = (LinearLayout) horizontalScroller.getChildAt(0);
        columnsLayout.removeAllViews();
        adapterByRoomIndex.clear();

        int boxHeight = getNormalizedBoxHeight(displayDensityScale);
        LayoutCalculator layoutCalculator = new LayoutCalculator(boxHeight);

        Context context = horizontalScroller.getContext();
        List<RoomData> roomDataList = scheduleData.getRoomDataList();
        for (int roomIndex = 0; roomIndex < roomDataList.size(); roomIndex++) {
            RoomData roomData = roomDataList.get(roomIndex);

            Map<Session, LayoutParams> layoutParamsBySession = layoutCalculator.calculateLayoutParams(roomData, conference);

            RecyclerView columnRecyclerView = new RecyclerView(context);
            columnRecyclerView.setHasFixedSize(true);
            columnRecyclerView.setFadingEdgeLength(0);
            columnRecyclerView.setNestedScrollingEnabled(false); // enables flinging
            columnRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            columnRecyclerView.setLayoutParams(new RecyclerView.LayoutParams(columnWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            List<Session> roomSessions = roomData.getSessions();
            SessionViewColumnAdapter adapter = new SessionViewColumnAdapter(roomSessions, layoutParamsBySession, sessionViewDrawer, this);
            columnRecyclerView.setAdapter(adapter);
            adapterByRoomIndex.put(roomIndex, adapter);

            columnsLayout.addView(columnRecyclerView);
        }
    }

    /**
     * Adds room title views as child views to the given {@code roomTitlesRowLayout}.
     * Previously added child views are removed.
     */
    private void addRoomTitleViews(
            @NonNull LinearLayout roomTitlesRowLayout,
            int columnWidth,
            @NonNull List<String> roomNames
    ) {
        roomTitlesRowLayout.removeAllViews();
        int textSize = getResources().getInteger(R.integer.room_title_size);
        LinearLayout.LayoutParams params = new LayoutParams(
                columnWidth, LayoutParams.WRAP_CONTENT, 1);
        params.gravity = Gravity.CENTER;
        int paddingRight = getSessionPadding();
        Context context = roomTitlesRowLayout.getContext();
        for (String roomName : roomNames) {
            TextView roomTitle = new TextView(context);
            roomTitle.setLayoutParams(params);
            roomTitle.setMaxLines(1);
            roomTitle.setEllipsize(TextUtils.TruncateAt.END);
            roomTitle.setPadding(0, 0, paddingRight, 0);
            roomTitle.setGravity(Gravity.CENTER);
            roomTitle.setTypeface(light);
            roomTitle.setText(roomName);
            roomTitle.setTextColor(0xffffffff);
            roomTitle.setTextSize(textSize);
            roomTitlesRowLayout.addView(roomTitle);
        }
    }

    /**
     * jump to current time or session, if we are on today's session list
     */
    private void scrollToCurrent(int boxHeight) {
        if (sessionId != null) {
            return;
        }
        int currentDayIndex = scheduleData.getDayIndex();
        if (currentDayIndex != MyApp.dateInfos.getIndexOfToday()) {
            return;
        }

        int columnIndex = -1;
        View layoutRootView = requireView();
        if (!isLandscape(layoutRootView.getContext())) {
            HorizontalSnapScrollView view = layoutRootView.findViewById(R.id.horizScroller);
            columnIndex = view.getColumnIndex();
            MyApp.LogDebug(LOG_TAG, "y pos  = " + columnIndex);
        }

        Moment nowMoment = Moment.now();
        int scrollAmount = scrollAmountCalculator.calculateScrollAmount(
                conference, MyApp.dateInfos, scheduleData, nowMoment, currentDayIndex, boxHeight, columnIndex);

        final int pos = scrollAmount;
        final NestedScrollView verticalScrollView = requireViewByIdCompat(layoutRootView, R.id.verticalScrollView);
        verticalScrollView.scrollTo(0, scrollAmount);
        verticalScrollView.post(() -> verticalScrollView.scrollTo(0, pos));
    }

    private void setBell(Session session) {
        NestedScrollView verticalScrollView = requireView().findViewById(R.id.verticalScrollView);
        if (verticalScrollView == null) {
            return;
        }
        View v = verticalScrollView.findViewWithTag(session);
        if (v == null) {
            return;
        }
        ImageView bell = v.findViewById(R.id.session_bell_view);
        if (bell == null) {
            return;
        }

        if (session.hasAlarm) {
            bell.setVisibility(View.VISIBLE);
        } else {
            bell.setVisibility(View.GONE);
        }
    }

    private void scrollTo(@NonNull Session session) {
        int height = getNormalizedBoxHeight(displayDensityScale);
        int pos = scrollAmountCalculator.calculateScrollAmount(conference, session, height);
        MyApp.LogDebug(LOG_TAG, "position is " + pos);
        View layoutRootView = requireView();
        final NestedScrollView verticalScrollView = requireViewByIdCompat(layoutRootView, R.id.verticalScrollView);
        verticalScrollView.post(() -> verticalScrollView.scrollTo(0, pos));
        final HorizontalSnapScrollView horiz = layoutRootView.findViewById(R.id.horizScroller);
        if (horiz != null) {
            final int hpos = scheduleData.findRoomIndex(session);
            MyApp.LogDebug(LOG_TAG, "scroll horiz to " + hpos);
            horiz.post(() -> horiz.scrollToColumn(hpos, false));
        }
    }

    private void chooseDay(int chosenDay) {
        if (chosenDay + 1 != mDay) {
            mDay = chosenDay + 1;
            saveCurrentDay(mDay);
            preserveVerticalScrollPosition = false;
            viewDay(true);
            fillTimes();
        }
    }

    private void fillTimes() {
        int normalizedBoxHeight = getNormalizedBoxHeight(displayDensityScale);
        boolean useDeviceTimeZone = appRepository.readUseDeviceTimeZoneEnabled();
        List<TimeTextViewParameter> parameters = TimeTextViewParameter.parametersOf(
                Moment.now(),
                conference,
                BuildConfig.SCHEDULE_FIRST_DAY_START_DAY,
                mDay,
                normalizedBoxHeight,
                useDeviceTimeZone
        );
        LinearLayout timeTextColumn = requireViewByIdCompat(requireView(), R.id.times_layout);
        timeTextColumn.removeAllViews();
        View timeTextView;
        for (TimeTextViewParameter parameter : parameters) {
            timeTextView = inflater.inflate(parameter.getLayout(), null);
            timeTextColumn.addView(timeTextView, LayoutParams.MATCH_PARENT, parameter.getHeight());
            TextView title = requireViewByIdCompat(timeTextView, R.id.time);
            title.setText(parameter.getTitleText());
        }
    }

    private int getSessionPadding() {
        int factor = isLandscape(requireContext()) ? 8 : 10;
        return (int) (factor * displayDensityScale);
    }

    private int getNormalizedBoxHeight(float scale) {
        String orientationText = isLandscape(requireContext()) ? "landscape" : "other orientation";
        MyApp.LogDebug(LOG_TAG, orientationText);
        return (int) (getResources().getInteger(R.integer.box_height) * scale);
    }

    public void loadSessions(@NonNull AppRepository appRepository, int day, boolean forceReload) {
        MyApp.LogDebug(LOG_TAG, "load sessions of day " + day);

        if (!forceReload && scheduleData != null && scheduleData.getDayIndex() == day) {
            return;
        }

        List<Session> sessions = appRepository.loadUncanceledSessionsForDayIndex(day);
        scheduleData = sessionsTransformer.transformSessions(day, sessions);
        scrollAmountCalculator = new ScrollAmountCalculator(Logging.get());
    }

    private void reloadAlarms() {
        if (scheduleData == null) {
            return;
        }

        Set<String> alarmSessionIds = appRepository.readAlarmSessionIds();
        for (Session session : scheduleData.getAllSessions()) {
            session.hasAlarm = alarmSessionIds.contains(session.sessionId);
        }

        refreshViews();
    }

    private void reloadHighlights() {
        if (scheduleData == null) {
            return;
        }

        Set<String> highlightSessionIds = appRepository.readHighlightSessionIds();
        for (Session session : scheduleData.getAllSessions()) {
            session.highlight = highlightSessionIds.contains(session.sessionId);
        }

        refreshViews();
    }

    @Override
    public void onClick(View v) {
        Session session = (Session) v.getTag();
        if (session == null) {
            throw new NullPointerException("A session must be assigned to the 'tag' attribute of the session view.");
        }
        MyApp.LogDebug(LOG_TAG, "Click on " + session.title);
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.openSessionDetails(session);
    }

    public void buildNavigationMenu() {
        Moment currentDate = Moment.now().startOfDay();
        MyApp.LogDebug(LOG_TAG, "Today is " + currentDate.toUtcDateTime().toLocalDate());
        String[] dayMenuEntries = NavigationMenuEntriesGenerator.getDayMenuEntries(
                MyApp.meta.getNumDays(),
                MyApp.dateInfos,
                currentDate,
                getString(R.string.day),
                getString(R.string.today)
        );
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                actionBar.getThemedContext(),
                R.layout.support_simple_spinner_dropdown_item_large,
                dayMenuEntries);
        arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_list_item);
        actionBar.setListNavigationCallbacks(arrayAdapter, new OnDaySelectedListener());
    }

    public void onParseDone(@NonNull ParseResult result) {
        Activity activity = requireActivity();
        int lastShiftsHash = appRepository.readLastEngelsystemShiftsHash();
        int currentShiftsHash = appRepository.readEngelsystemShiftsHash();
        MyApp.LogDebug(LOG_TAG, "Shifts hash (OLD) = " + lastShiftsHash);
        MyApp.LogDebug(LOG_TAG, "Shifts hash (NEW) = " + currentShiftsHash);
        boolean shiftsChanged = currentShiftsHash != lastShiftsHash;
        if (shiftsChanged) {
            appRepository.updateLastEngelsystemShiftsHash(currentShiftsHash);
        }
        if (result.isSuccess()) {
            if (MyApp.meta.getNumDays() == 0
                    || (result instanceof ParseScheduleResult
                    && !((ParseScheduleResult) result).getVersion().equals(MyApp.meta.getVersion()))
                    || shiftsChanged
            ) {
                MyApp.meta = appRepository.readMeta();
                FahrplanMisc.loadDays(appRepository);
                if (MyApp.meta.getNumDays() > 1) {
                    buildNavigationMenu();
                }
                mDay = appRepository.readDisplayDayIndex();
                if (mDay > MyApp.meta.getNumDays()) {
                    mDay = 1;
                }
                viewDay(true);
                fillTimes();
            } else {
                viewDay(false);
            }
        } else {
            String message = getParsingErrorMessage(result);
            MyApp.LogDebug(getClass().getSimpleName(), message);
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        }
        activity.invalidateOptionsMenu();
    }

    // TODO Consolidate HTTP status code handling with CustomHttpClient.showHttpError
    private String getParsingErrorMessage(@NonNull ParseResult parseResult) {
        String message = "";
        if (parseResult instanceof ParseScheduleResult) {
            String version = ((ParseScheduleResult) parseResult).getVersion();
            if (version.isEmpty()) {
                message = getString(R.string.schedule_parsing_error_generic);
            } else {
                message = getString(R.string.schedule_parsing_error_with_version, version);
            }
        } else if (parseResult instanceof ParseShiftsResult.Error) {
            ParseShiftsResult.Error errorResult = (ParseShiftsResult.Error) parseResult;
            if (errorResult.isForbidden()) {
                message = getString(R.string.engelsystem_shifts_parsing_error_forbidden);
            } else if (errorResult.isNotFound()) {
                message = getString(R.string.engelsystem_shifts_parsing_error_not_found);
            } else {
                message = getString(R.string.engelsystem_shifts_parsing_error_generic);
            }
        } else if (parseResult instanceof ParseShiftsResult.Exception) {
            message = getString(R.string.engelsystem_shifts_parsing_error_generic);
        }
        if (message.isEmpty()) {
            throw new IllegalStateException("Unknown parsing result: " + parseResult);
        }
        return message;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == FAHRPLAN_FRAGMENT_REQUEST_CODE &&
                resultCode == AlarmTimePickerFragment.ALERT_TIME_PICKED_RESULT_CODE &&
                data != null
        ) {
            int alarmTimesIndex = data.getIntExtra(
                    AlarmTimePickerFragment.ALARM_PICKED_INTENT_KEY, 0);
            onAlarmTimesIndexPicked(alarmTimesIndex);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showAlarmTimePicker() {
        AlarmTimePickerFragment.show(this, FAHRPLAN_FRAGMENT_REQUEST_CODE);
    }

    private void onAlarmTimesIndexPicked(int alarmTimesIndex) {
        if (lastSelectedSession == null) {
            Log.e(getClass().getSimpleName(), "onAlarmTimesIndexPicked: session: null. alarmTimesIndex: " + alarmTimesIndex);
            throw new NullPointerException("Session is null.");
        }
        FahrplanMisc.addAlarm(requireContext(), appRepository, lastSelectedSession, alarmTimesIndex);
        setBell(lastSelectedSession);
        updateMenuItems();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int menuItemIndex = item.getItemId();
        Session session = (Session) contextMenuView.getTag();
        lastSelectedSession = session;

        MyApp.LogDebug(LOG_TAG, "clicked on " + ((Session) contextMenuView.getTag()).sessionId);

        Context context = requireContext();
        switch (menuItemIndex) {
            case CONTEXT_MENU_ITEM_ID_FAVORITES:
                session.highlight = !session.highlight;
                appRepository.updateHighlight(session);
                sessionViewDrawer.setSessionBackground(session, contextMenuView);
                SessionViewDrawer.setSessionTextColor(session, contextMenuView);
                ((MainActivity) context).refreshFavoriteList();
                updateMenuItems();
                break;
            case CONTEXT_MENU_ITEM_ID_SET_ALARM:
                showAlarmTimePicker();
                break;
            case CONTEXT_MENU_ITEM_ID_DELETE_ALARM:
                FahrplanMisc.deleteAlarm(context, appRepository, session);
                setBell(session);
                updateMenuItems();
                break;
            case CONTEXT_MENU_ITEM_ID_ADD_TO_CALENDAR:
                new CalendarSharing(context, session).addToCalendar();
                break;
            case CONTEXT_MENU_ITEM_ID_SHARE:
                if (BuildConfig.ENABLE_CHAOSFLIX_EXPORT) {
                    break;
                }
            case CONTEXT_MENU_ITEM_ID_SHARE_TEXT:
                ZoneId timeZoneId = appRepository.readMeta().getTimeZoneId();
                String formattedSession = SimpleSessionFormat.format(session, timeZoneId);
                SessionSharer.shareSimple(context, formattedSession);
                break;
            case CONTEXT_MENU_ITEM_ID_SHARE_JSON:
                String jsonFormattedSession = JsonSessionFormat.format(session);
                if (!SessionSharer.shareJson(context, jsonFormattedSession)) {
                    Toast.makeText(context, R.string.share_error_activity_not_found, Toast.LENGTH_SHORT).show();
                }
                break;

        }
        return true;
    }

    private void updateMenuItems() {
        // Toggles the icon for "add/delete favorite" or "add/delete alarm".
        // Triggers SessionDetailsFragment.onPrepareOptionsMenu to be called
        requireActivity().invalidateOptionsMenu();
    }

    public void onCreateContextMenu(
            @NonNull ContextMenu menu,
            @NonNull View view,
            @Nullable ContextMenuInfo menuInfo
    ) {
        super.onCreateContextMenu(menu, view, menuInfo);
        contextMenuView = view;
        Session session = (Session) view.getTag();
        if (session.highlight) {
            menu.add(0, CONTEXT_MENU_ITEM_ID_FAVORITES, 0, getString(R.string.menu_item_title_unflag_as_favorite));
        } else {
            menu.add(0, CONTEXT_MENU_ITEM_ID_FAVORITES, 0, getString(R.string.menu_item_title_flag_as_favorite));
        }
        if (session.hasAlarm) {
            menu.add(0, CONTEXT_MENU_ITEM_ID_DELETE_ALARM, 2, getString(R.string.menu_item_title_delete_alarm));
        } else {
            menu.add(0, CONTEXT_MENU_ITEM_ID_SET_ALARM, 1, getString(R.string.menu_item_title_set_alarm));
        }
        menu.add(0, CONTEXT_MENU_ITEM_ID_ADD_TO_CALENDAR, 3, getString(R.string.menu_item_title_add_to_calendar));

        if (BuildConfig.ENABLE_CHAOSFLIX_EXPORT) {
            SubMenu share = menu.addSubMenu(0, CONTEXT_MENU_ITEM_ID_SHARE, 4, getString(R.string.menu_item_title_share_session));
            share.add(0, CONTEXT_MENU_ITEM_ID_SHARE_TEXT, 5, getString(R.string.menu_item_title_share_session_text));
            share.add(0, CONTEXT_MENU_ITEM_ID_SHARE_JSON, 6, getString(R.string.menu_item_title_share_session_json));
        } else {
            menu.add(0, CONTEXT_MENU_ITEM_ID_SHARE, 4, getString(R.string.menu_item_title_share_session));
        }
    }

    private View getSessionView(Session session) {
        NestedScrollView verticalScrollView = requireView().findViewById(R.id.verticalScrollView);
        if (verticalScrollView == null) {
            return null;
        }
        return verticalScrollView.findViewWithTag(session);
    }

    private void refreshViews() {
        if (scheduleData == null) {
            return;
        }
        for (Session session : scheduleData.getAllSessions()) {
            setBell(session);
            View v = getSessionView(session);
            if (v != null) {
                sessionViewDrawer.setSessionBackground(session, v);
                SessionViewDrawer.setSessionTextColor(session, v);
            }
        }
    }

    private class OnDaySelectedListener implements ActionBar.OnNavigationListener {

        private boolean isSynthetic = true;

        @Override
        public boolean onNavigationItemSelected(int itemPosition, long itemId) {
            if (runsAtLeastOnAndroidNougat() && isSynthetic) {
                isSynthetic = false;
                return true;
            }
            if (itemPosition < MyApp.meta.getNumDays()) {
                chooseDay(itemPosition);
                return true;
            }
            return false;
        }

        private boolean runsAtLeastOnAndroidNougat() {
            return Build.VERSION.SDK_INT > Build.VERSION_CODES.M;
        }

    }

}
