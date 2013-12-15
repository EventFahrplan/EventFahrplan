package nerd.tuxmobil.fahrplan.congress;

import nerd.tuxmobil.fahrplan.congress.CustomHttpClient.HTTP_STATUS;
import nerd.tuxmobil.fahrplan.congress.MyApp.TASKS;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends SherlockFragmentActivity implements OnParseCompleteListener, OnDownloadCompleteListener {

	private static final String LOG_TAG = "MainActivity";
	private FetchFahrplan fetcher;
	private FahrplanParser parser;
	private ProgressDialog progress = null;
	private TextView statusLineText;
	private LinearLayout statusBar;
	private Animation slideUpIn;
	private Animation slideDownOut;
	private MyApp global;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_layout);
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
		statusLineText = (TextView) findViewById(R.id.statusLineText);

		statusBar = (LinearLayout) findViewById(R.id.statusLine);
		statusBar.setVisibility(View.GONE);

		slideUpIn = AnimationUtils.loadAnimation(this, R.anim.slide_up_in);
		slideDownOut = AnimationUtils.loadAnimation(this, R.anim.slide_down_out);

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
				fetchFahrplan();
			}
			break;
		}

		if (findViewById(R.id.schedule) != null) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = fm.beginTransaction();
			fragmentTransaction.replace(R.id.schedule, new FahrplanFragment(), "schedule");
			fragmentTransaction.commit();
		}

	}

	public void parseFahrplan() {
		showParsingStatus();
		MyApp.task_running = TASKS.PARSE;
		parser.parse(MyApp.fahrplan_xml, MyApp.eTag);
	}

	public void onGotResponse(HTTP_STATUS status, String response, String eTagStr) {
		MyApp.LogDebug(LOG_TAG, "Response... " + status);
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
									MyApp.LogDebug(LOG_TAG, "fetch on cert accepted.");
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
				}
			}
			setProgressBarIndeterminateVisibility(false);
			return;
		}
		MyApp.LogDebug(LOG_TAG, "yehhahh");
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
			}
		}
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

		setProgressBarIndeterminateVisibility(false);
		if (MyApp.numdays == 0) {
			if (progress != null) {
				progress.dismiss();
				progress = null;
			}
		} else {
			MyApp.LogDebug(LOG_TAG, "hide status");
			statusBar.startAnimation(slideDownOut);
			statusBar.setVisibility(View.GONE);
		}
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
			statusLineText.setText(getString(R.string.progress_loading_data));
			statusBar.setVisibility(View.VISIBLE);
			statusBar.startAnimation(slideUpIn);
		}
	}

	public void showParsingStatus() {
		if (MyApp.numdays == 0) {
			// initial load
			progress = ProgressDialog.show(this, "", getResources().getString(
					R.string.progress_processing_data), true);
		} else {
			MyApp.LogDebug(LOG_TAG, "show parse status");
			statusLineText.setText(getString(R.string.progress_processing_data));
			if (statusBar.getVisibility() != View.VISIBLE) {
				statusBar.setVisibility(View.VISIBLE);
				statusBar.startAnimation(slideUpIn);
			}
		}
	}

	public void fetchFahrplan() {
		if (MyApp.task_running == TASKS.NONE) {
			MyApp.task_running = TASKS.FETCH;
			showFetchingStatus();
			fetcher.fetch("/congress/2013/Fahrplan/schedule.xml", MyApp.eTag);
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
		}
		return super.onOptionsItemSelected(item);
	}

}
