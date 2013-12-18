package nerd.tuxmobil.fahrplan.congress;

import nerd.tuxmobil.fahrplan.congress.CustomHttpClient.HTTP_STATUS;
import nerd.tuxmobil.fahrplan.congress.MyApp.TASKS;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.format.Time;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends SherlockFragmentActivity implements OnParseCompleteListener, OnDownloadCompleteListener, OnCloseDetailListener, OnRefreshEventMarers {

	private static final String LOG_TAG = "MainActivity";
	private FetchFahrplan fetcher;
	private FahrplanParser parser;
	private ProgressDialog progress = null;
	private MyApp global;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MyApp.LogDebug(LOG_TAG, "onCreate");
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.main_layout);
		getSupportActionBar().setTitle(R.string.fahrplan);
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
		progress = null;
		global = (MyApp) getApplicationContext();

		FahrplanMisc.loadMeta(this);
		FahrplanMisc.loadDays(this);

		MyApp.LogDebug(LOG_TAG, "task_running:" + MyApp.task_running);
		switch (MyApp.task_running) {
		case FETCH:
			MyApp.LogDebug(LOG_TAG, "fetch was pending, restart");
			showFetchingStatus();
			break;
		case PARSE:
			MyApp.LogDebug(LOG_TAG, "parse was pending, restart");
			showParsingStatus();
			break;
		case NONE:
			if (MyApp.numdays == 0) {
				MyApp.LogDebug(LOG_TAG,"fetch in onCreate bc. numdays==0");
				fetchFahrplan(this);
			}
			break;
		}

		if (findViewById(R.id.schedule) != null) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = fm.beginTransaction();
			fragmentTransaction.replace(R.id.schedule, new FahrplanFragment(), "schedule");
			fragmentTransaction.commit();
		}

		if (findViewById(R.id.detail) == null) {
			FragmentManager fm = getSupportFragmentManager();
			Fragment detail = fm.findFragmentByTag("detail");
			if (detail != null) {
				FragmentTransaction ft = fm.beginTransaction();
				ft.remove(detail).commit();
			}
		}
	}

	public void parseFahrplan() {
		showParsingStatus();
		MyApp.task_running = TASKS.PARSE;
		parser.setListener(this);
		parser.parse(MyApp.fahrplan_xml, MyApp.eTag);
	}

	public void onGotResponse(HTTP_STATUS status, String response, String eTagStr) {
		MyApp.LogDebug(LOG_TAG, "Response... " + status);
		MyApp.task_running = TASKS.NONE;
		if (MyApp.numdays == 0) {
			if (progress != null) {
				progress.dismiss();
				progress = null;
			}
		}
		if ((status == HTTP_STATUS.HTTP_OK) || (status == HTTP_STATUS.HTTP_NOT_MODIFIED)) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			Time now = new Time();
			now.setToNow();
			long millis = now.toMillis(true);
			Editor edit = prefs.edit();
			edit.putLong("last_fetch", millis);
			edit.commit();
		}
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
									MyApp.LogDebug(LOG_TAG, "fetch on cert accepted.");
									fetchFahrplan(MainActivity.this);
								}
							}, (Object) null);
				}
				break;
			}
			CustomHttpClient.showHttpError(this, global, status);
			setProgressBarIndeterminateVisibility(false);
			return;
		}
		MyApp.LogDebug(LOG_TAG, "yehhahh");
		setProgressBarIndeterminateVisibility(false);

		MyApp.fahrplan_xml = response;
		MyApp.eTag = eTagStr;
		parseFahrplan();
	}

	@Override
	public void onParseDone(Boolean result, String version) {
		MyApp.LogDebug(LOG_TAG, "parseDone: " + result + " , numdays="+MyApp.numdays);
		MyApp.task_running = TASKS.NONE;
		MyApp.fahrplan_xml = null;

		if (MyApp.numdays == 0) {
			if (progress != null) {
				progress.dismiss();
				progress = null;
			}
		}
		setProgressBarIndeterminateVisibility(false);
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentByTag("schedule");
		if ((fragment != null) && (fragment instanceof OnParseCompleteListener)) {
			((OnParseCompleteListener)fragment).onParseDone(result, version);
		}
	}

	public void showFetchingStatus() {
		if (MyApp.numdays == 0) {
			// initial load
			MyApp.LogDebug(LOG_TAG, "fetchFahrplan with numdays == 0");
			progress = ProgressDialog.show(this, "", getResources().getString(
					R.string.progress_loading_data), true);
		} else {
			MyApp.LogDebug(LOG_TAG, "show fetch status");
			setProgressBarIndeterminateVisibility(true);
		}
	}

	public void showParsingStatus() {
		if (MyApp.numdays == 0) {
			// initial load
			progress = ProgressDialog.show(this, "", getResources().getString(
					R.string.progress_processing_data), true);
		} else {
			MyApp.LogDebug(LOG_TAG, "show parse status");
			setProgressBarIndeterminateVisibility(true);
		}
	}

	public void fetchFahrplan(OnDownloadCompleteListener completeListener) {
		if (MyApp.task_running == TASKS.NONE) {
			MyApp.task_running = TASKS.FETCH;
			showFetchingStatus();
			fetcher.setListener(completeListener);
			fetcher.fetch(MyApp.schedulePath, MyApp.eTag);
		} else {
			MyApp.LogDebug(LOG_TAG, "fetch already in progress");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (progress != null) {
			progress.dismiss();
			progress = null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = getSupportMenuInflater();
		mi.inflate(R.menu.mainmenu, menu);
		return true;
	}

	void aboutDialog() {
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.about_dialog, (ViewGroup) findViewById(R.id.layout_root));

		TextView text = (TextView) layout.findViewById(R.id.eventVersion);
		text.setText(getString(R.string.fahrplan) + " " + MyApp.version);
		text = (TextView) layout.findViewById(R.id.eventTitle);
		text.setText(MyApp.title);
		MyApp.LogDebug(LOG_TAG, "title:" + MyApp.title);
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

		TextView logo_copyright = (TextView)layout.findViewById(R.id.copyright_logo);
		logo_copyright.setText(Html.fromHtml(getString(R.string.copyright_logo)));
		logo_copyright.setMovementMethod(LinkMovementMethod.getInstance());

		new AlertDialog.Builder(this).setTitle(getString(R.string.app_name))
				.setView(layout).setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).create().show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.item_refresh:
			fetchFahrplan(this);
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
		}
		return super.onOptionsItemSelected(item);
	}

	public void openLectureDetail(Lecture lecture, int mDay) {
		FrameLayout sidePane = (FrameLayout) findViewById(R.id.detail);
		MyApp.LogDebug(LOG_TAG, "openLectureDetail sidePane="+sidePane);
		if (sidePane != null) {
			sidePane.setVisibility(View.VISIBLE);
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = fm.beginTransaction();
			EventDetailFragment ev = new EventDetailFragment();
			Bundle args = new Bundle();
			args.putString("title", lecture.title);
			args.putString("subtitle", lecture.subtitle);
			args.putString("abstract", lecture.abstractt);
			args.putString("descr", lecture.description);
			args.putString("spkr", lecture.speakers.replaceAll(";", ", "));
			args.putString("links", lecture.links);
			args.putString("eventid", lecture.lecture_id);
			args.putInt("time", lecture.startTime);
			args.putInt("day", mDay);
			args.putBoolean("sidepane", true);
			ev.setArguments(args);
			fragmentTransaction.replace(R.id.detail, ev, "detail");
			fragmentTransaction.commit();
		} else {
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
	}

	@Override
	public void closeDetailView() {
		View sidePane = findViewById(R.id.detail);
		if (sidePane != null) sidePane.setVisibility(View.GONE);
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentByTag("detail");
		if (fragment != null) {
			FragmentTransaction fragmentTransaction = fm.beginTransaction();
			fragmentTransaction.remove(fragment).commit();
		}
	}

	@Override
	public void refreshEventMarkers() {
		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentByTag("schedule");
		if (fragment != null) {
			((FahrplanFragment)fragment).refreshEventMarkers();
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		super.onActivityResult(requestCode, resultCode, intent);

		switch (requestCode) {
			case MyApp.ALARMLIST:
			case MyApp.EVENTVIEW:
				if (resultCode == SherlockFragmentActivity.RESULT_OK) {
					refreshEventMarkers();
				}
				break;
		}
	}

}
