package nerd.tuxmobil.fahrplan.congress;

import java.util.ArrayList;
import java.util.HashMap;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

import nerd.tuxmobil.fahrplan.congress.CustomHttpClient.HTTP_STATUS;
import nerd.tuxmobil.fahrplan.congress.MyApp.TASKS;
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
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

public class Fahrplan extends Activity implements OnClickListener {
	private MyApp global;
	private ProgressDialog progress = null;
	private String LOG_TAG = "Fahrplan";
	private FetchFahrplan fetcher;
	private float scale;
	private LayoutInflater inflater;
	private int firstLectureStart = 0;
	private int lastLectureEnd = 0;
	private HashMap<String, Integer> trackColors;
	private int day = 1;
	private View dayTextView;
	public static Context context = null;
	public static String[] rooms = { "Saal 1", "Saal 2", "Saal 3" };
	private FahrplanParser parser;
	private LinearLayout statusBar;
	private Animation slideUpIn;
	private View refreshBtn;
	private Animation slideDownOut;
	private TextView statusLineText;
	private LecturesDBOpenHelper lecturesDB;
	private AlarmsDBOpenHelper alarmDB;
	private MetaDBOpenHelper metaDB;
	private SQLiteDatabase metadb = null;
	private SQLiteDatabase lecturedb = null;
	private SQLiteDatabase highlightdb = null;
	private SQLiteDatabase alarmdb = null;
	private HashMap<String, Integer> trackColorsHi;
	private HighlightDBOpenHelper highlightDB;
	public static final String PREFS_NAME = "settings";
	private ActionBar actionBar = null;
	private int screenWidth = 0;
	private Typeface boldCondensed;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boldCondensed = Typeface.createFromAsset(getAssets(), "Roboto-BoldCondensed.ttf");
		context = this;
		setContentView(R.layout.main);
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
		trackColors.put("Hacking", R.drawable.hacking_event_border);
		trackColors.put("Society", R.drawable.society_event_border);
		trackColors.put("Making", R.drawable.making_event_border);
		trackColors.put("Community", R.drawable.community_event_border);
		trackColors.put("Culture", R.drawable.culture_event_border);
		trackColors.put("Science", R.drawable.science_event_border);
		trackColors.put("Misc", R.drawable.misc_event_border);
		trackColors.put("Show", R.drawable.science_event_border);
		trackColors.put("Society and Politics", R.drawable.science_event_border);
		
		trackColorsHi = new HashMap<String, Integer>();
		trackColorsHi.put("Hacking", R.drawable.hacking_event_border_highlight);
		trackColorsHi.put("Society", R.drawable.society_event_border_highlight);
		trackColorsHi.put("Making", R.drawable.making_event_border_highlight);
		trackColorsHi.put("Community", R.drawable.community_event_border_highlight);
		trackColorsHi.put("Culture", R.drawable.culture_event_border_highlight);
		trackColorsHi.put("Science", R.drawable.science_event_border_highlight);
		trackColorsHi.put("Misc", R.drawable.misc_event_border_highlight);
		trackColorsHi.put("Show", R.drawable.science_event_border_highlight);
		trackColorsHi.put("Society and Politics", R.drawable.science_event_border_highlight);

        actionBar = (ActionBar) findViewById(R.id.actionbar);
        
        dayTextView = actionBar.addAction(new Action() {
            @Override
            public void performAction(View view) {
            	chooseDay();
            }
            @Override
            public int getDrawable() {
                return 0;
            }
            @Override
            public String getText() {
                return getString(R.string.day);
            }
            
            @Override
            public void ready(View view) {
            }
        });
        
        actionBar.addAction(new Action() {
            @Override
            public void performAction(View view) {
            	fetchFahrplan();
            }
            @Override
            public int getDrawable() {
                return R.drawable.refresh_btn;
            }
            @Override
            public String getText() {
                return null;
            }
            
            @Override
            public void ready(View view) {
            	refreshBtn = view;
            }
        });
        
		statusLineText = (TextView) findViewById(R.id.statusLineText);

		statusBar = (LinearLayout) findViewById(R.id.statusLine);
		statusBar.setVisibility(View.GONE);

		slideUpIn = AnimationUtils.loadAnimation(this, R.anim.slide_up_in);
		slideDownOut = AnimationUtils
				.loadAnimation(this, R.anim.slide_down_out);
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
		day = prefs.getInt("displayDay", 1);

		inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		lecturesDB = new LecturesDBOpenHelper(this);
		metaDB = new MetaDBOpenHelper(this);
		highlightDB = new HighlightDBOpenHelper(this);
		alarmDB = new AlarmsDBOpenHelper(this);

		loadMeta();
		loadDays();

		Intent intent = getIntent();
		String lecture_id = intent.getStringExtra("lecture_id");
		
		if (lecture_id != null) {
			Log.d(LOG_TAG,"Open with lecture_id "+lecture_id);
			day = intent.getIntExtra("day", day);
			Log.d(LOG_TAG,"day "+day);
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

	public void showParsingStatus() {
		if (MyApp.numdays == 0) {
			// initial load
			progress = ProgressDialog.show(this, "", getResources().getString(
					R.string.progress_processing_data), true);
		} else {
			statusLineText
					.setText(getString(R.string.progress_processing_data));
			if (statusBar.getVisibility() != View.VISIBLE) {
				refreshBtn.setVisibility(View.GONE);
				actionBar.setProgressBarVisibility(View.VISIBLE);
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
		if (lecturedb != null) lecturedb.close();
		if (highlightdb != null) highlightdb.close();
		if (alarmdb != null) alarmdb.close();
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

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.mainmenu, menu);
		return true;
	}

	public void showFetchingStatus() {
		if (MyApp.numdays == 0) {
			// initial load
			Log.d(LOG_TAG, "fetchFahrplan with numdays == 0");
			progress = ProgressDialog.show(this, "", getResources().getString(
					R.string.progress_loading_data), true);
		} else {
			refreshBtn.setVisibility(View.GONE);
			actionBar.setProgressBarVisibility(View.VISIBLE);
			statusLineText.setText(getString(R.string.progress_loading_data));
			statusBar.setVisibility(View.VISIBLE);
			statusBar.startAnimation(slideUpIn);
		}
	}
	
	public void fetchFahrplan() {
		if (MyApp.task_running == TASKS.NONE) {
			MyApp.task_running = TASKS.FETCH;
			showFetchingStatus();
			fetcher.fetch("/congress/2011/Fahrplan/schedule.de.xml");
		} else {
			Log.d(LOG_TAG, "fetch already in progress");
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		/*
		case R.id.item_refresh:
			fetchFahrplan();
			return true;
		case R.id.item_load:
			loadMeta();
			SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
			day = prefs.getInt("displayDay", 1);
			if (day > MyApp.numdays) {
				day = 1;
			}
			viewDay(true);
			return true;*/
		case R.id.item_choose_day:
			chooseDay();
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
		}
		return super.onOptionsItemSelected(item);
	}

	private void viewDay(boolean reload) {
//		Log.d(LOG_TAG, "viewDay("+reload+")");
		loadLectureList(day, reload);
		scanDayLectures();
		HorizontalSnapScrollView scroller = (HorizontalSnapScrollView) findViewById(R.id.horizScroller);
		if (scroller != null) {
			scroller.scrollTo(0, 0);
		}
		updateRoomTitle(0);

		fillTimes();
		fillRoom("Saal 1", R.id.raum1);
		fillRoom("Saal 2", R.id.raum2);
		fillRoom("Saal 3", R.id.raum3);
//		dayTextView.setText(String
//				.format("%s %d", getString(R.string.day), day));
		actionBar.updateText(dayTextView, String.format("%s %d", getString(R.string.day), day));
		scrollToCurrent(day);
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
		final ScrollView parent = (ScrollView)findViewById(R.id.scrollView1);
		View v = parent.findViewWithTag(lecture);
		ImageView bell = (ImageView)v.findViewById(R.id.bell);
		
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
	
	private void chooseDay() {
		CharSequence items[] = new CharSequence[MyApp.numdays];
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
			items[i] = sb.toString();
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.choose_day));
		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				day = item + 1;
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putInt("displayDay", day);

				editor.commit();

				viewDay(true);
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
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
				actionBar.setProgressBarVisibility(View.GONE);
				statusBar.startAnimation(slideDownOut);
				statusBar.setVisibility(View.GONE);
				refreshBtn.setVisibility(View.VISIBLE);			
				if (MyApp.numdays == 0) {
					if (progress != null) {
						progress.dismiss();
						progress = null;
					}
				} else {
					actionBar.setProgressBarVisibility(View.GONE);
					statusBar.startAnimation(slideDownOut);
					statusBar.setVisibility(View.GONE);
					refreshBtn.setVisibility(View.VISIBLE);
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
			actionBar.setProgressBarVisibility(View.GONE);
			statusBar.startAnimation(slideDownOut);
			statusBar.setVisibility(View.GONE);
			refreshBtn.setVisibility(View.VISIBLE);			
			if (MyApp.numdays == 0) {
				if (progress != null) {
					progress.dismiss();
					progress = null;
				}
			} else {
				actionBar.setProgressBarVisibility(View.GONE);
				statusBar.startAnimation(slideDownOut);
				statusBar.setVisibility(View.GONE);
				refreshBtn.setVisibility(View.VISIBLE);
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
			padding = (int) (8 * scale);
			break;
		default:
			Log.d(LOG_TAG, "other orientation");
			standardHeight = (int) (getResources().getInteger(R.integer.box_height) * scale);
			padding = (int) (10 * scale);
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

				Integer drawable;
				if (lecture.highlight) {
					drawable = trackColorsHi.get(lecture.track);
					padding += (int)(2 * scale);
				} else {
					drawable = trackColors.get(lecture.track);
				}
				if (drawable != null) {
					event.setBackgroundResource(drawable);
					event.setPadding(padding, padding, padding, padding);
				}
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
		
		if (lecturedb == null) {
			lecturedb = lecturesDB.getReadableDatabase();
		}
		Cursor cursor;
		
		try {
			cursor = lecturedb.query("lectures", LecturesDBOpenHelper.allcolumns,
					null, null, null,
					null, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
			lecturedb.close();
			lecturedb = null;
			return;
		}
		
		if (cursor.getCount() == 0) {
			// evtl. Datenbankreset wg. DB Formatänderung -> neu laden
			cursor.close();
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
	}
	
	private void loadLectureList(int day, boolean force) {
		Log.d(LOG_TAG, "load lectures of day " + day);

		if (lecturedb == null) {
			lecturedb = lecturesDB.getReadableDatabase();
		}
		if (highlightdb == null) {
			highlightdb = highlightDB.getReadableDatabase();
		}
		if ((force == false) && (MyApp.lectureList != null) && (MyApp.lectureListDay == day)) return;
		MyApp.lectureList = new ArrayList<Lecture>();
		Cursor cursor, hCursor;

		try {
			cursor = lecturedb.query("lectures", LecturesDBOpenHelper.allcolumns,
					"day=?", new String[] { String.format("%d", day) }, null,
					null, "relStart");
		} catch (SQLiteException e) {
			e.printStackTrace();
			lecturedb.close();
			lecturedb = null;
			highlightdb.close();
			highlightdb = null;
			return;
		}
		try {
			hCursor = highlightdb.query("highlight", HighlightDBOpenHelper.allcolumns,
					null, null, null,
					null, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
			lecturedb.close();
			lecturedb = null;
			highlightdb.close();
			highlightdb = null;
			return;
		}
		Log.d(LOG_TAG, "Got " + cursor.getCount() + " rows.");
		Log.d(LOG_TAG, "Got " + hCursor.getCount() + " highlight rows.");
		
		if (cursor.getCount() == 0) {
			// evtl. Datenbankreset wg. DB Formatänderung -> neu laden
			cursor.close();
			Log.d(LOG_TAG,"fetch on loading empty lecture list");
			fetchFahrplan();
			return;
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
		
		loadAlarms();
	}

	private void loadAlarms() {
		Cursor alarmCursor;
		
		for (Lecture lecture : MyApp.lectureList) {
			lecture.has_alarm = false;
		}
			
		if (alarmdb == null) {
			alarmdb = alarmDB.getReadableDatabase();
		}
		try {
			alarmCursor = alarmdb.query("alarms", AlarmsDBOpenHelper.allcolumns,
					null, null, null,
					null, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
			alarmdb.close();
			alarmdb = null;
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
		startActivity(intent);
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
			actionBar.setProgressBarVisibility(View.GONE);
			statusBar.startAnimation(slideDownOut);
			statusBar.setVisibility(View.GONE);
			refreshBtn.setVisibility(View.VISIBLE);
		}
		if (result) {
			if ((MyApp.numdays == 0) || (!version.equals(MyApp.version))) {
				loadMeta();
				loadDays();
				SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
				day = prefs.getInt("displayDay", 1);
				if (day > MyApp.numdays) {
					day = 1;
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

	void getAlarmTimeDialog(final View v) {

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
								addAlarm(v, alarm);
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.create().show();
	}

	private int[] alarm_times = { 0, 5, 10, 15, 30, 45, 60 };
	private View contextMenuView;
	
	public void deleteAlarm(Lecture lecture) {
		AlarmsDBOpenHelper alarmDB = new AlarmsDBOpenHelper(this);
		SQLiteDatabase db = alarmDB.getWritableDatabase();
		Cursor cursor;
		
		try {
		cursor = db.query("alarms", AlarmsDBOpenHelper.allcolumns,
					"eventid=?", new String[] { lecture.lecture_id }, null,
					null, null);
		} catch (SQLiteException e) {
			e.printStackTrace();
			Log.d(LOG_TAG,"failure on alarm query");
			db.close();
			return;
		} 
		
		if (cursor.getCount() == 0) {
			db.close();
			cursor.close();
			Log.d(LOG_TAG, "alarm for " + lecture.lecture_id + " not found");
			return;
		}
		
		cursor.moveToFirst();
		
		Intent intent = new Intent(this, AlarmReceiver.class);
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
		
		AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingintent = PendingIntent.getBroadcast(this, Integer.parseInt(lecture.lecture_id), intent, 0);
		
		// Cancel any existing alarms for this lecture
		alarmManager.cancel(pendingintent);
		
		lecture.has_alarm = false;
		setBell(lecture);
	}
	
	public void addAlarm(View v, int alarmTime) {
		Lecture lecture = (Lecture) v.getTag();
		Time time = lecture.getTime();
		long startTime = time.normalize(true);
		long when = time.normalize(true) - (alarm_times[alarmTime] * 60 * 1000);
		
		// DEBUG
		// when = System.currentTimeMillis() + (30 * 1000);
		
		time.set(when);
		Log.d(LOG_TAG, "Alarm time: "+when);
		
		
		Intent intent = new Intent(this, AlarmReceiver.class);
		intent.putExtra("lecture_id", lecture.lecture_id);
		intent.putExtra("day", lecture.day);
		intent.putExtra("title", lecture.title);
		intent.putExtra("startTime", startTime);
		
		intent.setAction("de.machtnix.fahrplan.ALARM");
		intent.setData(Uri.parse("alarm://"+lecture.lecture_id));
		
		AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingintent = PendingIntent.getBroadcast(this, Integer.parseInt(lecture.lecture_id), intent, 0);
		
		// Cancel any existing alarms for this lecture
		alarmManager.cancel(pendingintent);
		
		// Set new alarm
		alarmManager.set(AlarmManager.RTC_WAKEUP, when, pendingintent);
		
		// write to DB
		
		AlarmsDBOpenHelper alarmDB = new AlarmsDBOpenHelper(this);

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
		setBell(lecture);
	}
	
	public void writeHighlight(Lecture lecture) {
		HighlightDBOpenHelper highlightDB = new HighlightDBOpenHelper(this);

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
	
	public boolean onContextItemSelected(MenuItem item) {
		int menuItemIndex = item.getItemId();
		Lecture lecture = (Lecture)contextMenuView.getTag();
		
		Log.d(LOG_TAG,"clicked on "+((Lecture)contextMenuView.getTag()).lecture_id);
		
		switch (menuItemIndex) {
		case 0:
			Integer drawable;
			int padding = getEventPadding();
			if (lecture.highlight) {
				drawable = trackColors.get(lecture.track);
				lecture.highlight = false;
				writeHighlight(lecture);
			} else {
				drawable = trackColorsHi.get(lecture.track);
				lecture.highlight = true;
				writeHighlight(lecture);
				padding += (int)(2 * scale);
			}
			if (drawable != null) {
				contextMenuView.setBackgroundResource(drawable);
				contextMenuView.setPadding(padding, padding, padding, padding);
			}
			break;
		case 1:
			getAlarmTimeDialog(contextMenuView);
			break;
		case 2:
			deleteAlarm(lecture);
			break;
		}
		return true;
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, 0, 0, getString(R.string.toggle_highlight));
		menu.add(0, 1, 1, getString(R.string.set_alarm));
		contextMenuView = v;
		Lecture lecture = (Lecture)contextMenuView.getTag();
		if (lecture.has_alarm) {
			menu.add(0, 2, 2, getString(R.string.delete_alarm));
		}
	}

	public void refreshViews() {
		for (Lecture lecture : MyApp.lectureList) {
			setBell(lecture);
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		super.onActivityResult(requestCode, resultCode, intent);

		switch (requestCode) {
			case MyApp.ALARMLIST:
				Log.d(LOG_TAG, "Return from AlarmList with result " + resultCode);
				if (resultCode == RESULT_OK) {
					Log.d(LOG_TAG, "Reload alarms");
					loadAlarms();
					refreshViews();
				}
				break;
		}
	}
}