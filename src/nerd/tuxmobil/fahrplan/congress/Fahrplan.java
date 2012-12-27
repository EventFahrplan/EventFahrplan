package nerd.tuxmobil.fahrplan.congress;

import java.util.ArrayList;
import java.util.HashMap;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import nerd.tuxmobil.fahrplan.congress.CustomHttpClient.HTTP_STATUS;
import nerd.tuxmobil.fahrplan.congress.MyApp.TASKS;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class Fahrplan extends SherlockActivity implements OnClickListener {
	private MyApp global;
	private ProgressDialog progress = null;
	private static String LOG_TAG = "Fahrplan";
	private FetchFahrplan fetcher;
	private float scale;
	private LayoutInflater inflater;
	private int firstLectureStart = 0;
	private int lastLectureEnd = 0;
	private HashMap<String, Integer> trackColors;
	private int mDay = 1;
	private View dayTextView;
	public static Context context = null;
	public static String[] rooms = { "Saal 1", "Saal 4", "Saal 6" };
	private FahrplanParser parser;
	private LinearLayout statusBar;
	private Animation slideUpIn;
	private Animation slideDownOut;
	private TextView statusLineText;
	private MetaDBOpenHelper metaDB;
	private SQLiteDatabase metadb = null;
	private HashMap<String, Integer> trackColorsHi;
	public static final String PREFS_NAME = "settings";
	private int screenWidth = 0;
	private Typeface boldCondensed;
	private Typeface light;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boldCondensed = Typeface.createFromAsset(getAssets(), "Roboto-BoldCondensed.ttf");
		light = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		context = this;
		setContentView(R.layout.main);
		TextView roomName = (TextView)findViewById(R.id.roomName);
		if (roomName != null) roomName.setTypeface(light);
		roomName = (TextView)findViewById(R.id.roomName1);
		if (roomName != null) roomName.setTypeface(light);
		roomName = (TextView)findViewById(R.id.roomName2);
		if (roomName != null) roomName.setTypeface(light);
		roomName = (TextView)findViewById(R.id.roomName3);
		if (roomName != null) roomName.setTypeface(light);
		global = (MyApp) getApplicationContext();
		if (MyApp.fetcher == null) {
			fetcher = new FetchFahrplan();
		} else {
			fetcher = MyApp.fetcher;
		}
		if (MyApp.parser == null) {
			parser = new FahrplanParser(getApplicationContext());
		} else {
			parser = MyApp.parser;
		}
		scale = getResources().getDisplayMetrics().density;
		screenWidth = (int) (getResources().getDisplayMetrics().widthPixels / scale);
		Log.d(LOG_TAG, "screen width = " + screenWidth);
		screenWidth -= 38;	// Breite für Zeitenspalte
		switch (getResources().getConfiguration().orientation) {
			case Configuration.ORIENTATION_PORTRAIT:
				if (findViewById(R.id.horizScroller) != null) {
					int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
		                     (float) screenWidth, getResources().getDisplayMetrics());
					Log.d(LOG_TAG, "adjust column width to " + width);
					LinearLayout l = (LinearLayout) findViewById(R.id.raum1);
					LayoutParams p = (LayoutParams) l.getLayoutParams();
					p.width = width;
					l.setLayoutParams(p);
					l = (LinearLayout) findViewById(R.id.raum2);
					l.setLayoutParams(p);
					l = (LinearLayout) findViewById(R.id.raum3);
					l.setLayoutParams(p);
				}
				break;
		}
		progress = null;

		trackColors = new HashMap<String, Integer>();
//		trackColors.put("Hacking", R.drawable.hacking_event_border);
//		trackColors.put("Society", R.drawable.society_event_border);
//		trackColors.put("Making", R.drawable.making_event_border);
//		trackColors.put("Community", R.drawable.community_event_border);
//		trackColors.put("Culture", R.drawable.culture_event_border);
//		trackColors.put("Science", R.drawable.science_event_border);
//		trackColors.put("Misc", R.drawable.misc_event_border);
//		trackColors.put("Show", R.drawable.science_event_border);
//		trackColors.put("Society and Politics", R.drawable.science_event_border);
		trackColors.put("", R.drawable.event_border);

		trackColorsHi = new HashMap<String, Integer>();
//		trackColorsHi.put("Hacking", R.drawable.hacking_event_border_highlight);
//		trackColorsHi.put("Society", R.drawable.society_event_border_highlight);
//		trackColorsHi.put("Making", R.drawable.making_event_border_highlight);
//		trackColorsHi.put("Community", R.drawable.community_event_border_highlight);
//		trackColorsHi.put("Culture", R.drawable.culture_event_border_highlight);
//		trackColorsHi.put("Science", R.drawable.science_event_border_highlight);
//		trackColorsHi.put("Misc", R.drawable.misc_event_border_highlight);
//		trackColorsHi.put("Show", R.drawable.science_event_border_highlight);
//		trackColorsHi.put("Society and Politics", R.drawable.science_event_border_highlight);
		trackColorsHi.put("", R.drawable.event_border_highlight);

		statusLineText = (TextView) findViewById(R.id.statusLineText);

		statusBar = (LinearLayout) findViewById(R.id.statusLine);
		statusBar.setVisibility(View.GONE);

		slideUpIn = AnimationUtils.loadAnimation(this, R.anim.slide_up_in);
		slideDownOut = AnimationUtils
				.loadAnimation(this, R.anim.slide_down_out);
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
		mDay = prefs.getInt("displayDay", 1);

		inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		metaDB = new MetaDBOpenHelper(this);

		loadMeta();
		loadDays();

		Intent intent = getIntent();
		String lecture_id = intent.getStringExtra("lecture_id");

		if (lecture_id != null) {
			Log.d(LOG_TAG,"Open with lecture_id "+lecture_id);
			mDay = intent.getIntExtra("day", mDay);
			Log.d(LOG_TAG,"day "+mDay);
		}

		MyApp.fetcher.setActivity(this);	// save current activity and trigger possible completion event
		MyApp.parser.setActivity(this);		// save current activity and trigger possible completion event

		switch (MyApp.task_running) {
		case FETCH:
			Log.d(LOG_TAG, "fetch was pending, restart");
			showFetchingStatus();
			viewDay(false);
			break;
		case PARSE:
			Log.d(LOG_TAG, "parse was pending, restart");
			showParsingStatus();
			break;
		case NONE:
			if (MyApp.numdays == 0) {
				Log.d(LOG_TAG,"fetch in onCreate bc. numdays==0");
				fetchFahrplan();
			} else {
				viewDay(lecture_id != null);	// auf jeden Fall reload, wenn mit Lecture ID gestartet
			}
			break;
		}

		if (lecture_id != null) {
			scrollTo(lecture_id);
		}
	}

	@SuppressLint("NewApi")
	public static void addToCalender(Context context, Lecture l) {
		Intent intent = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);

		intent.putExtra(CalendarContract.Events.TITLE, l.title);
		intent.putExtra(CalendarContract.Events.EVENT_LOCATION, l.room);

		Time time = l.getTime();
		long when = time.normalize(true);
		intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, when);
		intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, when + (l.duration * 60000));
		context.startActivity(intent);
	}

	public static void share(Context context, Lecture l) {
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		StringBuilder sb = new StringBuilder();
		Time time = l.getTime();
		sb.append(l.title).append("\n").append(DateFormat.format("E, MMMM dd, yyyy hh:mm", time.toMillis(true)));
		sb.append(", ").append(l.room).append("\n\n").append("http://events.ccc.de/congress/2012/Fahrplan/events/").append(l.lecture_id).append(".en.html");
		sendIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
		sendIntent.setType("text/plain");
		context.startActivity(sendIntent);
	}

	public void showParsingStatus() {
		if (MyApp.numdays == 0) {
			// initial load
			progress = ProgressDialog.show(this, "", getResources().getString(
					R.string.progress_processing_data), true);
		} else {
			statusLineText
					.setText(getString(R.string.progress_processing_data));
			if (statusBar.getVisibility() != View.VISIBLE) {
				statusBar.setVisibility(View.VISIBLE);
				statusBar.startAnimation(slideUpIn);
			}
		}
	}

	public void parseFahrplan() {
		showParsingStatus();
		MyApp.task_running = TASKS.PARSE;
		parser.parse(MyApp.fahrplan_xml);
	}

	@Override
	public void onDestroy() {
		Log.d(LOG_TAG, "onDestroy");
		super.onDestroy();
		if (MyApp.fetcher != null) {
			MyApp.fetcher.setActivity(null);
		}
		if (MyApp.parser != null) {
			MyApp.parser.setActivity(null);
		}
		if (metadb != null) metadb.close();
		if (progress != null) {
			progress.dismiss();
			progress = null;
		}
	}

	@Override
	public void onResume() {
		Log.d(LOG_TAG, "onResume");
		super.onResume();
		fillTimes();
		supportInvalidateOptionsMenu();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = getSupportMenuInflater();
		mi.inflate(R.menu.mainmenu, menu);
		SubMenu submenu = menu.findItem(R.id.item_choose_day).getSubMenu();
		Time now = new Time();
		now.setToNow();
		StringBuilder currentDate = new StringBuilder();
		currentDate.append(String.format("%d", now.year));
		currentDate.append("-");
		currentDate.append(String.format("%02d", now.month + 1));
		currentDate.append("-");
		currentDate.append(String.format("%02d", now.monthDay));

		Log.d(LOG_TAG, "today is " + currentDate.toString());

		for (int i = 0; i < MyApp.numdays; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(getString(R.string.day)).append(" ").append(i + 1);
			for (DateList d : MyApp.dateList) {
				if (d.dayIdx == (i + 1)) {
					Log.d(LOG_TAG, "date of day " + sb.toString() + " is " + d.date);
					if (currentDate.toString().equals(d.date)) {
						sb.append(" - ");
						sb.append(getString(R.string.today));
					}
					break;
				}
			}
			submenu.add(Menu.NONE, i, 0, sb.toString());
		}

		return true;
	}

	public void showFetchingStatus() {
		if (MyApp.numdays == 0) {
			// initial load
			Log.d(LOG_TAG, "fetchFahrplan with numdays == 0");
			progress = ProgressDialog.show(this, "", getResources().getString(
					R.string.progress_loading_data), true);
		} else {
			statusLineText.setText(getString(R.string.progress_loading_data));
			statusBar.setVisibility(View.VISIBLE);
			statusBar.startAnimation(slideUpIn);
		}
	}

	public void fetchFahrplan() {
		if (MyApp.task_running == TASKS.NONE) {
			MyApp.task_running = TASKS.FETCH;
			showFetchingStatus();
			fetcher.fetch("/congress/2012/Fahrplan/schedule.de.xml");
		} else {
			Log.d(LOG_TAG, "fetch already in progress");
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.item_refresh:
			fetchFahrplan();
			return true;
		case R.id.item_about:
			aboutDialog();
			return true;
		case R.id.item_alarms:
			intent = new Intent(this, AlarmList.class);
			startActivityForResult(intent, MyApp.ALARMLIST);
			return true;
		case R.id.item_settings:
			intent = new Intent(this, Prefs.class);
			startActivity(intent);
			return true;
		default:
			if ((item.getItemId() >= 0) && (item.getItemId() < MyApp.numdays)) {
				chooseDay(item.getItemId());
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private void viewDay(boolean reload) {
//		Log.d(LOG_TAG, "viewDay("+reload+")");
		if (loadLectureList(this, mDay, reload) == false) {
			Log.d(LOG_TAG,"fetch on loading empty lecture list");
			fetchFahrplan();
		}
		scanDayLectures();
		HorizontalSnapScrollView scroller = (HorizontalSnapScrollView) findViewById(R.id.horizScroller);
		if (scroller != null) {
			scroller.scrollTo(0, 0);
		}
		updateRoomTitle(0);

		fillTimes();
		fillRoom("Saal 1", R.id.raum1);
		fillRoom("Saal 4", R.id.raum2);
		fillRoom("Saal 6", R.id.raum3);
		scrollToCurrent(mDay);
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
			horiz = (HorizontalSnapScrollView)findViewById(R.id.horizScroller);
			break;
		}

		int col = -1;
		if (horiz != null) {
			col = horiz.getColumn();
			Log.d(LOG_TAG, "y pos  = " + col);
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
					scrollAmount += height;
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
						Log.d(LOG_TAG, l.title);
						Log.d(LOG_TAG, time + " " + l.startTime + "/" + l.duration);
						scrollAmount -= ((time - l.startTime)/15) * height;
						time = l.startTime;
					}
				}
			}
		} else {
//			Log.d(LOG_TAG, "we are before "+firstLectureStart+" "+((now.hour * 60) + now.minute));
		}

//		Log.d(LOG_TAG, "scrolltoCurrent to "+scrollAmount);

		final int pos = scrollAmount;
		final ScrollView scrollView = (ScrollView)findViewById(R.id.scrollView1);
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
		ScrollView parent = (ScrollView)findViewById(R.id.scrollView1);
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
			Log.d(LOG_TAG, "landscape");
			height = (int) (getResources().getInteger(R.integer.box_height) * scale);
			break;
		default:
			Log.d(LOG_TAG, "other orientation");
			height = (int) (getResources().getInteger(R.integer.box_height) * scale);
			break;
		}
		for (Lecture lecture : MyApp.lectureList) {
			if (lecture_id.equals(lecture.lecture_id)) {
				final ScrollView parent = (ScrollView)findViewById(R.id.scrollView1);
				final int pos = (lecture.relStartTime - firstLectureStart)/15 * height;
				Log.d(LOG_TAG, "position is "+pos);
				parent.post(new Runnable() {

					@Override
					public void run() {
						parent.scrollTo(0, pos);
					}
				});
				if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
					final HorizontalSnapScrollView horiz = (HorizontalSnapScrollView)findViewById(R.id.horizScroller);
					if (horiz != null) {
						for (int i = 0; i < rooms.length; i++) {
							if (rooms[i].equals(lecture.room)) {
								Log.d(LOG_TAG,"scroll horiz to "+i);
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
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("displayDay", mDay);

		editor.commit();

		viewDay(true);
	}

	public void onGotResponse(HTTP_STATUS status, String response) {
		Log.d(LOG_TAG, "Response... " + status);
		MyApp.task_running = TASKS.NONE;
		if (status != HTTP_STATUS.HTTP_OK) {
			switch (status) {
				case HTTP_CANCELLED:
					break;
				case HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE: {
					UntrustedCertDialogs.acceptKeyDialog(
							R.string.dlg_certificate_message_fmt, this,
							new cert_accepted() {

								@Override
								public void cert_accepted() {
									Log.d(LOG_TAG, "fetch on cert accepted.");
									fetchFahrplan();
								}
							}, (Object) null);
				}
				break;
			}
			CustomHttpClient.showHttpError(this, global, status);
			if (MyApp.numdays == 0) {
				if (progress != null) {
					progress.dismiss();
					progress = null;
				}
			} else {
				statusBar.startAnimation(slideDownOut);
				statusBar.setVisibility(View.GONE);
				if (MyApp.numdays == 0) {
					if (progress != null) {
						progress.dismiss();
						progress = null;
					}
				} else {
					statusBar.startAnimation(slideDownOut);
					statusBar.setVisibility(View.GONE);
				}
			}
			setProgressBarIndeterminateVisibility(false);
			return;
		}
		Log.d(LOG_TAG, "yehhahh");
		if (MyApp.numdays == 0) {
			if (progress != null) {
				progress.dismiss();
				progress = null;
			}
		} else {
			statusBar.startAnimation(slideDownOut);
			statusBar.setVisibility(View.GONE);
			if (MyApp.numdays == 0) {
				if (progress != null) {
					progress.dismiss();
					progress = null;
				}
			} else {
				statusBar.startAnimation(slideDownOut);
				statusBar.setVisibility(View.GONE);
			}
		}
		setProgressBarIndeterminateVisibility(false);

		MyApp.fahrplan_xml = response;
		parseFahrplan();
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
		LinearLayout timeSpalte = (LinearLayout) findViewById(R.id.times_layout);
		timeSpalte.removeAllViews();
		int height;
		Time now = new Time();
		now.setToNow();
		View event;

		switch (getResources().getConfiguration().orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			Log.d(LOG_TAG, "landscape");
			height = (int) (getResources().getInteger(R.integer.box_height) * scale);
			break;
		default:
			Log.d(LOG_TAG, "other orientation");
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
			timeSpalte.addView(event, LayoutParams.MATCH_PARENT, height);
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
//					drawable = trackColorsHi.get(lecture.track);
			drawable = trackColorsHi.get("");
		} else {
//					drawable = trackColors.get(lecture.track);
			drawable = trackColors.get("");
		}
		if (drawable != null) {
			view.setBackgroundResource(drawable);
			view.setPadding(padding, padding, padding, padding);
		}
	}

	private void fillRoom(String roomName, int roomId) {
		LinearLayout room = (LinearLayout) findViewById(roomId);
		room.removeAllViews();
		int endTime = firstLectureStart;
		int padding = getEventPadding();
		int standardHeight;

		switch (getResources().getConfiguration().orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			Log.d(LOG_TAG, "landscape");
			standardHeight = (int) (getResources().getInteger(R.integer.box_height) * scale);
			break;
		default:
			Log.d(LOG_TAG, "other orientation");
			standardHeight = (int) (getResources().getInteger(R.integer.box_height) * scale);
			break;
		}
		for (Lecture lecture : MyApp.lectureList) {
			if (roomName.equals(lecture.room)) {
				if (lecture.relStartTime > endTime) {
					View event = new View(this);
					int height = (int) (standardHeight
							* (lecture.relStartTime - endTime) / 15);
					room.addView(event, LayoutParams.MATCH_PARENT, height);
				}
				View event = inflater.inflate(R.layout.event_layout, null);
				int height = standardHeight * (lecture.duration / 15);
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

	private void loadDays() {
		MyApp.dateList = new ArrayList<DateList>();
		LecturesDBOpenHelper lecturesDB = new LecturesDBOpenHelper(context);

		SQLiteDatabase lecturedb = lecturesDB.getReadableDatabase();
		Cursor cursor;

		try {
			cursor = lecturedb.query("lectures", LecturesDBOpenHelper.allcolumns,
					null, null, null,
					null, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
			lecturedb.close();
			lecturesDB.close();
			return;
		}

		if (cursor.getCount() == 0) {
			// evtl. Datenbankreset wg. DB Formatänderung -> neu laden
			cursor.close();
			lecturesDB.close();
			lecturedb.close();
			return;
		}

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			int day = cursor.getInt(3);
			String date = cursor.getString(14);

			if (DateList.dateInList(MyApp.dateList, day) == false) {
				MyApp.dateList.add(new DateList(day, date));
			}
			cursor.moveToNext();
		}
		cursor.close();

		for (DateList dayL : MyApp.dateList) {
			Log.d(LOG_TAG, "date day " + dayL.dayIdx + " = " + dayL.date);
		}
		lecturesDB.close();
		lecturedb.close();
	}

	public static boolean loadLectureList(Context context, int day, boolean force) {
		Log.d(LOG_TAG, "load lectures of day " + day);

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
		Log.d(LOG_TAG, "Got " + cursor.getCount() + " rows.");
		Log.d(LOG_TAG, "Got " + hCursor.getCount() + " highlight rows.");

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
			Log.d(LOG_TAG, "lecture "+lecture_id+" is hightlighted:"+highlighted);

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
		Log.d(LOG_TAG, "Got " + alarmCursor.getCount() + " alarm rows.");

		alarmCursor.moveToFirst();
		while (!alarmCursor.isAfterLast()) {
			String lecture_id = alarmCursor.getString(4);
			Log.d(LOG_TAG, "lecture "+lecture_id+" has alarm");

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

	private void loadMeta() {
		if (metadb == null) {
			metadb = metaDB.getReadableDatabase();
		}
		Cursor cursor;
		try {
			cursor = metadb.query("meta", MetaDBOpenHelper.allcolumns, null, null,
					null, null, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
			metadb.close();
			metadb = null;
			return;
		}

		MyApp.numdays = 0;
		MyApp.version = "";
		MyApp.title = "";
		MyApp.subtitle = "";
		MyApp.dayChangeHour = 4;
		MyApp.dayChangeMinute = 0;

		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			if (cursor.getColumnCount() > 0)
				MyApp.numdays = cursor.getInt(0);
			if (cursor.getColumnCount() > 1)
				MyApp.version = cursor.getString(1);
			if (cursor.getColumnCount() > 2)
				MyApp.title = cursor.getString(2);
			if (cursor.getColumnCount() > 3)
				MyApp.subtitle = cursor.getString(3);
			if (cursor.getColumnCount() > 4)
				MyApp.dayChangeHour = cursor.getInt(4);
			if (cursor.getColumnCount() > 5)
				MyApp.dayChangeMinute = cursor.getInt(5);
		}

		Log.d(LOG_TAG, "loadMeta: numdays=" + MyApp.numdays + " version:"
				+ MyApp.version + " " + MyApp.title);
		cursor.close();
	}

	@Override
	public void onClick(View v) {
		Lecture lecture = (Lecture) v.getTag();
		Log.d(LOG_TAG, "Click on " + lecture.title);
		Intent intent = new Intent(this, EventDetail.class);
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

	public void onParseDone(Boolean result, String version) {
		Log.d(LOG_TAG, "parseDone: " + result + " , numdays="+MyApp.numdays);
		MyApp.task_running = TASKS.NONE;
		MyApp.fahrplan_xml = null;

		setProgressBarIndeterminateVisibility(false);
		if (MyApp.numdays == 0) {
			if (progress != null) {
				progress.dismiss();
				progress = null;
			}
		} else {
			statusBar.startAnimation(slideDownOut);
			statusBar.setVisibility(View.GONE);
		}
		if (result) {
			if ((MyApp.numdays == 0) || (!version.equals(MyApp.version))) {
				loadMeta();
				loadDays();
				SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
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
		supportInvalidateOptionsMenu();
	}

	void aboutDialog() {
		LayoutInflater inflater = (LayoutInflater) getApplicationContext()
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.about_dialog,
				(ViewGroup) findViewById(R.id.layout_root));

		TextView text = (TextView) layout.findViewById(R.id.eventVersion);
		text.setText(getString(R.string.fahrplan) + " " + MyApp.version);
		text = (TextView) layout.findViewById(R.id.eventTitle);
		text.setText(MyApp.title);
		Log.d(LOG_TAG, "title:" + MyApp.title);
		text = (TextView) layout.findViewById(R.id.eventSubtitle);
		text.setText(MyApp.subtitle);
		text = (TextView) layout.findViewById(R.id.appVersion);
		try {
			text
					.setText(getString(R.string.appVersion)
							+ " "
							+ getApplicationContext().getPackageManager()
									.getPackageInfo("nerd.tuxmobil.fahrplan.congress", 0).versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			text.setText("");
		}

		new AlertDialog.Builder(this).setTitle(getString(R.string.app_name))
				.setView(layout).setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).create().show();
	}

	void getAlarmTimeDialog(final Lecture lecture) {

		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.reminder_dialog,
				(ViewGroup) findViewById(R.id.layout_root));

		final Spinner spinner = (Spinner) layout.findViewById(R.id.spinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(this, R.array.alarm_array,
						android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		TextView msg = (TextView)layout.findViewById(R.id.message);
		msg.setText(R.string.choose_alarm_time);

		new AlertDialog.Builder(this).setTitle(R.string.setup_alarm).setView(layout)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								int alarm = spinner.getSelectedItemPosition();
								Log.d(LOG_TAG, "alarm chosen: "+alarm);
								addAlarm(Fahrplan.this, lecture, alarm);
								setBell(lecture);
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.create().show();
	}

	private View contextMenuView;

	public static void deleteAlarm(Context context, Lecture lecture) {
		AlarmsDBOpenHelper alarmDB = new AlarmsDBOpenHelper(context);
		SQLiteDatabase db = alarmDB.getWritableDatabase();
		Cursor cursor;

		try {
		cursor = db.query("alarms", AlarmsDBOpenHelper.allcolumns,
					"eventid=?", new String[] { lecture.lecture_id }, null,
					null, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
			Log.d("delete alarm","failure on alarm query");
			db.close();
			return;
		}

		if (cursor.getCount() == 0) {
			db.close();
			cursor.close();
			Log.d("delete_alarm", "alarm for " + lecture.lecture_id + " not found");
			return;
		}

		cursor.moveToFirst();

		Intent intent = new Intent(context, AlarmReceiver.class);
		String lecture_id = cursor.getString(4);
		intent.putExtra("lecture_id", lecture_id);
		int day = cursor.getInt(6);
		intent.putExtra("day", day);
		String title = cursor.getString(1);
		intent.putExtra("title", title);
		long startTime = cursor.getLong(5);
		intent.putExtra("startTime", startTime);
		// delete any previous alarms of this lecture
		db.delete("alarms", "eventid=?", new String[] { lecture.lecture_id });
		db.close();

		intent.setAction("de.machtnix.fahrplan.ALARM");
		intent.setData(Uri.parse("alarm://"+lecture.lecture_id));

		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingintent = PendingIntent.getBroadcast(context, Integer.parseInt(lecture.lecture_id), intent, 0);

		// Cancel any existing alarms for this lecture
		alarmManager.cancel(pendingintent);

		lecture.has_alarm = false;
	}

	public static void addAlarm(Context context, Lecture lecture, int alarmTime) {
		Time time = lecture.getTime();
		long startTime = time.normalize(true);
		int[] alarm_times = { 0, 5, 10, 15, 30, 45, 60 };
		long when = time.normalize(true) - (alarm_times[alarmTime] * 60 * 1000);

		// DEBUG
		// when = System.currentTimeMillis() + (30 * 1000);

		time.set(when);
		Log.d("addAlarm", "Alarm time: "+when);


		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra("lecture_id", lecture.lecture_id);
		intent.putExtra("day", lecture.day);
		intent.putExtra("title", lecture.title);
		intent.putExtra("startTime", startTime);

		intent.setAction("de.machtnix.fahrplan.ALARM");
		intent.setData(Uri.parse("alarm://"+lecture.lecture_id));

		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingintent = PendingIntent.getBroadcast(context, Integer.parseInt(lecture.lecture_id), intent, 0);

		// Cancel any existing alarms for this lecture
		alarmManager.cancel(pendingintent);

		// Set new alarm
		alarmManager.set(AlarmManager.RTC_WAKEUP, when, pendingintent);

		// write to DB

		AlarmsDBOpenHelper alarmDB = new AlarmsDBOpenHelper(context);

		SQLiteDatabase db = alarmDB.getWritableDatabase();

		// delete any previous alarms of this lecture
		try {
			db.beginTransaction();
			db.delete("alarms", "eventid=?", new String[] { lecture.lecture_id });

			ContentValues values = new ContentValues();

			values.put("eventid", Integer.parseInt(lecture.lecture_id));
			values.put("title", lecture.title);
			values.put("time", when);
			values.put("timeText", time.format("%Y-%m-%d %H:%M"));
			values.put("displayTime", startTime);
			values.put("day", lecture.day);

			db.insert("alarms", null, values);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
		} finally {
			db.endTransaction();
			db.close();
		}

		lecture.has_alarm = true;
	}

	public static void writeHighlight(Context context, Lecture lecture) {
		HighlightDBOpenHelper highlightDB = new HighlightDBOpenHelper(context);

		SQLiteDatabase db = highlightDB.getWritableDatabase();

		try {
			db.beginTransaction();
			db.delete("highlight", "eventid=?", new String[] { lecture.lecture_id });

			ContentValues values = new ContentValues();

			values.put("eventid", Integer.parseInt(lecture.lecture_id));
			values.put("highlight", lecture.highlight ? 1 : 0);

			db.insert("highlight", null, values);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		int menuItemIndex = item.getItemId();
		Lecture lecture = (Lecture)contextMenuView.getTag();

		Log.d(LOG_TAG,"clicked on "+((Lecture)contextMenuView.getTag()).lecture_id);

		switch (menuItemIndex) {
		case 0:
			Integer drawable;
			int padding = getEventPadding();
			if (lecture.highlight) {
//				drawable = trackColors.get(lecture.track);
				drawable = trackColors.get("");
				lecture.highlight = false;
				writeHighlight(this, lecture);
			} else {
//				drawable = trackColorsHi.get(lecture.track);
				drawable = trackColorsHi.get("");
				lecture.highlight = true;
				writeHighlight(this, lecture);
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
			deleteAlarm(this, lecture);
			setBell(lecture);
			break;
		case 3:
			addToCalender(this, lecture);
			break;
		case 4:
			share(this, lecture);
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
		ScrollView parent = (ScrollView)findViewById(R.id.scrollView1);
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
				if (resultCode == RESULT_OK) {
					Log.d(LOG_TAG, "Reload alarms");
					loadAlarms(this);
					refreshViews();
				}
				break;
		}
	}

}