package nerd.tuxmobil.fahrplan.congress.schedule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.ligi.tracedroid.logging.Log;
import org.threeten.bp.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import info.metadude.android.eventfahrplan.commons.logging.Logging;
import info.metadude.android.eventfahrplan.commons.temporal.Moment;
import kotlin.Unit;
import nerd.tuxmobil.fahrplan.congress.BuildConfig;
import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmTimePickerFragment;
import nerd.tuxmobil.fahrplan.congress.calendar.CalendarSharing;
import nerd.tuxmobil.fahrplan.congress.extensions.Contexts;
import nerd.tuxmobil.fahrplan.congress.models.Alarm;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;
import nerd.tuxmobil.fahrplan.congress.net.ParseResult;
import nerd.tuxmobil.fahrplan.congress.net.ParseScheduleResult;
import nerd.tuxmobil.fahrplan.congress.net.ParseShiftsResult;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;
import nerd.tuxmobil.fahrplan.congress.sharing.JsonLectureFormat;
import nerd.tuxmobil.fahrplan.congress.sharing.LectureSharer;
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleLectureFormat;
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc;

import static kotlin.collections.CollectionsKt.single;
import static nerd.tuxmobil.fahrplan.congress.extensions.Resource.getNormalizedBoxHeight;

public class FahrplanFragment extends Fragment implements LectureViewEventsHandler {

    public interface OnRefreshEventMarkers {

        void refreshEventMarkers();
    }

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

    private static final int ONE_DAY = (int) Duration.ofDays(1).toMinutes();
    private static final int FIFTEEN_MINUTES = 15;

    private float scale;

    private LayoutInflater inflater;

    private final Conference conference = new Conference();

    private AppRepository appRepository;

    private int mDay = 1;

    public static Context context = null;

    public static final String[] rooms = {
            "Saal 1",
            "Saal 2",
            "Saal G",
            "Saal 6",
            "Saal 17",
            "Lounge"
    };

    private Typeface light;

    private View contextMenuView;

    private int roomColumnWidth;
    private int maxRoomColumnsVisible;

    private String lectureId;        // started with lectureId

    private Lecture lastSelectedLecture;

    private LectureViewDrawer lectureViewDrawer;

    private Map<Integer, LectureViewColumnAdapter> adapterByRoomIndex = new HashMap<>();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        appRepository = AppRepository.INSTANCE;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        context = requireContext();
        light = Typeface.createFromAsset(
                context.getAssets(), "Roboto-Light.ttf");
        lectureViewDrawer = new LectureViewDrawer(context, this, this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = view.getContext();
        scale = getResources().getDisplayMetrics().density;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        MyApp.LogDebug(LOG_TAG, "screen width = " + screenWidth);
        MyApp.LogDebug(LOG_TAG, "time width " + getResources().getDimension(R.dimen.time_width));
        screenWidth -= getResources().getDimension(R.dimen.time_width);
        maxRoomColumnsVisible = HorizontalSnapScrollView.calcMaxCols(getResources(), screenWidth);
        MyApp.LogDebug(LOG_TAG, "max cols: " + maxRoomColumnsVisible);
        roomColumnWidth = (int) ((float) screenWidth / maxRoomColumnsVisible); // TODO Assignment might be obsolete. Remove if verified.
        HorizontalScrollView roomScroller =
                view.findViewById(R.id.roomScroller);
        if (roomScroller != null) {
            HorizontalSnapScrollView snapScroller =
                    view.findViewById(R.id.horizScroller);
            if (snapScroller != null) {
                snapScroller.setChildScroller(roomScroller);
            }
            roomScroller.setOnTouchListener((v, event) -> true);
        }

        mDay = appRepository.readDisplayDayIndex();

        inflater = Contexts.getLayoutInflater(context);

        Intent intent = requireActivity().getIntent();
        lectureId = intent.getStringExtra("lecture_id");

        if (lectureId != null) {
            MyApp.LogDebug(LOG_TAG, "Open with lectureId " + lectureId);
            mDay = intent.getIntExtra("day", mDay);
            MyApp.LogDebug(LOG_TAG, "day " + mDay);
        }

        if (MyApp.meta.getNumDays() > 1) {
            buildNavigationMenu();
        }
    }

    private void saveCurrentDay(int day) {
        appRepository.updateDisplayDayIndex(day);
    }

    @Override
    public void onResume() {
        Log.d(LOG_TAG, "onResume");
        if (MyApp.lectureList == null) {
            Log.d(LOG_TAG, "MyApp.lectureList = " + null);
        } else {
            if (MyApp.lectureList.isEmpty()) {
                Log.d(LOG_TAG, "MyApp.lectureList is empty");
            } else {
                Log.d(LOG_TAG, "MyApp.lectureList contains " + MyApp.lectureList.size() + " items.");
            }
        }
        super.onResume();
        Activity activity = requireActivity();
        activity.invalidateOptionsMenu();

        Intent intent = activity.getIntent();

        Log.d(LOG_TAG, "lectureId = " + lectureId);
        lectureId = intent.getStringExtra("lecture_id");

        if (lectureId != null) {
            Log.d(LOG_TAG, "Open with lectureId " + lectureId);
            mDay = intent.getIntExtra("day", mDay);
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
                    // auf jeden Fall reload, wenn mit Lecture ID gestartet
                    viewDay(lectureId != null);
                }
                break;
        }

        if (lectureId != null) {
            Lecture lecture = single(MyApp.lectureList, currentLecture -> lectureId.equals(currentLecture.lectureId));
            scrollTo(lecture);
            FrameLayout sidePane = activity.findViewById(R.id.detail);
            if (sidePane != null) {
                ((MainActivity) activity).openLectureDetail(lecture, mDay, false);
            }
            intent.removeExtra("lecture_id");   // jump to given lectureId only once
        }
        fillTimes();
    }

    private void viewDay(boolean forceReload) {
        Log.d(LOG_TAG, "viewDay(" + forceReload + ")");
        View layoutRoot = getView();
        int boxHeight = getNormalizedBoxHeight(getResources(), scale, LOG_TAG);

        HorizontalSnapScrollView horizontalScroller = layoutRoot.findViewById(R.id.horizScroller);
        if (horizontalScroller != null) {
            horizontalScroller.scrollTo(0, 0);
        }

        loadLectureList(appRepository, mDay, forceReload);
        List<Lecture> lecturesOfDay = MyApp.lectureList;

        if (lecturesOfDay != null) {
            if (!lecturesOfDay.isEmpty()) {
                conference.calculateTimeFrame(lecturesOfDay, dateUTC -> new Moment(dateUTC).getMinuteOfDay());
                MyApp.LogDebug(LOG_TAG, "Conference = " + conference);
            }
            if (horizontalScroller != null) {
                if (horizontalScroller.getColumnWidth() != 0) {
                    // update pre-calculated roomColumnWidth with actual layout
                    roomColumnWidth = horizontalScroller.getColumnWidth();
                }
                addRoomColumns(horizontalScroller, lecturesOfDay, forceReload);
            }
        }

        HorizontalScrollView roomScroller = layoutRoot.findViewById(R.id.roomScroller);
        if (roomScroller != null) {
            addRoomTitleViews(roomScroller);
        }

        MainActivity.getInstance().shouldScheduleScrollToCurrentTimeSlot(() -> {
            scrollToCurrent(mDay, boxHeight);
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

    private void addRoomColumns(@NonNull HorizontalSnapScrollView horizontalScroller, @NonNull List<Lecture> lectures, boolean forceReload) {
        int columnIndexLeft = horizontalScroller.getColumn();
        int columnIndexRight = columnIndexLeft + maxRoomColumnsVisible;

        // whenever possible, just update recycler views
        if (!forceReload && !adapterByRoomIndex.isEmpty()) {
            for (int columnIndex = columnIndexLeft; columnIndex <= columnIndexRight; columnIndex++) {
                int roomIndex = MyApp.roomList.get(columnIndex);
                //noinspection ConstantConditions
                adapterByRoomIndex.get(roomIndex).notifyDataSetChanged();
            }
            return;
        }

        LinearLayout columnsLayout = (LinearLayout) horizontalScroller.getChildAt(0);
        columnsLayout.removeAllViews();
        adapterByRoomIndex.clear();

        int boxHeight = getNormalizedBoxHeight(getResources(), scale, LOG_TAG);
        LecturesByRoomIndex lecturesByRoomIndex = new LecturesByRoomIndex(lectures);
        LayoutCalculator layoutCalculator = new LayoutCalculator(Logging.Companion.get(), boxHeight);

        for (int columnIndex = 0; columnIndex < MyApp.roomCount; columnIndex++) {
            int roomIndex = MyApp.roomList.get(columnIndex);
            List<Lecture> roomLectures = lecturesByRoomIndex.get(roomIndex);

            Map<Lecture, LayoutParams> layoutParamsByLecture = layoutCalculator.calculateLayoutParams(roomIndex, roomLectures, conference);

            RecyclerView columnRecyclerView = new RecyclerView(context);
            columnRecyclerView.setHasFixedSize(true);
            columnRecyclerView.setFadingEdgeLength(0);
            columnRecyclerView.setNestedScrollingEnabled(false); // enables flinging
            columnRecyclerView.setLayoutManager(new LinearLayoutManager(context));
            columnRecyclerView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            LectureViewColumnAdapter adapter = new LectureViewColumnAdapter(roomLectures, layoutParamsByLecture, lectureViewDrawer, this);
            columnRecyclerView.setAdapter(adapter);
            adapterByRoomIndex.put(roomIndex, adapter);

            columnsLayout.addView(columnRecyclerView);
        }
    }

    private void addRoomTitleViews(HorizontalScrollView scroller) {
        LinearLayout root = (LinearLayout) scroller.getChildAt(0);
        root.removeAllViews();
        Set<Entry<String, Integer>> roomTitleSet = MyApp.roomsMap.entrySet();
        int textSize = getResources().getInteger(R.integer.room_title_size);
        for (int i = 0; i < MyApp.roomCount; i++) {
            TextView roomTitle = new TextView(context);
            LinearLayout.LayoutParams p = new LayoutParams(
                    roomColumnWidth, LayoutParams.WRAP_CONTENT, 1);
            p.gravity = Gravity.CENTER;
            roomTitle.setLayoutParams(p);
            roomTitle.setMaxLines(1);
            roomTitle.setEllipsize(TextUtils.TruncateAt.END);
            roomTitle.setPadding(0, 0, getEventPadding(), 0);
            roomTitle.setGravity(Gravity.CENTER);
            roomTitle.setTypeface(light);
            int v = MyApp.roomList.get(i);
            for (Entry<String, Integer> entry : roomTitleSet) {
                if (entry.getValue() == v) {
                    roomTitle.setText(entry.getKey());
                    break;
                }
            }
            roomTitle.setTextColor(0xffffffff);
            roomTitle.setTextSize(textSize);
            root.addView(roomTitle);
        }
    }

    /**
     * jump to current time or lecture, if we are on today's lecture list
     */
    private void scrollToCurrent(int day, int height) {
        // Log.d(LOG_TAG, "lectureListDay: " + MyApp.lectureListDay);
        if (lectureId != null) {
            return;
        }
        if (MyApp.lectureListDay != MyApp.dateInfos.getIndexOfToday()) {
            return;
        }
        Moment nowMoment = new Moment();
        HorizontalSnapScrollView horiz = null;

        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                break;
            default:
                horiz = getView().findViewById(R.id.horizScroller);
                break;
        }

        int col = -1;
        if (horiz != null) {
            col = horiz.getColumn();
            MyApp.LogDebug(LOG_TAG, "y pos  = " + col);
        }
        int time = conference.getFirstEventStartsAt();
        int printTime = time;
        int scrollAmount = 0;

        if (!(nowMoment.getMinuteOfDay() < conference.getFirstEventStartsAt() &&
                MyApp.dateInfos.sameDay(nowMoment, MyApp.lectureListDay))) {

            TimeSegment timeSegment;
            while (time < conference.getLastEventEndsAt()) {
                timeSegment = new TimeSegment(printTime);
                if (timeSegment.isMatched(nowMoment, FIFTEEN_MINUTES)) {
                    break;
                } else {
                    scrollAmount += height * 3;
                }
                time += FIFTEEN_MINUTES;
                printTime = time;
                if (printTime >= ONE_DAY) {
                    printTime -= ONE_DAY;
                }
            }

            for (Lecture l : MyApp.lectureList) {
                if (l.day == day && l.startTime <= time && l.startTime + l.duration > time) {
                    if (col == -1 || col >= 0 && l.roomIndex == MyApp.roomList.get(col)) {
                        MyApp.LogDebug(LOG_TAG, l.title);
                        MyApp.LogDebug(LOG_TAG, time + " " + l.startTime + "/" + l.duration);
                        scrollAmount -= ((time - l.startTime) / 5) * height;
                        time = l.startTime;
                    }
                }
            }
        } else {
            // Log.d(LOG_TAG, "we are before " + firstLectureStart + " " + ((now.hour * 60) + now.minute));
        }

        // Log.d(LOG_TAG, "scrolltoCurrent to " + scrollAmount);

        final int pos = scrollAmount;
        final ScrollView scrollView = getView().findViewById(R.id.scrollView1);
        scrollView.scrollTo(0, scrollAmount);
        scrollView.post(() -> scrollView.scrollTo(0, pos));
    }

    private void setBell(Lecture lecture) {
        ScrollView parent = getView().findViewById(R.id.scrollView1);
        if (parent == null) {
            return;
        }
        View v = parent.findViewWithTag(lecture);
        if (v == null) {
            return;
        }
        ImageView bell = v.findViewById(R.id.bell);
        if (bell == null) {
            return;
        }

        if (lecture.hasAlarm) {
            bell.setVisibility(View.VISIBLE);
        } else {
            bell.setVisibility(View.GONE);
        }
    }

    private void scrollTo(@NonNull Lecture lecture) {
        final ScrollView parent = getView().findViewById(R.id.scrollView1);
        int height = getNormalizedBoxHeight(getResources(), scale, LOG_TAG);
        final int pos = (lecture.relStartTime - conference.getFirstEventStartsAt()) / 5 * height;
        MyApp.LogDebug(LOG_TAG, "position is " + pos);
        parent.post(() -> parent.scrollTo(0, pos));
        final HorizontalSnapScrollView horiz = getView().findViewById(R.id.horizScroller);
        if (horiz != null) {
            final int hpos = MyApp.roomList.keyAt(MyApp.roomList.indexOfValue(lecture.roomIndex));
            MyApp.LogDebug(LOG_TAG, "scroll horiz to " + hpos);
            horiz.post(() -> horiz.scrollToColumn(hpos, false));
        }
    }

    private void chooseDay(int chosenDay) {
        if (chosenDay + 1 != mDay) {
            mDay = chosenDay + 1;
            saveCurrentDay(mDay);
            viewDay(true);
            fillTimes();
        }
    }

    private void fillTimes() {
        int time = conference.getFirstEventStartsAt();
        int printTime = time;
        LinearLayout timeTextColumn = getView().findViewById(R.id.times_layout);
        timeTextColumn.removeAllViews();
        Moment nowMoment = new Moment();
        View timeTextView;
        int timeTextViewHeight = 3 * getNormalizedBoxHeight(getResources(), scale, LOG_TAG);
        TimeSegment timeSegment;
        while (time < conference.getLastEventEndsAt()) {
            timeSegment = new TimeSegment(printTime);
            int timeTextLayout;
            if (isToday(nowMoment) && timeSegment.isMatched(nowMoment, FIFTEEN_MINUTES)) {
                timeTextLayout = R.layout.time_layout_now;
            } else {
                timeTextLayout = R.layout.time_layout;
            }
            timeTextView = inflater.inflate(timeTextLayout, null);
            timeTextColumn.addView(timeTextView, LayoutParams.MATCH_PARENT, timeTextViewHeight);
            TextView title = timeTextView.findViewById(R.id.time);
            title.setText(timeSegment.getFormattedText());
            time += FIFTEEN_MINUTES;
            printTime = time;
            if (printTime >= ONE_DAY) {
                printTime -= ONE_DAY;
            }
        }
    }

    private boolean isToday(@NonNull Moment moment) {
        return moment.getMonthDay() - BuildConfig.SCHEDULE_FIRST_DAY_START_DAY == mDay - 1;
    }

    private int getEventPadding() {
        int padding;
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                padding = (int) (8 * scale);
                break;
            default:
                padding = (int) (10 * scale);
                break;
        }
        return padding;
    }

    public static void loadLectureList(@NonNull AppRepository appRepository, int day, boolean forceReload) {
        MyApp.LogDebug(LOG_TAG, "load lectures of day " + day);

        if (!forceReload && MyApp.lectureList != null && MyApp.lectureListDay == day) {
            return;
        }

        MyApp.lectureList = appRepository.loadUncanceledLecturesForDayIndex(day);
        if (MyApp.lectureList.isEmpty()) {
            return;
        }
        MyApp.lectureListDay = day;

        MyApp.roomsMap.clear();
        MyApp.roomList.clear();
        for (Lecture lecture : MyApp.lectureList) {
            if (!MyApp.roomsMap.containsKey(lecture.room)) {
                if (!MyApp.roomsMap.containsValue(lecture.roomIndex)) {
                    // room name : room index
                    MyApp.roomsMap.put(lecture.room, lecture.roomIndex);
                } else {
                    // upgrade from DB without roomIndex
                    int newIndex;
                    for (newIndex = 0; newIndex < rooms.length; newIndex++) {
                        // Is the current room in the list of prioritized rooms?
                        if (lecture.room.equals(rooms[newIndex])) {
                            break;
                        }
                    }
                    // Room is not in the list of prioritized rooms.
                    // A new room index is calculated now.
                    if (newIndex == rooms.length) {
                        newIndex = 0;
                        while (MyApp.roomsMap.containsValue(newIndex)) {
                            newIndex++;
                        }
                    }
                    MyApp.roomsMap.put(lecture.room, newIndex);
                    MyApp.LogDebug(LOG_TAG,
                            "Upgrade room " + lecture.room + " to index " + newIndex);
                    lecture.roomIndex = newIndex;
                }
            }
            // upgrade
            if (lecture.roomIndex == 0) {
                lecture.roomIndex = MyApp.roomsMap.get(lecture.room);
            }
        }
        MyApp.roomCount = MyApp.roomsMap.size();
        MyApp.LogDebug(LOG_TAG, "room count = " + MyApp.roomCount);
        List<Integer> rooms = new ArrayList<>(MyApp.roomsMap.values());
        Collections.sort(rooms);
        int k = 0;
        for (Integer v : rooms) {
            MyApp.LogDebug(LOG_TAG, "room column " + k + " is room " + v);
            MyApp.roomList.append(k, v);
            k++;
        }

        if (!MyApp.lectureList.isEmpty() && MyApp.lectureList.get(0).dateUTC > 0) {
            Collections.sort(MyApp.lectureList, (lhs, rhs) -> Long.compare(lhs.dateUTC, rhs.dateUTC));
        }

        loadAlarms(appRepository);
    }

    public static void loadAlarms(@NonNull AppRepository appRepository) {
        if (MyApp.lectureList == null) {
            return;
        }

        for (Lecture lecture : MyApp.lectureList) {
            lecture.hasAlarm = false;
        }

        List<Alarm> alarms = appRepository.readAlarms();
        MyApp.LogDebug(LOG_TAG, "Got " + alarms.size() + " alarm rows.");
        for (Alarm alarm : alarms) {
            MyApp.LogDebug(LOG_TAG, "Event " + alarm.getEventId() + " has alarm.");
            for (Lecture lecture : MyApp.lectureList) {
                if (lecture.lectureId.equals(alarm.getEventId())) {
                    lecture.hasAlarm = true;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        Lecture lecture = (Lecture) v.getTag();
        if (lecture == null) {
            throw new NullPointerException("A lecture must be assigned to the 'tag' attribute of the lecture view.");
        }
        MyApp.LogDebug(LOG_TAG, "Click on " + lecture.title);
        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.openLectureDetail(lecture, mDay, false);
    }

    public void buildNavigationMenu() {
        Moment currentDate = new Moment().startOfDay();
        MyApp.LogDebug(LOG_TAG, "Today is " + currentDate.toUTCDateTime().toLocalDate());
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FAHRPLAN_FRAGMENT_REQUEST_CODE &&
                resultCode == AlarmTimePickerFragment.ALERT_TIME_PICKED_RESULT_CODE) {
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
        if (lastSelectedLecture == null) {
            Log.e(getClass().getSimpleName(), "onAlarmTimesIndexPicked: lecture: null. alarmTimesIndex: " + alarmTimesIndex);
            throw new NullPointerException("Lecture is null.");
        }
        FahrplanMisc.addAlarm(requireContext(), appRepository, lastSelectedLecture, alarmTimesIndex);
        setBell(lastSelectedLecture);
        updateMenuItems();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int menuItemIndex = item.getItemId();
        Lecture lecture = (Lecture) contextMenuView.getTag();
        lastSelectedLecture = lecture;

        MyApp.LogDebug(LOG_TAG, "clicked on " + ((Lecture) contextMenuView.getTag()).lectureId);

        Context context = requireContext();
        switch (menuItemIndex) {
            case CONTEXT_MENU_ITEM_ID_FAVORITES:
                lecture.highlight = !lecture.highlight;
                appRepository.updateHighlight(lecture);
                lectureViewDrawer.setLectureBackground(lecture, contextMenuView);
                LectureViewDrawer.setLectureTextColor(lecture, contextMenuView);
                ((MainActivity) context).refreshFavoriteList();
                updateMenuItems();
                break;
            case CONTEXT_MENU_ITEM_ID_SET_ALARM:
                showAlarmTimePicker();
                break;
            case CONTEXT_MENU_ITEM_ID_DELETE_ALARM:
                FahrplanMisc.deleteAlarm(context, appRepository, lecture);
                setBell(lecture);
                updateMenuItems();
                break;
            case CONTEXT_MENU_ITEM_ID_ADD_TO_CALENDAR:
                CalendarSharing.addToCalendar(lecture, context);
                break;
            case CONTEXT_MENU_ITEM_ID_SHARE:
                if (BuildConfig.ENABLE_CHAOSFLIX_EXPORT) {
                    break;
                }
            case CONTEXT_MENU_ITEM_ID_SHARE_TEXT:
                String formattedLecture = SimpleLectureFormat.format(lecture);
                if (!LectureSharer.shareSimple(context, formattedLecture)) {
                    Toast.makeText(context, R.string.share_error_activity_not_found, Toast.LENGTH_SHORT).show();
                }
                break;
            case CONTEXT_MENU_ITEM_ID_SHARE_JSON:
                String jsonFormattedLecture = JsonLectureFormat.format(lecture);
                if (!LectureSharer.shareJson(context, jsonFormattedLecture)) {
                    Toast.makeText(context, R.string.share_error_activity_not_found, Toast.LENGTH_SHORT).show();
                }
                break;

        }
        return true;
    }

    private void updateMenuItems() {
        // Toggles the icon for "add/delete favorite" or "add/delete alarm".
        // Triggers EventDetailFragment.onPrepareOptionsMenu to be called
        requireActivity().invalidateOptionsMenu();
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        contextMenuView = view;
        Lecture lecture = (Lecture) view.getTag();
        if (lecture.highlight) {
            menu.add(0, CONTEXT_MENU_ITEM_ID_FAVORITES, 0, getString(R.string.menu_item_title_unflag_as_favorite));
        } else {
            menu.add(0, CONTEXT_MENU_ITEM_ID_FAVORITES, 0, getString(R.string.menu_item_title_flag_as_favorite));
        }
        if (lecture.hasAlarm) {
            menu.add(0, CONTEXT_MENU_ITEM_ID_DELETE_ALARM, 2, getString(R.string.menu_item_title_delete_alarm));
        } else {
            menu.add(0, CONTEXT_MENU_ITEM_ID_SET_ALARM, 1, getString(R.string.menu_item_title_set_alarm));
        }
        menu.add(0, CONTEXT_MENU_ITEM_ID_ADD_TO_CALENDAR, 3, getString(R.string.menu_item_title_add_to_calendar));

        if (BuildConfig.ENABLE_CHAOSFLIX_EXPORT) {
            SubMenu share = menu.addSubMenu(0, CONTEXT_MENU_ITEM_ID_SHARE, 4, getString(R.string.menu_item_title_share_event));
            share.add(0, CONTEXT_MENU_ITEM_ID_SHARE_TEXT, 5, getString(R.string.menu_item_title_share_event_text));
            share.add(0, CONTEXT_MENU_ITEM_ID_SHARE_JSON, 6, getString(R.string.menu_item_title_share_event_json));
        } else {
            menu.add(0, CONTEXT_MENU_ITEM_ID_SHARE, 4, getString(R.string.menu_item_title_share_event));
        }
    }

    private View getLectureView(Lecture lecture) {
        ScrollView parent = getView().findViewById(R.id.scrollView1);
        if (parent == null) {
            return null;
        }
        return parent.findViewWithTag(lecture);
    }

    public void refreshViews() {
        if (MyApp.lectureList == null) {
            return;
        }
        for (Lecture lecture : MyApp.lectureList) {
            setBell(lecture);
            View v = getLectureView(lecture);
            if (v != null) {
                lectureViewDrawer.setLectureBackground(lecture, v);
                LectureViewDrawer.setLectureTextColor(lecture, v);
            }
        }
    }

    public void refreshEventMarkers() {
        MyApp.LogDebug(LOG_TAG, "Reload alarms");
        refreshViews();
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