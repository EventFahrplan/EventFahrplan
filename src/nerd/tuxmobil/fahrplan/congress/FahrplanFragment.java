package nerd.tuxmobil.fahrplan.congress;

import java.util.ArrayList;
import java.util.HashMap;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Time;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class FahrplanFragment extends SherlockFragment implements OnClickListener, OnNavigationListener, OnParseCompleteListener {
	private MyApp global;
	private static String LOG_TAG = "Fahrplan";
	private float scale;
	private LayoutInflater inflater;
	private int firstLectureStart = 0;
	private int lastLectureEnd = 0;
	private HashMap<String, Integer> trackColors;
	private int mDay = 1;
	private View dayTextView;
	public static Context context = null;
	public static String[] rooms = { "Saal 1", "Saal 2", "Saal G", "Saal 6" };
	private HashMap<String, Integer> trackColorsHi;
	public static final String PREFS_NAME = "settings";
	private int screenWidth = 0;
	private Typeface boldCondensed;
	private Typeface light;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
		boldCondensed = Typeface.createFromAsset(getSherlockActivity().getAssets(), "Roboto-BoldCondensed.ttf");
		light = Typeface.createFromAsset(getSherlockActivity().getAssets(), "Roboto-Light.ttf");
		context = getSherlockActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.schedule, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		TextView roomName = (TextView)view.findViewById(R.id.roomName);
		if (roomName != null) roomName.setTypeface(light);
		roomName = (TextView)view.findViewById(R.id.roomName1);
		if (roomName != null) roomName.setTypeface(light);
		roomName = (TextView)view.findViewById(R.id.roomName2);
		if (roomName != null) roomName.setTypeface(light);
		roomName = (TextView)view.findViewById(R.id.roomName3);
		if (roomName != null) roomName.setTypeface(light);
		roomName = (TextView)view.findViewById(R.id.roomName4);
		if (roomName != null) roomName.setTypeface(light);
		global = (MyApp) getSherlockActivity().getApplicationContext();
		scale = getResources().getDisplayMetrics().density;
		screenWidth = (int) (getResources().getDisplayMetrics().widthPixels / scale);
		MyApp.LogDebug(LOG_TAG, "screen width = " + screenWidth);
		screenWidth -= 38;	// Breite für Zeitenspalte
		switch (getResources().getConfiguration().orientation) {
			case Configuration.ORIENTATION_PORTRAIT:
				if (view.findViewById(R.id.horizScroller) != null) {
					int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
		                     (float) screenWidth, getResources().getDisplayMetrics());
					MyApp.LogDebug(LOG_TAG, "adjust column width to " + width);
					LinearLayout l = (LinearLayout) view.findViewById(R.id.raum1);
					LayoutParams p = (LayoutParams) l.getLayoutParams();
					p.width = width;
					l.setLayoutParams(p);
					l = (LinearLayout) view.findViewById(R.id.raum2);
					l.setLayoutParams(p);
					l = (LinearLayout) view.findViewById(R.id.raum3);
					l.setLayoutParams(p);
					l = (LinearLayout) view.findViewById(R.id.raum4);
					l.setLayoutParams(p);
				}
				break;
		}

		trackColors = new HashMap<String, Integer>();
		trackColors.put("Art & Beauty", R.drawable.event_border_default_art_beauty);
		trackColors.put("CCC", R.drawable.event_border_default_ccc);
		trackColors.put("Entertainment", R.drawable.event_border_default_entertainment);
		trackColors.put("Ethics, Society & Politics", R.drawable.event_border_default_ethics_society_politics);
		trackColors.put("Hardware & Making", R.drawable.event_border_default_hardware_making);
		trackColors.put("Other", R.drawable.event_border_default_other);
		trackColors.put("Science & Engineering", R.drawable.event_border_default_science_engineering);
		trackColors.put("Security & Safety", R.drawable.event_border_default_security_safety);
		trackColors.put("", R.drawable.event_border_default);

		trackColorsHi = new HashMap<String, Integer>();
		trackColorsHi.put("Art & Beauty", R.drawable.event_border_highlight_art_beauty);
		trackColorsHi.put("CCC", R.drawable.event_border_highlight_ccc);
		trackColorsHi.put("Entertainment", R.drawable.event_border_highlight_entertainment);
		trackColorsHi.put("Ethics, Society & Politics", R.drawable.event_border_highlight_ethics_society_politics);
		trackColorsHi.put("Hardware & Making", R.drawable.event_border_highlight_hardware_making);
		trackColorsHi.put("Other", R.drawable.event_border_highlight_other);
		trackColorsHi.put("Science & Engineering", R.drawable.event_border_highlight_science_engineering);
		trackColorsHi.put("Security & Safety", R.drawable.event_border_highlight_security_safety);
		trackColorsHi.put("", R.drawable.event_border_highlight);

		SharedPreferences prefs = getSherlockActivity().getSharedPreferences(PREFS_NAME, 0);
		mDay = prefs.getInt("displayDay", 1);

		inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		Intent intent = getSherlockActivity().getIntent();
		String lecture_id = intent.getStringExtra("lecture_id");

		if (lecture_id != null) {
			MyApp.LogDebug(LOG_TAG,"Open with lecture_id "+lecture_id);
			mDay = intent.getIntExtra("day", mDay);
			MyApp.LogDebug(LOG_TAG,"day "+mDay);
		}

		if (MyApp.numdays > 0) {
			build_navigation_menu();
		}
		MyApp.fetcher.setListener((OnDownloadCompleteListener)getSherlockActivity());	// save current activity and trigger possible completion event
		MyApp.parser.setListener((OnParseCompleteListener)getSherlockActivity());		// save current activity and trigger possible completion event

		switch (MyApp.task_running) {
		case FETCH:
			MyApp.LogDebug(LOG_TAG, "fetch was pending, restart");
			if (MyApp.numdays != 0) viewDay(false);
			break;
		case PARSE:
			MyApp.LogDebug(LOG_TAG, "parse was pending, restart");
			break;
		case NONE:
			if (MyApp.numdays != 0) {
				viewDay(lecture_id != null);	// auf jeden Fall reload, wenn mit Lecture ID gestartet
			}
			break;
		}

		if (lecture_id != null) {
			scrollTo(lecture_id);
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

	@Override
	public void onResume() {
		MyApp.LogDebug(LOG_TAG, "onResume");
		super.onResume();
		fillTimes();
		getSherlockActivity().supportInvalidateOptionsMenu();
	}

	public static void updateRoomTitle(int room) {
		if (context != null) {
			TextView roomName = (TextView) ((Activity) context)
					.findViewById(R.id.roomName);
			if (roomName != null) {
				try {
					roomName.setText(rooms[room]);
				} catch (ArrayIndexOutOfBoundsException e) {
					roomName.setText(String.format("unknown %d", room));
				}
			}
		}
	}

	private void viewDay(boolean reload) {
//		Log.d(LOG_TAG, "viewDay("+reload+")");

		if (loadLectureList(getSherlockActivity(), mDay, reload) == false) {
			MyApp.LogDebug(LOG_TAG,"fetch on loading empty lecture list");
		// FIXME
//			fetchFahrplan();
		}
		scanDayLectures();
		HorizontalSnapScrollView scroller = (HorizontalSnapScrollView) getView().findViewById(R.id.horizScroller);
		if (scroller != null) {
			scroller.scrollTo(0, 0);
		}
		updateRoomTitle(0);

		fillTimes();
		fillRoom("Saal 1", R.id.raum1);
		fillRoom("Saal 2", R.id.raum2);
		fillRoom("Saal G", R.id.raum3);
		fillRoom("Saal 6", R.id.raum4);
		scrollToCurrent(mDay);
		ActionBar actionbar = getSherlockActivity().getSupportActionBar();
		if (actionbar != null) {
			actionbar.setSelectedNavigationItem(mDay-1);
		}
	}

	/**
	 * jump to current time or lecture, if we are on today's lecture list
	 * @param day
	 */
	private void scrollToCurrent(int day) {
		int height;
//		Log.d(LOG_TAG, "lectureListDay: " + MyApp.lectureListDay);
		if (MyApp.lectureListDay != DateList.getIndexOfToday(MyApp.dateList, MyApp.dayChangeHour, MyApp.dayChangeMinute)) return;
		Time now = new Time();
		now.setToNow();
		HorizontalSnapScrollView horiz = null;

		switch (getResources().getConfiguration().orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			height = (int) (getResources().getInteger(R.integer.box_height) * scale);
			break;
		default:
			height = (int) (getResources().getInteger(R.integer.box_height) * scale);
			horiz = (HorizontalSnapScrollView)getView().findViewById(R.id.horizScroller);
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

		if (!((((now.hour * 60) + now.minute) < firstLectureStart) && DateList.sameDay(now, MyApp.lectureListDay, MyApp.dateList))) {
			while (time < lastLectureEnd) {
				int hour = printTime / 60;
				int minute = printTime % 60;
				if ((now.hour == hour) && (now.minute >= minute)
						&& (now.minute < (minute + 15))) {
					break;
				} else {
					scrollAmount += (height * 3);
				}
				time += 15;
				printTime = time;
				if (printTime >= (24 * 60)) {
					printTime -= (24 * 60);
				}
			}

			for (Lecture l : MyApp.lectureList) {
				if ((l.day == day) && (l.startTime <= time) && (l.startTime + l.duration > time)) {
					if ((col == -1) || ((col >= 0) && (l.room.equals(rooms[col])))) {
						MyApp.LogDebug(LOG_TAG, l.title);
						MyApp.LogDebug(LOG_TAG, time + " " + l.startTime + "/" + l.duration);
						scrollAmount -= ((time - l.startTime)/5) * height;
						time = l.startTime;
					}
				}
			}
		} else {
//			Log.d(LOG_TAG, "we are before "+firstLectureStart+" "+((now.hour * 60) + now.minute));
		}

//		Log.d(LOG_TAG, "scrolltoCurrent to "+scrollAmount);

		final int pos = scrollAmount;
		final ScrollView scrollView = (ScrollView)getView().findViewById(R.id.scrollView1);
		scrollView.scrollTo(0, scrollAmount);
		scrollView.post(new Runnable() {

			@Override
			public void run() {
				scrollView.scrollTo(0, pos);
			}
		});
	}

	private void setBell(Lecture lecture)
	{
		ScrollView parent = (ScrollView)getView().findViewById(R.id.scrollView1);
		if (parent == null)	return;
		View v = parent.findViewWithTag(lecture);
		if (v == null) return;
		ImageView bell = (ImageView)v.findViewById(R.id.bell);
		if (bell == null) return;

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
				final ScrollView parent = (ScrollView)getView().findViewById(R.id.scrollView1);
				final int pos = (lecture.relStartTime - firstLectureStart)/5 * height;
				MyApp.LogDebug(LOG_TAG, "position is "+pos);
				parent.post(new Runnable() {

					@Override
					public void run() {
						parent.scrollTo(0, pos);
					}
				});
				if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
					final HorizontalSnapScrollView horiz = (HorizontalSnapScrollView)getView().findViewById(R.id.horizScroller);
					if (horiz != null) {
						for (int i = 0; i < rooms.length; i++) {
							if (rooms[i].equals(lecture.room)) {
								MyApp.LogDebug(LOG_TAG,"scroll horiz to "+i);
								final int hpos = i;
								horiz.post(new Runnable() {

									@Override
									public void run() {
										horiz.scrollToColumn(hpos);
									}
								});
								break;
							}
						}
					}

				}
				break;
			}
		}
	}

	private void chooseDay(int chosenDay) {
		mDay = chosenDay + 1;
		SharedPreferences settings = getSherlockActivity().getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("displayDay", mDay);

		editor.commit();

		viewDay(true);
	}

	private void scanDayLectures() {
		firstLectureStart = -1;
		lastLectureEnd = -1;
		for (Lecture lecture : MyApp.lectureList) {
			if (firstLectureStart == -1) {
				firstLectureStart = lecture.relStartTime;
			} else if (lecture.relStartTime < firstLectureStart) {
				firstLectureStart = lecture.relStartTime;
			}
			if (lastLectureEnd == -1) {
				lastLectureEnd = lecture.relStartTime + lecture.duration;
			} else if ((lecture.relStartTime + lecture.duration) > lastLectureEnd) {
				lastLectureEnd = lecture.relStartTime + lecture.duration;
			}
		}
	}

	private void fillTimes() {
		int time = firstLectureStart;
		int printTime = time;
		LinearLayout timeSpalte = (LinearLayout) getView().findViewById(R.id.times_layout);
		timeSpalte.removeAllViews();
		int height;
		Time now = new Time();
		now.setToNow();
		View event;

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

		while (time < lastLectureEnd) {
			StringBuilder sb = new StringBuilder();
			int hour = printTime / 60;
			int minute = printTime % 60;
			sb.append(String.format("%02d", hour)).append(":");
			sb.append(String.format("%02d", minute));
			if ((now.hour == hour) && (now.minute >= minute)
					&& (now.minute < (minute + 15))) {
				event = inflater.inflate(R.layout.time_layout_now, null);
			} else {
				event = inflater.inflate(R.layout.time_layout, null);
			}
			timeSpalte.addView(event, LayoutParams.MATCH_PARENT, height * 3);
			TextView title = (TextView) event.findViewById(R.id.time);
			title.setText(sb.toString());
			time += 15;
			printTime = time;
			if (printTime >= (24 * 60)) {
				printTime -= (24 * 60);
			}
		}
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

	private void setLectureBackground(Lecture lecture, View view) {
		Integer drawable;
		int padding = getEventPadding();
		if (lecture.highlight) {
			drawable = trackColorsHi.get(lecture.track);
		} else {
			drawable = trackColors.get(lecture.track);
		}
		if (drawable != null) {
			view.setBackgroundResource(drawable);
			view.setPadding(padding, padding, padding, padding);
		}
	}

	private void fillRoom(String roomName, int roomId) {
		LinearLayout room = (LinearLayout) getView().findViewById(roomId);
		room.removeAllViews();
		int endTime = firstLectureStart;
		int padding = getEventPadding();
		int standardHeight;

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
		for (Lecture lecture : MyApp.lectureList) {
			if (roomName.equals(lecture.room)) {
				if (lecture.relStartTime > endTime) {
					View event = new View(getSherlockActivity());
					int height = (int) (standardHeight
							* (lecture.relStartTime - endTime) / 5);
					room.addView(event, LayoutParams.MATCH_PARENT, height);
				}
				View event = inflater.inflate(R.layout.event_layout, null);
				int height = (int) (standardHeight * (lecture.duration / 5));
				ImageView bell = (ImageView) event.findViewById(R.id.bell);
				if (lecture.has_alarm) {
					bell.setVisibility(View.VISIBLE);
				} else {
					bell.setVisibility(View.GONE);
				}
				room.addView(event, LayoutParams.MATCH_PARENT, height);
				TextView title = (TextView) event
						.findViewById(R.id.event_title);
				title.setTypeface(boldCondensed);
				title.setText(lecture.title);
				title = (TextView) event.findViewById(R.id.event_subtitle);
				title.setText(lecture.subtitle);
				title = (TextView) event.findViewById(R.id.event_speakers);
				title.setText(lecture.speakers.replaceAll(";", ", "));
				title = (TextView) event.findViewById(R.id.event_track);
				StringBuilder sb = new StringBuilder();
				sb.append(lecture.track);
				if ((lecture.lang != null) && (lecture.lang.length() > 0)) {
					sb.append(" [").append(lecture.lang).append("]");
				}
				title.setText(sb.toString());

				setLectureBackground(lecture, event);
				event.setOnClickListener(this);
				event.setLongClickable(true);
//				event.setOnLongClickListener(this);
				event.setOnCreateContextMenuListener(this);
				event.setTag(lecture);
				endTime = lecture.relStartTime + lecture.duration;
			}
		}
	}

	public static boolean loadLectureList(Context context, int day, boolean force) {
		MyApp.LogDebug(LOG_TAG, "load lectures of day " + day);

		if ((force == false) && (MyApp.lectureList != null) && (MyApp.lectureListDay == day)) return true;

		SQLiteDatabase lecturedb = null;
		LecturesDBOpenHelper lecturesDB = new LecturesDBOpenHelper(context);
		lecturedb = lecturesDB.getReadableDatabase();

		HighlightDBOpenHelper highlightDB = new HighlightDBOpenHelper(context);
		SQLiteDatabase highlightdb = highlightDB.getReadableDatabase();

		MyApp.lectureList = new ArrayList<Lecture>();
		Cursor cursor, hCursor;

		try {
			cursor = lecturedb.query("lectures", LecturesDBOpenHelper.allcolumns,
					"day=?", new String[] { String.format("%d", day) }, null,
					null, "relStart");
		} catch (SQLiteException e) {
			e.printStackTrace();
			lecturedb.close();
			highlightdb.close();
			lecturesDB.close();
			return false;
		}
		try {
			hCursor = highlightdb.query("highlight", HighlightDBOpenHelper.allcolumns,
					null, null, null,
					null, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
			lecturedb.close();
			highlightdb.close();
			lecturesDB.close();
			return false;
		}
		MyApp.LogDebug(LOG_TAG, "Got " + cursor.getCount() + " rows.");
		MyApp.LogDebug(LOG_TAG, "Got " + hCursor.getCount() + " highlight rows.");

		if (cursor.getCount() == 0) {
			cursor.close();
			lecturedb.close();
			highlightdb.close();
			lecturesDB.close();
			return false;
		}

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Lecture lecture = new Lecture(cursor.getString(0));

			lecture.title = cursor.getString(1);
			lecture.subtitle = cursor.getString(2);
			lecture.day = cursor.getInt(3);
			lecture.room = cursor.getString(4);
			lecture.startTime = cursor.getInt(5);
			lecture.duration = cursor.getInt(6);
			lecture.speakers = cursor.getString(7);
			lecture.track = cursor.getString(8);
			lecture.type = cursor.getString(9);
			lecture.lang = cursor.getString(10);
			lecture.abstractt = cursor.getString(11);
			lecture.description = cursor.getString(12);
			lecture.relStartTime = cursor.getInt(13);
			lecture.date = cursor.getString(14);
			lecture.links = cursor.getString(15);

			MyApp.lectureList.add(lecture);
			cursor.moveToNext();
		}
		cursor.close();
		MyApp.lectureListDay = day;

		hCursor.moveToFirst();
		while (!hCursor.isAfterLast()) {
			String lecture_id = hCursor.getString(1);
			int highlighted = hCursor.getInt(2);
			MyApp.LogDebug(LOG_TAG, "lecture "+lecture_id+" is hightlighted:"+highlighted);

			for (Lecture lecture : MyApp.lectureList) {
				if (lecture.lecture_id.equals(lecture_id)) {
					lecture.highlight = (highlighted == 1 ? true : false);
				}
			}
			hCursor.moveToNext();
		}
		hCursor.close();

		loadAlarms(context);

		highlightdb.close();
		lecturedb.close();
		lecturesDB.close();
		return true;
	}

	public static void loadAlarms(Context context) {
		Cursor alarmCursor;
		SQLiteDatabase alarmdb = null;

		for (Lecture lecture : MyApp.lectureList) {
			lecture.has_alarm = false;
		}

		AlarmsDBOpenHelper alarmDB = new AlarmsDBOpenHelper(context);
		alarmdb = alarmDB.getReadableDatabase();

		try {
			alarmCursor = alarmdb.query("alarms", AlarmsDBOpenHelper.allcolumns,
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
			String lecture_id = alarmCursor.getString(4);
			MyApp.LogDebug(LOG_TAG, "lecture "+lecture_id+" has alarm");

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
		Intent intent = new Intent(getSherlockActivity(), EventDetail.class);
		intent.putExtra("title", lecture.title);
		intent.putExtra("subtitle", lecture.subtitle);
		intent.putExtra("abstract", lecture.abstractt);
		intent.putExtra("descr", lecture.description);
		intent.putExtra("spkr", lecture.speakers.replaceAll(";", ", "));
		intent.putExtra("links", lecture.links);
		intent.putExtra("eventid", lecture.lecture_id);
		intent.putExtra("time", lecture.startTime);
		intent.putExtra("day", mDay);
		startActivityForResult(intent, MyApp.EVENTVIEW);
	}

	public void build_navigation_menu() {
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
			for (DateList d : MyApp.dateList) {
				if (d.dayIdx == (i + 1)) {
					MyApp.LogDebug(LOG_TAG, "date of day " + sb.toString() + " is " + d.date);
					if (currentDate.toString().equals(d.date)) {
						sb.append(" - ");
						sb.append(getString(R.string.today));
					}
					break;
				}
			}
			days_menu[i] = sb.toString();
		}
		ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getSherlockActivity(),
		    R.layout.sherlock_spinner_dropdown_item, days_menu);
		arrayAdapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		actionBar.setListNavigationCallbacks(arrayAdapter, this);
		actionBar.setDisplayShowTitleEnabled(false);
	}

	public void onParseDone(Boolean result, String version) {
		if (result) {
			if ((MyApp.numdays == 0) || (!version.equals(MyApp.version))) {
				FahrplanMisc.loadMeta(getSherlockActivity());
				FahrplanMisc.loadDays(getSherlockActivity());
				build_navigation_menu();
				SharedPreferences prefs = getSherlockActivity().getSharedPreferences(PREFS_NAME, 0);
				mDay = prefs.getInt("displayDay", 1);
				if (mDay > MyApp.numdays) {
					mDay = 1;
				}
				viewDay(true);
				final Toast done = Toast.makeText(global
						.getApplicationContext(), String.format(
						getString(R.string.aktualisiert_auf), version),
						Toast.LENGTH_LONG);
				done.show();
			} else {
				viewDay(false);
			}
		} else {
			// FIXME Fehlermeldung;
		}
		getSherlockActivity().supportInvalidateOptionsMenu();
	}

	void getAlarmTimeDialog(final Lecture lecture) {

		LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.reminder_dialog,
				(ViewGroup) getView().findViewById(R.id.layout_root));

		final Spinner spinner = (Spinner) layout.findViewById(R.id.spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(getSherlockActivity(), R.array.alarm_array,
						android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		TextView msg = (TextView)layout.findViewById(R.id.message);
		msg.setText(R.string.choose_alarm_time);

		new AlertDialog.Builder(getSherlockActivity()).setTitle(R.string.setup_alarm).setView(layout)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								int alarm = spinner.getSelectedItemPosition();
								MyApp.LogDebug(LOG_TAG, "alarm chosen: "+alarm);
								FahrplanMisc.addAlarm(getSherlockActivity(), lecture, alarm);
								setBell(lecture);
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.create().show();
	}

	private View contextMenuView;

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		int menuItemIndex = item.getItemId();
		Lecture lecture = (Lecture)contextMenuView.getTag();

		MyApp.LogDebug(LOG_TAG,"clicked on "+((Lecture)contextMenuView.getTag()).lecture_id);

		switch (menuItemIndex) {
		case 0:
			Integer drawable;
			int padding = getEventPadding();
			if (lecture.highlight) {
				drawable = trackColors.get(lecture.track);
				lecture.highlight = false;
				FahrplanMisc.writeHighlight(getSherlockActivity(), lecture);
			} else {
				drawable = trackColorsHi.get(lecture.track);
				lecture.highlight = true;
				FahrplanMisc.writeHighlight(getSherlockActivity(), lecture);
			}
			if (drawable != null) {
				contextMenuView.setBackgroundResource(drawable);
				contextMenuView.setPadding(padding, padding, padding, padding);
			}
			break;
		case 1:
			getAlarmTimeDialog(lecture);
			break;
		case 2:
			FahrplanMisc.deleteAlarm(getSherlockActivity(), lecture);
			setBell(lecture);
			break;
		case 3:
			FahrplanMisc.addToCalender(getSherlockActivity(), lecture);
			break;
		case 4:
			FahrplanMisc.share(getSherlockActivity(), lecture);
			break;
		}
		return true;
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		contextMenuView = v;
		Lecture lecture = (Lecture)v.getTag();
		if (lecture.highlight) {
			menu.add(0, 0, 0, getString(R.string.unflag_as_favorite));
		} else {
			menu.add(0, 0, 0, getString(R.string.flag_as_favorite));
		}
		if (lecture.has_alarm) {
			menu.add(0, 2, 2, getString(R.string.delete_alarm));
		} else {
			menu.add(0, 1, 1, getString(R.string.set_alarm));
		}
		if (Build.VERSION.SDK_INT >= 14) {
			menu.add(0, 3, 3, getString(R.string.addToCalendar));
		}
		menu.add(0, 4, 4, getString(R.string.share));
	}

	private View getLectureView(Lecture lecture) {
		ScrollView parent = (ScrollView)getView().findViewById(R.id.scrollView1);
		if (parent == null) return null;
		View v = parent.findViewWithTag(lecture);
		return v;
	}

	public void refreshViews() {
		for (Lecture lecture : MyApp.lectureList) {
			setBell(lecture);
			View v = getLectureView(lecture);
			if (v != null) {
				setLectureBackground(lecture, v);
			}
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		super.onActivityResult(requestCode, resultCode, intent);

		switch (requestCode) {
			case MyApp.ALARMLIST:
			case MyApp.EVENTVIEW:
				if (resultCode == SherlockFragmentActivity.RESULT_OK) {
					MyApp.LogDebug(LOG_TAG, "Reload alarms");
					loadAlarms(getSherlockActivity());
					refreshViews();
				}
				break;
		}
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