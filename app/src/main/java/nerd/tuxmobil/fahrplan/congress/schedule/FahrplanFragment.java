package nerd.tuxmobil.fahrplan.congress.schedule;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmTimePickerFragment;
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys;
import nerd.tuxmobil.fahrplan.congress.models.DateInfo;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;
import nerd.tuxmobil.fahrplan.congress.persistence.AlarmsDBOpenHelper;
import nerd.tuxmobil.fahrplan.congress.persistence.FahrplanContract.AlarmsTable;
import nerd.tuxmobil.fahrplan.congress.serialization.FahrplanParser;
import nerd.tuxmobil.fahrplan.congress.sharing.LectureSharer;
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleLectureFormat;
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc;
import nerd.tuxmobil.fahrplan.congress.utils.LectureUtils;

public class FahrplanFragment extends Fragment implements
        OnClickListener,
        ActionBar.OnNavigationListener,
        FahrplanParser.OnParseCompleteListener {

    public interface OnRefreshEventMarkers {

        void refreshEventMarkers();
    }

    private MyApp global;

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

    private int firstLectureStart = 0;

    private int lastLectureEnd = 0;

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

    private String lecture_id;        // started with lecture_id
    private HashMap<String, Integer> trackAccentColors;
    private HashMap<String, Integer> trackAccentColorsHighlight;

    private Lecture lastSelectedLecture;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        boldCondensed = Typeface.createFromAsset(
                getActivity().getAssets(), "Roboto-BoldCondensed.ttf");
        light = Typeface.createFromAsset(
                getActivity().getAssets(), "Roboto-Light.ttf");
        context = getActivity();
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
                context, R.color.event_drawable_selection_stroke);
        eventDrawableRippleColor = ContextCompat.getColor(
                context, R.color.event_drawable_ripple);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.schedule, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        global = (MyApp) getActivity().getApplicationContext();
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
            roomScroller.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }

        trackNameBackgroundColorDefaultPairs = TrackBackgrounds.getTrackNameBackgroundColorDefaultPairs(getActivity());
        trackNameBackgroundColorHighlightPairs = TrackBackgrounds.getTrackNameBackgroundColorHighlightPairs(getActivity());
        trackAccentColors = TrackBackgrounds.getTrackAccentColorNormal(getActivity());
        trackAccentColorsHighlight = TrackBackgrounds.getTrackAccentColorHighlight(getActivity());

        SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, 0);
        mDay = prefs.getInt("displayDay", 1);

        inflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Intent intent = getActivity().getIntent();
        lecture_id = intent.getStringExtra("lecture_id");

        if (lecture_id != null) {
            MyApp.LogDebug(LOG_TAG, "Open with lecture_id " + lecture_id);
            mDay = intent.getIntExtra("day", mDay);
            MyApp.LogDebug(LOG_TAG, "day " + mDay);
        }

        if (MyApp.numdays > 1) {
            buildNavigationMenu();
        }
    }

    @Override
    public void onDestroy() {
        MyApp.LogDebug(LOG_TAG, "onDestroy");
        super.onDestroy();
        if (MyApp.fetcher != null) {
            MyApp.fetcher.setListener(null);
        }
        if (MyApp.parser != null) {
            MyApp.parser.setListener(null);
        }
    }

    private void saveCurrentDay(int day) {
        SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("displayDay", day);
        editor.apply();
    }

    @Override
    public void onResume() {
        MyApp.LogDebug(LOG_TAG, "onResume");
        super.onResume();
        getActivity().invalidateOptionsMenu();

        Intent intent = getActivity().getIntent();

        lecture_id = intent.getStringExtra("lecture_id");

        if (lecture_id != null) {
            MyApp.LogDebug(LOG_TAG, "Open with lecture_id " + lecture_id);
            mDay = intent.getIntExtra("day", mDay);
            MyApp.LogDebug(LOG_TAG, "day " + mDay);
            saveCurrentDay(mDay);
        }

        if (MyApp.numdays != 0) {
            // auf jeden Fall reload, wenn mit Lecture ID gestartet
            viewDay(lecture_id != null);
        }

        switch (MyApp.task_running) {
            case FETCH:
                MyApp.LogDebug(LOG_TAG, "fetch was pending, restart");
                if (MyApp.numdays != 0) {
                    viewDay(false);
                }
                break;
            case PARSE:
                MyApp.LogDebug(LOG_TAG, "parse was pending, restart");
                break;
            case NONE:
                if (MyApp.numdays != 0) {
                    // auf jeden Fall reload, wenn mit Lecture ID gestartet
                    viewDay(lecture_id != null);
                }
                break;
        }

        if (lecture_id != null) {
            scrollTo(lecture_id);
            FrameLayout sidePane = getActivity().findViewById(R.id.detail);
            if (sidePane != null) {
                Lecture lecture = LectureUtils.getLecture(MyApp.lectureList, lecture_id);
                ((MainActivity) getActivity()).openLectureDetail(lecture, mDay);
            }
            intent.removeExtra("lecture_id");   // jump to given lecture_id only once
        }
        fillTimes();
    }

    private void viewDay(boolean reload) {
        // Log.d(LOG_TAG, "viewDay(" + reload + ")");

        if (!loadLectureList(getActivity(), mDay, reload)) {
            MyApp.LogDebug(LOG_TAG, "fetch on loading empty lecture list");
            // FIXME
            // fetchFahrplan();
        }
        scanDayLectures();
        HorizontalSnapScrollView scroller =
                getView().findViewById(R.id.horizScroller);
        if (scroller != null) {
            scroller.scrollTo(0, 0);
        }
        HorizontalScrollView roomScroller =
                getView().findViewById(R.id.roomScroller);
        if (scroller != null) {
            addRoomColumns(scroller);
        }
        if (roomScroller != null) {
            addRoomTitleViews(roomScroller);
        }

        for (int i = 0; i < MyApp.room_count; i++) {
            fillRoom((ViewGroup) scroller.getChildAt(0), i);
        }
        scrollToCurrent(mDay);
        ActionBar actionbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionbar != null && MyApp.numdays > 1) {
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
        for (int i = 0; i < MyApp.room_count; i++) {
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
        for (int i = 0; i < MyApp.room_count; i++) {
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
    private void scrollToCurrent(int day) {
        int height;
        // Log.d(LOG_TAG, "lectureListDay: " + MyApp.lectureListDay);
        if (lecture_id != null) {
            return;
        }
        if (MyApp.lectureListDay != MyApp.dateInfos.getIndexOfToday(
                MyApp.dayChangeHour, MyApp.dayChangeMinute)) {
            return;
        }
        Time now = new Time();
        now.setToNow();
        HorizontalSnapScrollView horiz = null;

        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                height = (int) (getResources().getInteger(R.integer.box_height) * scale);
                break;
            default:
                height = (int) (getResources().getInteger(R.integer.box_height) * scale);
                horiz = getView().findViewById(R.id.horizScroller);
                break;
        }

        int col = -1;
        if (horiz != null) {
            col = horiz.getColumn();
            MyApp.LogDebug(LOG_TAG, "y pos  = " + col);
        }
        int time = firstLectureStart;
        int printTime = time;
        int scrollAmount = 0;

        if (!((((now.hour * 60) + now.minute) < firstLectureStart) &&
                MyApp.dateInfos.sameDay(now, MyApp.lectureListDay))) {

            TimeSegment timeSegment;
            while (time < lastLectureEnd) {
                timeSegment = new TimeSegment(printTime);
                if (timeSegment.isMatched(now, FIFTEEN_MINUTES)) {
                    break;
                } else {
                    scrollAmount += (height * 3);
                }
                time += FIFTEEN_MINUTES;
                printTime = time;
                if (printTime >= ONE_DAY) {
                    printTime -= ONE_DAY;
                }
            }

            for (Lecture l : MyApp.lectureList) {
                if ((l.day == day) && (l.startTime <= time) && (l.startTime + l.duration > time)) {
                    if ((col == -1) || ((col >= 0) && (l.room_index == MyApp.roomList.get(col)))) {
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
        scrollView.post(new Runnable() {

            @Override
            public void run() {
                scrollView.scrollTo(0, pos);
            }
        });
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

        if (lecture.has_alarm) {
            bell.setVisibility(View.VISIBLE);
        } else {
            bell.setVisibility(View.GONE);
        }
    }

    private void scrollTo(String lecture_id) {
        int height;
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                MyApp.LogDebug(LOG_TAG, "landscape");
                height = (int) (getResources().getInteger(R.integer.box_height) * scale);
                break;
            default:
                MyApp.LogDebug(LOG_TAG, "other orientation");
                height = (int) (getResources().getInteger(R.integer.box_height) * scale);
                break;
        }
        for (Lecture lecture : MyApp.lectureList) {
            if (lecture_id.equals(lecture.lecture_id)) {
                final ScrollView parent = getView().findViewById(R.id.scrollView1);
                final int pos = (lecture.relStartTime - firstLectureStart) / 5 * height;
                MyApp.LogDebug(LOG_TAG, "position is " + pos);
                parent.post(new Runnable() {

                    @Override
                    public void run() {
                        parent.scrollTo(0, pos);
                    }
                });
                final HorizontalSnapScrollView horiz =
                        getView().findViewById(R.id.horizScroller);
                if (horiz != null) {
                    final int hpos = MyApp.roomList.keyAt(
                            MyApp.roomList.indexOfValue(lecture.room_index));
                    MyApp.LogDebug(LOG_TAG, "scroll horiz to " + hpos);
                    horiz.post(new Runnable() {

                        @Override
                        public void run() {
                            horiz.scrollToColumn(hpos, false);
                        }
                    });
                }
                break;
            }
        }
    }

    private void chooseDay(int chosenDay) {
        if ((chosenDay + 1) != mDay) {
            mDay = chosenDay + 1;
            saveCurrentDay(mDay);
            viewDay(true);
            fillTimes();
        }
    }

    private int minutesOfDay(long dateUTC) {
        Time t = new Time();
        t.set(dateUTC);
        return (t.hour * 60) + t.minute;
    }

    private void scanDayLectures() {
        if ((MyApp.lectureList == null) || (MyApp.lectureList.size() == 0)) return;
        Lecture l = MyApp.lectureList.get(0); // they are already sorted
        long end = 0;
        if (l.dateUTC > 0) {
            firstLectureStart = minutesOfDay(l.dateUTC);
        } else {
            firstLectureStart = l.relStartTime;
        }
        lastLectureEnd = -1;
        for (Lecture lecture : MyApp.lectureList) {
            if (l.dateUTC > 0) {
                if (end == 0) {
                    end = lecture.dateUTC + (lecture.duration * 60000);
                } else if ((lecture.dateUTC + (lecture.duration * 60000)) > end) {
                    end = lecture.dateUTC + (lecture.duration * 60000);
                }
            } else {
                if (lastLectureEnd == -1) {
                    lastLectureEnd = lecture.relStartTime + lecture.duration;
                } else if ((lecture.relStartTime + lecture.duration) > lastLectureEnd) {
                    lastLectureEnd = lecture.relStartTime + lecture.duration;
                }
            }
        }
        if (end > 0) {
            lastLectureEnd = minutesOfDay(end);
            if (lastLectureEnd < firstLectureStart) {
                lastLectureEnd += ONE_DAY;
            }
        }
        MyApp.LogDebug(LOG_TAG, "firstLectureStart=" + firstLectureStart);
        MyApp.LogDebug(LOG_TAG, "lastLectureEnd=" + lastLectureEnd);
    }

    private void fillTimes() {
        int time = firstLectureStart;
        int printTime = time;
        LinearLayout timeTextColumn = getView().findViewById(R.id.times_layout);
        timeTextColumn.removeAllViews();
        Time now = new Time();
        now.setToNow();
        View timeTextView;
        int timeTextViewHeight = getTimeTextViewHeight();
        TimeSegment timeSegment;
        while (time < lastLectureEnd) {
            timeSegment = new TimeSegment(printTime);
            int timeTextLayout;
            if (timeSegment.isMatched(now, FIFTEEN_MINUTES)) {
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

    private int getTimeTextViewHeight() {
        int integer;
        Resources resources = getResources();
        switch (resources.getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                MyApp.LogDebug(LOG_TAG, "landscape");
                integer = resources.getInteger(R.integer.box_height);
                break;
            default:
                MyApp.LogDebug(LOG_TAG, "other orientation");
                integer = resources.getInteger(R.integer.box_height);
                break;
        }
        return ((int) (integer * scale)) * 3;
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean alternativeHighlightingIsEnabled = prefs.getBoolean(
                BundleKeys.PREFS_ALTERNATIVE_HIGHLIGHT, true);
        boolean eventIsFavored = event.highlight;
        @ColorRes int backgroundColorResId;
        if (eventIsFavored) {
            Integer colorResId = trackNameBackgroundColorHighlightPairs.get(event.track);
            backgroundColorResId = colorResId == null ? R.color.event_border_highlight : colorResId;
        } else {
            Integer colorResId = trackNameBackgroundColorDefaultPairs.get(event.track);
            backgroundColorResId = colorResId == null ? R.color.event_border_default : colorResId;
        }
        @ColorInt int backgroundColor = ContextCompat.getColor(getContext(), backgroundColorResId);
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
        TextView track = view.findViewById(R.id.event_track);
        TextView title = view.findViewById(R.id.event_title);
        TextView subtitle = view.findViewById(R.id.event_subtitle);
        TextView speakers = view.findViewById(R.id.event_speakers);
        Integer color;
        Context context = getContext();
        if (lecture.highlight) {
            color = trackAccentColorsHighlight.get(lecture.track);
            title.setTextColor(ContextCompat.getColor(context, R.color.event_title_highlight));
            subtitle.setTextColor(ContextCompat.getColor(context, R.color.event_title_highlight));
            speakers.setTextColor(ContextCompat.getColor(context, R.color.event_title_highlight));
        } else {
            color = trackAccentColors.get(lecture.track);
            title.setTextColor(ContextCompat.getColor(context, R.color.event_title));
            subtitle.setTextColor(ContextCompat.getColor(context, R.color.event_title));
            speakers.setTextColor(ContextCompat.getColor(context, R.color.event_title));
        }
        if (color == null) {
            track.setTextColor(ContextCompat.getColor(context, R.color.event_title));
        } else {
            track.setTextColor(ContextCompat.getColor(context, color));
        }
    }

    private void fillRoom(ViewGroup root, int roomIdx) {
        LinearLayout room = (LinearLayout) root.getChildAt(roomIdx);
        room.removeAllViews();
        int endTime = firstLectureStart;
        int padding = getEventPadding();
        int standardHeight;
        int startTime;
        int room_index = MyApp.roomList.get(roomIdx);
        View event = null;
        int margin;

        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                MyApp.LogDebug(LOG_TAG, "landscape");
                standardHeight = (int) (getResources().getInteger(R.integer.box_height) * scale);
                break;
            default:
                MyApp.LogDebug(LOG_TAG, "other orientation");
                standardHeight = (int) (getResources().getInteger(R.integer.box_height) * scale);
                break;
        }
        for (int idx = 0; idx < MyApp.lectureList.size(); idx++) {
            Lecture lecture = MyApp.lectureList.get(idx);
            if (lecture.room_index == room_index) {
                if (lecture.dateUTC > 0) {
                    startTime = minutesOfDay(lecture.dateUTC);
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
                for (int next_idx = idx + 1; next_idx < MyApp.lectureList.size(); next_idx++) {
                    next = MyApp.lectureList.get(next_idx);
                    if (next.room_index == room_index) {
                        break;
                    }
                    next = null;
                }
                if (next != null) {
                    if (next.dateUTC > 0) {
                        if (lecture.dateUTC + (lecture.duration * 60000) > next.dateUTC) {
                            MyApp.LogDebug(LOG_TAG, lecture.title + " collides with " + next.title);
                            lecture.duration = (int) ((next.dateUTC - lecture.dateUTC) / 60000);
                        }
                    }
                }

                event = inflater.inflate(R.layout.event_layout, null);
                int height = standardHeight * (lecture.duration / 5);
                ImageView bell = event.findViewById(R.id.bell);
                if (lecture.has_alarm) {
                    bell.setVisibility(View.VISIBLE);
                } else {
                    bell.setVisibility(View.GONE);
                }
                room.addView(event, LayoutParams.MATCH_PARENT, height);
                LayoutParams lp = (LayoutParams) event.getLayoutParams();
                lp.topMargin = margin;
                event.setLayoutParams(lp);
                TextView title = event.findViewById(R.id.event_title);
                title.setTypeface(boldCondensed);
                title.setText(lecture.title);
                title = event.findViewById(R.id.event_subtitle);
                title.setText(lecture.subtitle);
                title = event.findViewById(R.id.event_speakers);
                title.setText(lecture.getFormattedSpeakers());
                title = event.findViewById(R.id.event_track);
                StringBuilder sb = new StringBuilder();
                sb.append(lecture.track);
                if ((lecture.lang != null) && (lecture.lang.length() > 0)) {
                    sb.append(" [").append(lecture.lang).append("]");
                }
                title.setText(sb.toString());
                View recordingOptOut = event.findViewById(R.id.novideo);
                if (recordingOptOut != null) {
                    recordingOptOut.setVisibility(
                            lecture.recordingOptOut ? View.VISIBLE : View.GONE);
                }

                setLectureBackground(lecture, event);
                setLectureTextColor(lecture, event);
                event.setOnClickListener(this);
                event.setLongClickable(true);
                // event.setOnLongClickListener(this);
                event.setOnCreateContextMenuListener(this);
                event.setTag(lecture);
                endTime = startTime + lecture.duration;
            }
        }
    }

    public static boolean loadLectureList(Context context, int day, boolean force) {
        MyApp.LogDebug(LOG_TAG, "load lectures of day " + day);

        if (!force && MyApp.lectureList != null && MyApp.lectureListDay == day) {
            return true;
        }

        MyApp.lectureList = FahrplanMisc.loadLecturesForDayIndex(context, day);
        if (MyApp.lectureList == null) return false;

        int lectureIndex = MyApp.lectureList.size() - 1;
        while (lectureIndex >= 0) {
            Lecture l = MyApp.lectureList.get(lectureIndex);
            if (l.changedIsCanceled) MyApp.lectureList.remove(lectureIndex);
            lectureIndex--;
        }
        MyApp.lectureListDay = day;

        MyApp.roomsMap.clear();
        MyApp.roomList.clear();
        for (Lecture lecture : MyApp.lectureList) {
            if (!MyApp.roomsMap.containsKey(lecture.room)) {
                if (!MyApp.roomsMap.containsValue(lecture.room_index)) {
                    MyApp.roomsMap.put(lecture.room, lecture.room_index);
                } else {
                    // upgrade from DB without room_index
                    int new_index;
                    for (new_index = 0; new_index < rooms.length; new_index++) {
                        if (lecture.room.equals(rooms[new_index])) {
                            break;
                        }
                    }
                    if (new_index == rooms.length) {
                        new_index = 0;
                        while (MyApp.roomsMap.containsValue(new_index)) {
                            new_index++;
                        }
                    }
                    MyApp.roomsMap.put(lecture.room, new_index);
                    MyApp.LogDebug(LOG_TAG,
                            "Upgrade room " + lecture.room + " to index " + new_index);
                    lecture.room_index = new_index;
                }
            }
            // upgrade
            if (lecture.room_index == 0) {
                lecture.room_index = MyApp.roomsMap.get(lecture.room);
            }
        }
        MyApp.room_count = MyApp.roomsMap.size();
        MyApp.LogDebug(LOG_TAG, "room count = " + MyApp.room_count);
        List<Integer> rooms = new ArrayList<>(MyApp.roomsMap.values());
        Collections.sort(rooms);
        int k = 0;
        for (Integer v : rooms) {
            MyApp.LogDebug(LOG_TAG, "room column " + k + " is room " + v);
            MyApp.roomList.append(k, v);
            k++;
        }

        if ((MyApp.lectureList.size() > 0) && (MyApp.lectureList.get(0).dateUTC > 0)) {
            Collections.sort(MyApp.lectureList, new Comparator<Lecture>() {

                @Override
                public int compare(Lecture lhs, Lecture rhs) {
                    if (lhs.dateUTC < rhs.dateUTC) {
                        return -1;
                    }
                    if (lhs.dateUTC > rhs.dateUTC) {
                        return 1;
                    }
                    return 0;
                }

            });
        }

        loadAlarms(context);

        return true;
    }

    public static void loadAlarms(Context context) {
        if (MyApp.lectureList == null) {
            return;
        }

        Cursor alarmCursor;
        SQLiteDatabase alarmdb = null;

        for (Lecture lecture : MyApp.lectureList) {
            lecture.has_alarm = false;
        }

        AlarmsDBOpenHelper alarmDB = new AlarmsDBOpenHelper(context);
        alarmdb = alarmDB.getReadableDatabase();

        try {
            alarmCursor = alarmdb.query(
                    AlarmsTable.NAME,
                    AlarmsDBOpenHelper.allcolumns,
                    null, null, null,
                    null, null);
        } catch (SQLiteException e) {
            e.printStackTrace();
            alarmdb.close();
            alarmdb = null;
            alarmDB.close();
            return;
        }
        MyApp.LogDebug(LOG_TAG, "Got " + alarmCursor.getCount() + " alarm rows.");

        alarmCursor.moveToFirst();
        while (!alarmCursor.isAfterLast()) {
            String lecture_id = alarmCursor.getString(
                    alarmCursor.getColumnIndex(AlarmsTable.Columns.EVENT_ID));
            MyApp.LogDebug(LOG_TAG, "lecture " + lecture_id + " has alarm");

            for (Lecture lecture : MyApp.lectureList) {
                if (lecture.lecture_id.equals(lecture_id)) {
                    lecture.has_alarm = true;
                }
            }
            alarmCursor.moveToNext();
        }
        alarmCursor.close();
        alarmdb.close();
        alarmDB.close();
    }

    @Override
    public void onClick(View v) {
        Lecture lecture = (Lecture) v.getTag();
        MyApp.LogDebug(LOG_TAG, "Click on " + lecture.title);
        MainActivity main = (MainActivity) getActivity();
        if (main != null) {
            main.openLectureDetail(lecture, mDay);
        }
    }

    public void buildNavigationMenu() {
        Time now = new Time();
        now.setToNow();
        StringBuilder currentDate = new StringBuilder();
        currentDate.append(String.format("%d", now.year));
        currentDate.append("-");
        currentDate.append(String.format("%02d", now.month + 1));
        currentDate.append("-");
        currentDate.append(String.format("%02d", now.monthDay));

        MyApp.LogDebug(LOG_TAG, "today is " + currentDate.toString());

        String[] days_menu = new String[MyApp.numdays];
        for (int i = 0; i < MyApp.numdays; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(getString(R.string.day)).append(" ").append(i + 1);
            for (DateInfo dateInfo : MyApp.dateInfos) {
                if (dateInfo.dayIdx == (i + 1)) {
                    MyApp.LogDebug(LOG_TAG, "DateInfo of day '" + sb.toString() + "': " + dateInfo);
                    if (currentDate.toString().equals(dateInfo.date)) {
                        sb.append(" - ");
                        sb.append(getString(R.string.today));
                    }
                    break;
                }
            }
            days_menu[i] = sb.toString();
        }
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                actionBar.getThemedContext(),
                R.layout.support_simple_spinner_dropdown_item_large,
                days_menu);
        arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_list_item);
        actionBar.setListNavigationCallbacks(arrayAdapter, this);
//        actionBar.setDisplayShowTitleEnabled(false);
//        Spinner spinner = (Spinner)getActivity().findViewById(R.id.spinner_toolbar);
//        spinner.setAdapter(arrayAdapter);
    }

    public void onParseDone(Boolean result, String version) {
        if (result) {
            if ((MyApp.numdays == 0) || (!version.equals(MyApp.version))) {
                FahrplanMisc.loadMeta(getActivity());
                FahrplanMisc.loadDays(getActivity());
                if (MyApp.numdays > 1) {
                    buildNavigationMenu();
                }
                SharedPreferences prefs = getActivity().getSharedPreferences(PREFS_NAME, 0);
                mDay = prefs.getInt("displayDay", 1);
                if (mDay > MyApp.numdays) {
                    mDay = 1;
                }
                viewDay(true);
                fillTimes();
            } else {
                viewDay(false);
            }
        } else {
            Toast.makeText(
                    global.getApplicationContext(),
                    getParsingErrorMessage(version),
                    Toast.LENGTH_LONG).show();
        }
        getActivity().invalidateOptionsMenu();
    }

    private String getParsingErrorMessage(final String version) {
        if (version == null || version.length() < 1) {
            return getString(R.string.parsing_error_generic);
        } else {
            return getString(R.string.parsing_error_with_version, version);
        }
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
        FahrplanMisc.addAlarm(getActivity(), lastSelectedLecture, alarmTimesIndex);
        setBell(lastSelectedLecture);
        updateMenuItems();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int menuItemIndex = item.getItemId();
        Lecture lecture = (Lecture) contextMenuView.getTag();
        lastSelectedLecture = lecture;

        MyApp.LogDebug(LOG_TAG, "clicked on " + ((Lecture) contextMenuView.getTag()).lecture_id);

        switch (menuItemIndex) {
            case CONTEXT_MENU_ITEM_ID_FAVORITES:
                if (lecture.highlight) {
                    lecture.highlight = false;
                    FahrplanMisc.writeHighlight(getActivity(), lecture);
                } else {
                    lecture.highlight = true;
                    FahrplanMisc.writeHighlight(getActivity(), lecture);
                }
                setLectureBackground(lecture, contextMenuView);
                setLectureTextColor(lecture, contextMenuView);
                MainActivity main = (MainActivity) getActivity();
                if (main != null) {
                    main.refreshFavoriteList();
                }
                updateMenuItems();
                break;
            case CONTEXT_MENU_ITEM_ID_SET_ALARM:
                showAlarmTimePicker();
                break;
            case CONTEXT_MENU_ITEM_ID_DELETE_ALARM:
                FahrplanMisc.deleteAlarm(getActivity(), lecture);
                setBell(lecture);
                updateMenuItems();
                break;
            case CONTEXT_MENU_ITEM_ID_ADD_TO_CALENDAR:
                FahrplanMisc.addToCalender(getActivity(), lecture);
                break;
            case CONTEXT_MENU_ITEM_ID_SHARE:
                String formattedLecture = SimpleLectureFormat.format(lecture);
                Context context = getContext();
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
        getActivity().supportInvalidateOptionsMenu();
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        contextMenuView = view;
        Lecture lecture = (Lecture) view.getTag();
        if (lecture.highlight) {
            menu.add(0, CONTEXT_MENU_ITEM_ID_FAVORITES, 0, getString(R.string.unflag_as_favorite));
        } else {
            menu.add(0, CONTEXT_MENU_ITEM_ID_FAVORITES, 0, getString(R.string.flag_as_favorite));
        }
        if (lecture.has_alarm) {
            menu.add(0, CONTEXT_MENU_ITEM_ID_DELETE_ALARM, 2, getString(R.string.delete_alarm));
        } else {
            menu.add(0, CONTEXT_MENU_ITEM_ID_SET_ALARM, 1, getString(R.string.set_alarm));
        }
        if (Build.VERSION.SDK_INT >= 14) {
            menu.add(0, CONTEXT_MENU_ITEM_ID_ADD_TO_CALENDAR, 3, getString(R.string.addToCalendar));
        }
        menu.add(0, CONTEXT_MENU_ITEM_ID_SHARE, 4, getString(R.string.share));
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

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if (itemPosition < MyApp.numdays) {
            chooseDay(itemPosition);
            return true;
        }
        return false;
    }

}