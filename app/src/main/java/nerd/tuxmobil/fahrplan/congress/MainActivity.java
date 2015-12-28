package nerd.tuxmobil.fahrplan.congress;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import nerd.tuxmobil.fahrplan.congress.CustomHttpClient.HTTP_STATUS;
import nerd.tuxmobil.fahrplan.congress.FahrplanContract.FragmentTags;
import nerd.tuxmobil.fahrplan.congress.MyApp.TASKS;

public class MainActivity extends AppCompatActivity implements
        OnParseCompleteListener,
        OnDownloadCompleteListener,
        OnCloseDetailListener,
        OnRefreshEventMarkers,
        OnCertAccepted,
        AbstractListFragment.OnLectureListClick,
        FragmentManager.OnBackStackChangedListener,
        ConfirmationDialog.OnConfirmationDialogClicked {

    private static final String LOG_TAG = "MainActivity";

    private FetchFahrplan fetcher;

    private FahrplanParser parser;

    private ProgressDialog progress = null;

    private MyApp global;
    private ProgressBar progressBar = null;
    private boolean showUpdateAction = true;
    private static MainActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        MyApp.LogDebug(LOG_TAG, "onCreate");
        setContentView(R.layout.main_layout);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        progressBar = (ProgressBar)findViewById(R.id.progress);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.fahrplan);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_actionbar_logo);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorActionBar)));

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
                if ((MyApp.numdays == 0) && (savedInstanceState == null)) {
                    MyApp.LogDebug(LOG_TAG, "fetch in onCreate bc. numdays==0");
                    fetchFahrplan(this);
                }
                break;
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (findViewById(R.id.schedule) != null) {
            FragmentManager fm = getSupportFragmentManager();
            if (fm.findFragmentByTag(FragmentTags.SCHEDULE) == null) {
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction
                        .replace(R.id.schedule, new FahrplanFragment(), FragmentTags.SCHEDULE);
                fragmentTransaction.commit();
            }
        }

        if (findViewById(R.id.detail) == null) {
            FragmentManager fm = getSupportFragmentManager();
            Fragment detail = fm.findFragmentByTag(FragmentTags.DETAIL);
            if (detail != null) {
                FragmentTransaction ft = fm.beginTransaction();
                ft.remove(detail).commit();
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        MyApp.LogDebug(LOG_TAG, "onNewIntent");
        setIntent(intent);
    }

    public void parseFahrplan() {
        showParsingStatus();
        MyApp.task_running = TASKS.PARSE;
        parser.setListener(this);
        parser.parse(MyApp.fahrplan_xml, MyApp.eTag);
    }

    public void onGotResponse(HTTP_STATUS status, String response, String eTagStr, String host) {
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
                case HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE:
                    CertificateDialogFragment dlg = new CertificateDialogFragment();
                    dlg.show(getSupportFragmentManager(), CertificateDialogFragment.FRAGMENT_TAG);
                    break;
            }
            CustomHttpClient.showHttpError(this, global, status, host);
            progressBar.setVisibility(View.INVISIBLE);
            showUpdateAction = true;
            supportInvalidateOptionsMenu();
            return;
        }
        MyApp.LogDebug(LOG_TAG, "yehhahh");
        progressBar.setVisibility(View.INVISIBLE);
        showUpdateAction = true;
        supportInvalidateOptionsMenu();

        MyApp.fahrplan_xml = response;
        MyApp.eTag = eTagStr;
        parseFahrplan();
    }

    @Override
    public void onParseDone(Boolean result, String version) {
        MyApp.LogDebug(LOG_TAG, "parseDone: " + result + " , numdays=" + MyApp.numdays);
        MyApp.task_running = TASKS.NONE;
        MyApp.fahrplan_xml = null;

        if (MyApp.numdays == 0) {
            if (progress != null) {
                progress.dismiss();
                progress = null;
            }
        }
        progressBar.setVisibility(View.INVISIBLE);
        showUpdateAction = true;
        supportInvalidateOptionsMenu();
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(FragmentTags.SCHEDULE);
        if ((fragment != null) && (fragment instanceof OnParseCompleteListener)) {
            ((OnParseCompleteListener) fragment).onParseDone(result, version);
        }
        fragment = fm.findFragmentByTag(FragmentTags.CHANGES);
        if ((fragment != null) && (fragment instanceof ChangeListFragment)) {
            ((ChangeListFragment) fragment).onRefresh();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean(BundleKeys.PREFS_CHANGES_SEEN, true) == false) changesDialog();
    }

    public void showFetchingStatus() {
        if (MyApp.numdays == 0) {
            // initial load
            MyApp.LogDebug(LOG_TAG, "fetchFahrplan with numdays == 0");
            progress = ProgressDialog.show(this, "", getResources().getString(
                    R.string.progress_loading_data), true);
        } else {
            MyApp.LogDebug(LOG_TAG, "show fetch status");
            progressBar.setVisibility(View.VISIBLE);
            showUpdateAction = false;
            supportInvalidateOptionsMenu();
        }
    }

    public void showParsingStatus() {
        if (MyApp.numdays == 0) {
            // initial load
            progress = ProgressDialog.show(this, "", getResources().getString(
                    R.string.progress_processing_data), true);
        } else {
            MyApp.LogDebug(LOG_TAG, "show parse status");
            progressBar.setVisibility(View.VISIBLE);
            showUpdateAction = false;
            supportInvalidateOptionsMenu();
        }
    }

    public void fetchFahrplan(OnDownloadCompleteListener completeListener) {
        if (MyApp.task_running == TASKS.NONE) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String alternateURL = prefs.getString(BundleKeys.PREFS_SCHEDULE_URL, null);
            String url;
            if (!TextUtils.isEmpty(alternateURL)) {
                url = alternateURL;
            } else {
                url = BuildConfig.SCHEDULE_URL;
            }

            MyApp.task_running = TASKS.FETCH;
            showFetchingStatus();
            fetcher.setListener(completeListener);
            fetcher.fetch(url, MyApp.eTag);
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
    protected void onPause() {
        if (MyApp.fetcher != null) {
            MyApp.fetcher.setListener(null);
        }
        if (MyApp.parser != null) {
            MyApp.parser.setListener(null);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MyApp.fetcher != null) {
            MyApp.fetcher.setListener(this);
        }
        if (MyApp.parser != null) {
            MyApp.parser.setListener(this);
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean(BundleKeys.PREFS_CHANGES_SEEN, true) == false) changesDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.mainmenu, menu);
        MenuItem item = menu.findItem(R.id.item_refresh);
        if (item != null) {
            item.setVisible(showUpdateAction);
        }
        return true;
    }

    void changesDialog() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag("changesDialog");

        if (fragment == null) {
            LectureList changedLectures = FahrplanMisc.readChanges(this);
            DialogFragment about = ChangesDialog.newInstance(
                    MyApp.version,
                    FahrplanMisc.getChangedLectureCount(changedLectures, false),
                    FahrplanMisc.getNewLectureCount(changedLectures, false),
                    FahrplanMisc.getCancelledLectureCount(changedLectures, false),
                    FahrplanMisc.getChangedLectureCount(changedLectures, true) +
                            FahrplanMisc.getNewLectureCount(changedLectures, true) +
                            FahrplanMisc.getCancelledLectureCount(changedLectures, true));
            about.show(getSupportFragmentManager(), "changesDialog");
        }
    }

    void aboutDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        DialogFragment about = new AboutDialog();
        about.show(ft, "about");
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
                intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, MyApp.SETTINGS);
                return true;
            case R.id.item_changes:
                openLectureChanges();
                return true;
            case R.id.item_starred_list:
                FrameLayout sidePane = (FrameLayout) findViewById(R.id.detail);
                if (sidePane == null) {
                    intent = new Intent(this, StarredListActivity.class);
                    startActivityForResult(intent, MyApp.STARRED);
                } else {
                    FragmentManager fm = getSupportFragmentManager();
                    sidePane.setVisibility(View.VISIBLE);
                    FragmentTransaction fragmentTransaction = fm.beginTransaction();
                    StarredListFragment starred = StarredListFragment.newInstance(true);
                    fragmentTransaction.replace(R.id.detail, starred, FragmentTags.STARRED);
                    fragmentTransaction.addToBackStack(FragmentTags.STARRED);
                    fragmentTransaction.commit();
                }
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    public void openLectureDetail(Lecture lecture, int mDay) {
        if (lecture == null) return;
        FrameLayout sidePane = (FrameLayout) findViewById(R.id.detail);
        MyApp.LogDebug(LOG_TAG, "openLectureDetail sidePane=" + sidePane);
        if (sidePane != null) {
            FragmentManager fm = getSupportFragmentManager();
            sidePane.setVisibility(View.VISIBLE);
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            EventDetailFragment ev = new EventDetailFragment();
            Bundle args = new Bundle();
            args.putString(BundleKeys.EVENT_TITLE, lecture.title);
            args.putString(BundleKeys.EVENT_SUBTITLE, lecture.subtitle);
            args.putString(BundleKeys.EVENT_ABSTRACT, lecture.abstractt);
            args.putString(BundleKeys.EVENT_DESCRIPTION, lecture.description);
            args.putString(BundleKeys.EVENT_SPEAKERS, lecture.getFormattedSpeakers());
            args.putString(BundleKeys.EVENT_LINKS, lecture.links);
            args.putString(BundleKeys.EVENT_ID, lecture.lecture_id);
            args.putInt(BundleKeys.EVENT_TIME, lecture.startTime);
            args.putInt(BundleKeys.EVENT_DAY, mDay);
            args.putString(BundleKeys.EVENT_ROOM, lecture.room);
            args.putBoolean(BundleKeys.SIDEPANE, true);
            ev.setArguments(args);
            fragmentTransaction.replace(R.id.detail, ev, FragmentTags.DETAIL);
            fragmentTransaction.addToBackStack(FragmentTags.DETAIL);
            fragmentTransaction.commit();
        } else {
            EventDetail.startForResult(this, lecture, mDay);
        }
    }

    @Override
    public void closeDetailView() {
        View sidePane = findViewById(R.id.detail);
        if (sidePane != null) {
            sidePane.setVisibility(View.GONE);
        }
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(FragmentTags.DETAIL);
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.remove(fragment).commit();
        }
    }

    public void reloadAlarms() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(FragmentTags.SCHEDULE);
        if (fragment != null) {
            ((FahrplanFragment) fragment).loadAlarms(this);
        }
    }
    @Override
    public void refreshEventMarkers() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(FragmentTags.SCHEDULE);
        if (fragment != null) {
            ((FahrplanFragment) fragment).refreshEventMarkers();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case MyApp.ALARMLIST:
            case MyApp.EVENTVIEW:
            case MyApp.CHANGELOG:
            case MyApp.STARRED:
                if (resultCode == Activity.RESULT_OK) {
                    refreshEventMarkers();
                }
                break;
            case MyApp.SETTINGS:
                if ((resultCode == Activity.RESULT_OK) && (intent.getBooleanExtra(BundleKeys.PREFS_ALTERNATIVE_HIGHLIGHT, false))) {
                    if (findViewById(R.id.schedule) != null) {
                        FragmentManager fm = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fm.beginTransaction();
                        fragmentTransaction
                                .replace(R.id.schedule, new FahrplanFragment(), FragmentTags.SCHEDULE);
                        fragmentTransaction.commit();
                    }
                }
        }
    }

    @Override
    public void cert_accepted() {
        MyApp.LogDebug(LOG_TAG, "fetch on cert accepted.");
        fetchFahrplan(MainActivity.this);
    }

    @Override
    public void onLectureListClick(Lecture lecture) {
        if (lecture != null) openLectureDetail(lecture, lecture.day );
    }

    @Override
    public void onBackStackChanged() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment pane = fm.findFragmentById(R.id.detail);
        if (pane == null) {
            View sidePane = findViewById(R.id.detail);
            if (sidePane != null)  {
                sidePane.setVisibility(View.GONE);
            }
        }
        supportInvalidateOptionsMenu();
    }

    public void refreshFavoriteList() {
        FragmentManager fm = getSupportFragmentManager();
        StarredListFragment favoriteFragment = (StarredListFragment)fm.findFragmentByTag(FragmentTags.STARRED);
        if (favoriteFragment != null) {
            favoriteFragment.onRefresh();
        }
    }

    public void openLectureChanges() {
        FrameLayout sidePane = (FrameLayout) findViewById(R.id.detail);
        if (sidePane == null) {
            Intent intent = new Intent(this, ChangeListActivity.class);
            startActivityForResult(intent, MyApp.CHANGELOG);
        } else {
            FragmentManager fm = getSupportFragmentManager();
            sidePane.setVisibility(View.VISIBLE);
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            ChangeListFragment changes = ChangeListFragment.newInstance(true);
            fragmentTransaction.replace(R.id.detail, changes, FragmentTags.CHANGES);
            fragmentTransaction.addToBackStack(FragmentTags.CHANGES);
            fragmentTransaction.commit();
        }
    }

    @Override
    public void onAccepted(int dlgRequestCode) {
        switch (dlgRequestCode) {
            case StarredListFragment.DELETE_ALL_FAVORITES_REQUEST_CODE:
                FragmentManager fm = getSupportFragmentManager();
                StarredListFragment fragment = (StarredListFragment)fm.findFragmentByTag(
                        FahrplanContract.FragmentTags.STARRED);
                if (fragment != null) {
                    fragment.deleteAllFavorites();
                }
                break;
        }
    }

    @Override
    public void onDenied(int dlgRequestCode) {
    }

    public static MainActivity getInstance() {
        return instance;
    }
}
