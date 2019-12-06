package nerd.tuxmobil.fahrplan.congress.schedule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import kotlin.Unit;
import nerd.tuxmobil.fahrplan.congress.BuildConfig;
import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmTimePickerFragment;
import nerd.tuxmobil.fahrplan.congress.calendar.CalendarSharing;
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys;
import nerd.tuxmobil.fahrplan.congress.extensions.Contexts;
import nerd.tuxmobil.fahrplan.congress.models.Alarm;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;
import nerd.tuxmobil.fahrplan.congress.net.ParseResult;
import nerd.tuxmobil.fahrplan.congress.net.ParseScheduleResult;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;
import nerd.tuxmobil.fahrplan.congress.sharing.LectureSharer;
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleLectureFormat;
import nerd.tuxmobil.fahrplan.congress.utils.DateHelper;
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc;
import nerd.tuxmobil.fahrplan.congress.utils.LectureUtils;

import static nerd.tuxmobil.fahrplan.congress.extensions.Resource.getNormalizedBoxHeight;

public class FahrplanFragment extends Fragment implements OnClickListener {

    public interface OnRefreshEventMarkers {

        void refreshEventMarkers();
    }

    private static String LOG_TAG = "Fahrplan";

    public static final String FRAGMENT_TAG = "schedule";

    public static final int FAHRPLAN_FRAGMENT_REQUEST_CODE = 6166;

    private static final int CONTEXT_MENU_ITEM_ID_FAVORITES = 0;
    private static final int CONTEXT_MENU_ITEM_ID_SET_ALARM = 1;
    private static final int CONTEXT_MENU_ITEM_ID_DELETE_ALARM = 2;
    private static final int CONTEXT_MENU_ITEM_ID_ADD_TO_CALENDAR = 3;
    private static final int CONTEXT_MENU_ITEM_ID_SHARE = 4;

    private static final int ONE_DAY = 24 * 60;
    private static final int FIFTEEN_MINUTES = 15;

    private float scale;

    private LayoutInflater inflater;

    private Conference conference = new Conference();

    private AppRepository appRepository;

    private HashMap<String, Integer> trackNameBackgroundColorDefaultPairs;

    private HashMap<String, Integer> trackNameBackgroundColorHighlightPairs;

    private int mDay = 1;

    private View dayTextView;

    public static Context context = null;

    public static String[] rooms = {
            "Saal 1",
            "Saal 2",
            "Saal G",
            "Saal 6",
            "Saal 17",
            "Lounge"
    };

    public static final String PREFS_NAME = "settings";

    private int screenWidth = 0;

    private Typeface boldCondensed;

    private Typeface light;

    private int eventDrawableInsetTop;

    private int eventDrawableInsetLeft;

    private int eventDrawableInsetRight;

    private float eventDrawableCornerRadius;

    private float eventDrawableStrokeWidth;

    private
    @ColorInt
    int eventDrawableStrokeColor;

    private
    @ColorInt
    int eventDrawableRippleColor;

    private View contextMenuView;

    private int columnWidth;

    private String lectureId;        // started with lectureId
    private HashMap<String, Integer> trackAccentColors;
    private HashMap<String, Integer> trackAccentColorsHighlight;

    private Lecture lastSelectedLecture;

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
        boldCondensed = Typeface.createFromAsset(
                context.getAssets(), "Roboto-BoldCondensed.ttf");
        light = Typeface.createFromAsset(
                context.getAssets(), "Roboto-Light.ttf");
        Resources resources = getResources();
        eventDrawableInsetTop = resources.getDimensionPixelSize(
                R.dimen.event_drawable_inset_top);
        eventDrawableInsetLeft = resources.getDimensionPixelSize(
                R.dimen.event_drawable_inset_left);
        eventDrawableInsetRight = resources.getDimensionPixelSize(
                R.dimen.event_drawable_inset_right);
        eventDrawableCornerRadius = resources.getDimensionPixelSize(
                R.dimen.event_drawable_corner_radius);
        eventDrawableStrokeWidth = resources.getDimensionPixelSize(
                R.dimen.event_drawable_selection_stroke_width);
        eventDrawableStrokeColor = ContextCompat.getColor(
                FahrplanFragment.context, R.color.event_drawable_selection_stroke);
        eventDrawableRippleColor = ContextCompat.getColor(
                FahrplanFragment.context, R.color.event_drawable_ripple);
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
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        MyApp.LogDebug(LOG_TAG, "screen width = " + screenWidth);
        MyApp.LogDebug(LOG_TAG, "time width " + getResources().getDimension(R.dimen.time_width));
        screenWidth -= getResources().getDimension(R.dimen.time_width);
        int max_cols = HorizontalSnapScrollView.calcMaxCols(getResources(), screenWidth);
        MyApp.LogDebug(LOG_TAG, "max cols: " + max_cols);
        columnWidth = (int) ((float) screenWidth / max_cols); // Width for the row column
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

        trackNameBackgroundColorDefaultPairs = TrackBackgrounds.getTrackNameBackgroundColorDefaultPairs(context);
        trackNameBackgroundColorHighlightPairs = TrackBackgrounds.getTrackNameBackgroundColorHighlightPairs(context);
        trackAccentColors = TrackBackgrounds.getTrackAccentColorNormal(context);
        trackAccentColorsHighlight = TrackBackgrounds.getTrackAccentColorHighlight(context);

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        mDay = prefs.getInt("displayDay", 1);

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
        SharedPreferences settings = requireContext().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("displayDay", day);
        editor.apply();
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
            scrollTo(lectureId);
            FrameLayout sidePane = activity.findViewById(R.id.detail);
            if (sidePane != null) {
                Lecture lecture = LectureUtils.getLecture(MyApp.lectureList, lectureId);
                ((MainActivity) activity).openLectureDetail(lecture, mDay, false);
            }
            intent.removeExtra("lecture_id");   // jump to given lectureId only once
        }
        fillTimes();
    }

    private void viewDay(boolean reload) {
        Log.d(LOG_TAG, "viewDay(" + reload + ")");

        loadLectureList(appRepository, mDay, reload);
        List<Lecture> lectures = MyApp.lectureList;
        if (lectures != null && !lectures.isEmpty()) {
            conference.calculateTimeFrame(lectures, DateHelper::getMinutesOfDay);
            MyApp.LogDebug(LOG_TAG, "Conference = " + conference);
        }
        View layoutRoot = getView();
        HorizontalSnapScrollView scroller = layoutRoot.findViewById(R.id.horizScroller);
        if (scroller != null) {
            scroller.scrollTo(0, 0);
            addRoomColumns(scroller);
        }
        HorizontalScrollView roomScroller = layoutRoot.findViewById(R.id.roomScroller);
        if (roomScroller != null) {
            addRoomTitleViews(roomScroller);
        }

        int boxHeight = getNormalizedBoxHeight(getResources(), scale, LOG_TAG);
        for (int i = 0; i < MyApp.roomCount; i++) {
            ViewGroup rootView = (ViewGroup) scroller.getChildAt(0);
            LinearLayout roomView = (LinearLayout) rootView.getChildAt(i);
            int roomIndex = MyApp.roomList.get(i);
            fillRoom(roomView, roomIndex, MyApp.lectureList, boxHeight);
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

    private void addRoomColumns(HorizontalSnapScrollView scroller) {
        LinearLayout root = (LinearLayout) scroller.getChildAt(0);
        root.removeAllViews();
        if (scroller.getColumnWidth() != 0) {
            // update pre-calculated width with actual layout
            columnWidth = scroller.getColumnWidth();
        }
        for (int i = 0; i < MyApp.roomCount; i++) {
            LinearLayout roomLayout = new LinearLayout(context);
            LinearLayout.LayoutParams p = new LayoutParams(
                    columnWidth, LayoutParams.MATCH_PARENT, 1);
            roomLayout.setOrientation(LinearLayout.VERTICAL);
            roomLayout.setLayoutParams(p);
            root.addView(roomLayout);
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
                    columnWidth, LayoutParams.WRAP_CONTENT, 1);
            p.gravity = Gravity.CENTER;
            roomTitle.setLayoutParams(p);
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
        if (MyApp.lectureListDay != MyApp.dateInfos.getIndexOfToday(
                MyApp.meta.getDayChangeHour(), MyApp.meta.getDayChangeMinute())) {
            return;
        }
        Time now = new Time();
        now.setToNow();
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

        if (!(now.hour * 60 + now.minute < conference.getFirstEventStartsAt() &&
                MyApp.dateInfos.sameDay(now, MyApp.lectureListDay))) {

            TimeSegment timeSegment;
            while (time < conference.getLastEventEndsAt()) {
                timeSegment = new TimeSegment(printTime);
                if (timeSegment.isMatched(now, FIFTEEN_MINUTES)) {
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

    private void scrollTo(String lectureId) {
        for (Lecture lecture : MyApp.lectureList) {
            if (lectureId.equals(lecture.lectureId)) {
                final ScrollView parent = getView().findViewById(R.id.scrollView1);
                int height = getNormalizedBoxHeight(getResources(), scale, LOG_TAG);
                final int pos = (lecture.relStartTime - conference.getFirstEventStartsAt()) / 5 * height;
                MyApp.LogDebug(LOG_TAG, "position is " + pos);
                parent.post(() -> parent.scrollTo(0, pos));
                final HorizontalSnapScrollView horiz =
                        getView().findViewById(R.id.horizScroller);
                if (horiz != null) {
                    final int hpos = MyApp.roomList.keyAt(
                            MyApp.roomList.indexOfValue(lecture.roomIndex));
                    MyApp.LogDebug(LOG_TAG, "scroll horiz to " + hpos);
                    horiz.post(() -> horiz.scrollToColumn(hpos, false));
                }
                break;
            }
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
        Time now = new Time();
        now.setToNow();
        View timeTextView;
        int timeTextViewHeight = 3 * getNormalizedBoxHeight(getResources(), scale, LOG_TAG);
        TimeSegment timeSegment;
        while (time < conference.getLastEventEndsAt()) {
            timeSegment = new TimeSegment(printTime);
            int timeTextLayout;
            if (isToday(now) && timeSegment.isMatched(now, FIFTEEN_MINUTES)) {
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

    private boolean isToday(Time time) {
        return time.monthDay - BuildConfig.SCHEDULE_FIRST_DAY_START_DAY == mDay - 1;
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

    private void setLectureBackground(Lecture event, View eventView) {
        Context context = eventView.getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean defaultValue = getResources().getBoolean(R.bool.preferences_alternative_highlight_enabled_default_value);
        boolean alternativeHighlightingIsEnabled = prefs.getBoolean(
                BundleKeys.PREFS_ALTERNATIVE_HIGHLIGHT, defaultValue);
        boolean eventIsFavored = event.highlight;
        @ColorRes int backgroundColorResId;
        if (eventIsFavored) {
            Integer colorResId = trackNameBackgroundColorHighlightPairs.get(event.track);
            backgroundColorResId = colorResId == null ? R.color.event_border_highlight : colorResId;
        } else {
            Integer colorResId = trackNameBackgroundColorDefaultPairs.get(event.track);
            backgroundColorResId = colorResId == null ? R.color.event_border_default : colorResId;
        }
        @ColorInt int backgroundColor = ContextCompat.getColor(context, backgroundColorResId);
        EventDrawable eventDrawable;
        if (eventIsFavored && alternativeHighlightingIsEnabled) {
            eventDrawable = new EventDrawable(
                    backgroundColor,
                    eventDrawableCornerRadius,
                    eventDrawableRippleColor,
                    eventDrawableStrokeColor,
                    eventDrawableStrokeWidth);
        } else {
            eventDrawable = new EventDrawable(
                    backgroundColor,
                    eventDrawableCornerRadius,
                    eventDrawableRippleColor);
        }
        eventDrawable.setLayerInset(EventDrawable.BACKGROUND_LAYER_INDEX,
                eventDrawableInsetLeft,
                eventDrawableInsetTop,
                eventDrawableInsetRight,
                0);
        eventDrawable.setLayerInset(EventDrawable.STROKE_LAYER_INDEX,
                eventDrawableInsetLeft,
                eventDrawableInsetTop,
                eventDrawableInsetRight,
                0);
        eventView.setBackgroundDrawable(eventDrawable);
        int padding = getEventPadding();
        eventView.setPadding(padding, padding, padding, padding);
    }

    private void setLectureTextColor(Lecture lecture, View view) {
        TextView title = view.findViewById(R.id.event_title);
        TextView subtitle = view.findViewById(R.id.event_subtitle);
        TextView speakers = view.findViewById(R.id.event_speakers);
        int colorResId = lecture.highlight ? R.color.event_title_highlight : R.color.event_title;
        int textColor = ContextCompat.getColor(view.getContext(), colorResId);
        title.setTextColor(textColor);
        subtitle.setTextColor(textColor);
        speakers.setTextColor(textColor);
    }

    private void fillRoom(LinearLayout room, int roomIndex, @NonNull List<Lecture> lectures, int standardHeight) {
        room.removeAllViews();
        int endTime = conference.getFirstEventStartsAt();
        int startTime;
        View event = null;
        int margin;

        for (int idx = 0; idx < lectures.size(); idx++) {
            Lecture lecture = lectures.get(idx);
            if (lecture.roomIndex == roomIndex) {
                if (lecture.dateUTC > 0) {
                    startTime = DateHelper.getMinutesOfDay(lecture.dateUTC);
                    if (startTime < endTime) {
                        startTime += ONE_DAY;
                    }
                } else {
                    startTime = lecture.relStartTime;
                }
                if (startTime > endTime) {
                    margin = standardHeight * (startTime - endTime) / 5;
                    if (event != null) {
                        LayoutParams lp = (LayoutParams) event.getLayoutParams();
                        lp.bottomMargin = margin;
                        event.setLayoutParams(lp);
                        margin = 0;
                    }
                } else {
                    margin = 0;
                }

                // fix overlapping events
                Lecture next = null;
                for (int nextIndex = idx + 1; nextIndex < lectures.size(); nextIndex++) {
                    next = lectures.get(nextIndex);
                    if (next.roomIndex == roomIndex) {
                        break;
                    }
                    next = null;
                }
                if (next != null) {
                    if (next.dateUTC > 0) {
                        if (lecture.dateUTC + lecture.duration * 60000 > next.dateUTC) {
                            MyApp.LogDebug(LOG_TAG, lecture.title + " collides with " + next.title);
                            lecture.duration = (int) ((next.dateUTC - lecture.dateUTC) / 60000);
                        }
                    }
                }

                event = inflater.inflate(R.layout.event_layout, null);
                int height = standardHeight * (lecture.duration / 5);
                room.addView(event, LayoutParams.MATCH_PARENT, height);
                LayoutParams lp = (LayoutParams) event.getLayoutParams();
                lp.topMargin = margin;
                event.setLayoutParams(lp);
                updateEventView(event, lecture);
                endTime = startTime + lecture.duration;
            }
        }
    }

    private void updateEventView(View eventView, Lecture lecture) {
        ImageView bell = eventView.findViewById(R.id.bell);
        bell.setVisibility(lecture.hasAlarm ? View.VISIBLE : View.GONE);
        TextView title = eventView.findViewById(R.id.event_title);
        title.setTypeface(boldCondensed);
        title.setText(lecture.title);
        title = eventView.findViewById(R.id.event_subtitle);
        title.setText(lecture.subtitle);
        title = eventView.findViewById(R.id.event_speakers);
        title.setText(lecture.getFormattedSpeakers());
        title = eventView.findViewById(R.id.event_track);
        title.setText(lecture.getFormattedTrackText());
        title.setContentDescription(lecture.getFormattedTrackContentDescription(eventView.getContext()));
        View recordingOptOut = eventView.findViewById(R.id.novideo);
        if (recordingOptOut != null) {
            recordingOptOut.setVisibility(lecture.recordingOptOut ? View.VISIBLE : View.GONE);
        }
        setLectureBackground(lecture, eventView);
        setLectureTextColor(lecture, eventView);
        eventView.setOnClickListener(this);
        eventView.setLongClickable(true);
        eventView.setOnCreateContextMenuListener(this);
        eventView.setTag(lecture);
    }

    public static void loadLectureList(@NonNull AppRepository appRepository, int day, boolean force) {
        MyApp.LogDebug(LOG_TAG, "load lectures of day " + day);

        if (!force && MyApp.lectureList != null && MyApp.lectureListDay == day) {
            return;
        }

        MyApp.lectureList = FahrplanMisc.getUncanceledLectures(appRepository, day);
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
        MyApp.LogDebug(LOG_TAG, "Click on " + lecture.title);
        MainActivity mainActivity = (MainActivity) requireActivity();
        if (mainActivity != null) {
            mainActivity.openLectureDetail(lecture, mDay, false);
        }
    }

    public void buildNavigationMenu() {
        String currentDate = DateHelper.getCurrentDate();
        MyApp.LogDebug(LOG_TAG, "Today is " + currentDate);
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
        if (result.isSuccess()) {
            if (MyApp.meta.getNumDays() == 0
                    || (result instanceof ParseScheduleResult
                    && !((ParseScheduleResult) result).getVersion().equals(MyApp.meta.getVersion()))
            ) {
                MyApp.meta = appRepository.readMeta();
                FahrplanMisc.loadDays(appRepository);
                if (MyApp.meta.getNumDays() > 1) {
                    buildNavigationMenu();
                }
                SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, 0);
                mDay = prefs.getInt("displayDay", 1);
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
            MyApp.LogDebug(getClass().getName(), message);
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        }
        activity.invalidateOptionsMenu();
    }

    private String getParsingErrorMessage(@NonNull ParseResult parseResult) {
        String message = "";
        if (parseResult instanceof ParseScheduleResult) {
            String version = ((ParseScheduleResult) parseResult).getVersion();
            if (version.isEmpty()) {
                message = getString(R.string.schedule_parsing_error_generic);
            } else {
                message = getString(R.string.schedule_parsing_error_with_version, version);
            }
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
            Log.e(getClass().getName(), "onAlarmTimesIndexPicked: lecture: null. alarmTimesIndex: " + alarmTimesIndex);
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
                setLectureBackground(lecture, contextMenuView);
                setLectureTextColor(lecture, contextMenuView);
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
                String formattedLecture = SimpleLectureFormat.format(lecture);
                if (!LectureSharer.shareSimple(context, formattedLecture)) {
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
        menu.add(0, CONTEXT_MENU_ITEM_ID_SHARE, 4, getString(R.string.menu_item_title_share_event));
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
                setLectureBackground(lecture, v);
                setLectureTextColor(lecture, v);
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