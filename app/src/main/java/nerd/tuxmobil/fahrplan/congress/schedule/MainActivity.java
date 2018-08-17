package nerd.tuxmobil.fahrplan.congress.schedule;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import org.ligi.snackengage.SnackEngage;
import org.ligi.snackengage.SnackEngageBuilder;
import org.ligi.snackengage.conditions.AfterNumberOfOpportunities;
import org.ligi.snackengage.conditions.NeverAgainWhenClickedOnce;
import org.ligi.snackengage.snacks.BaseSnack;
import org.ligi.snackengage.snacks.DefaultRateSnack;
import org.ligi.snackengage.snacks.OpenURLSnack;

import java.util.List;

import nerd.tuxmobil.fahrplan.congress.BuildConfig;
import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.MyApp.TASKS;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.about.AboutDialog;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmList;
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment;
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity;
import nerd.tuxmobil.fahrplan.congress.changes.ChangeListActivity;
import nerd.tuxmobil.fahrplan.congress.changes.ChangeListFragment;
import nerd.tuxmobil.fahrplan.congress.changes.ChangesDialog;
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys;
import nerd.tuxmobil.fahrplan.congress.details.EventDetail;
import nerd.tuxmobil.fahrplan.congress.details.EventDetailFragment;
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListActivity;
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListFragment;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;
import nerd.tuxmobil.fahrplan.congress.navigation.C3navSnack;
import nerd.tuxmobil.fahrplan.congress.net.CertificateDialogFragment;
import nerd.tuxmobil.fahrplan.congress.net.CustomHttpClient;
import nerd.tuxmobil.fahrplan.congress.net.CustomHttpClient.HTTP_STATUS;
import nerd.tuxmobil.fahrplan.congress.net.FetchFahrplan;
import nerd.tuxmobil.fahrplan.congress.reporting.TraceDroidEmailSender;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;
import nerd.tuxmobil.fahrplan.congress.serialization.FahrplanParser;
import nerd.tuxmobil.fahrplan.congress.settings.SettingsActivity;
import nerd.tuxmobil.fahrplan.congress.sidepane.OnSidePaneCloseListener;
import nerd.tuxmobil.fahrplan.congress.utils.ConfirmationDialog;
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc;

public class MainActivity extends BaseActivity implements
        FahrplanParser.OnParseCompleteListener,
        FetchFahrplan.OnDownloadCompleteListener,
        OnSidePaneCloseListener,
        FahrplanFragment.OnRefreshEventMarkers,
        CertificateDialogFragment.OnCertAccepted,
        AbstractListFragment.OnLectureListClick,
        FragmentManager.OnBackStackChangedListener,
        ConfirmationDialog.OnConfirmationDialogClicked {

    private static final String LOG_TAG = "MainActivity";
    private static final String VENUE_LEIPZIG_MESSE = "leipzig-messe";

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
        Toolbar toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.fahrplan);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        int actionBarColor = ContextCompat.getColor(this, R.color.colorActionBar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionBarColor));

        TraceDroidEmailSender.sendStackTraces(this);

        if (MyApp.fetcher == null) {
            fetcher = new FetchFahrplan();
        } else {
            fetcher = MyApp.fetcher;
        }
        AppRepository appRepository = AppRepository.Companion.getInstance(this);
        if (MyApp.parser == null) {
            parser = new FahrplanParser(getApplicationContext(), appRepository);
        } else {
            parser = MyApp.parser;
        }
        progress = null;
        global = (MyApp) getApplicationContext();

        MyApp.meta = appRepository.readMeta();
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
                if ((MyApp.meta.getNumDays() == 0) && (savedInstanceState == null)) {
                    MyApp.LogDebug(LOG_TAG, "fetch in onCreate bc. numDays==0");
                    fetchFahrplan(this);
                }
                break;
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (findViewById(R.id.schedule) != null) {
            if (findFragment(FahrplanFragment.FRAGMENT_TAG) == null) {
                replaceFragment(R.id.schedule, new FahrplanFragment(),
                        FahrplanFragment.FRAGMENT_TAG);
            }
        }

        if (findViewById(R.id.detail) == null) {
            removeFragment(EventDetailFragment.FRAGMENT_TAG);
        }

        initUserEngagement();
    }

    private void initUserEngagement() {
        int actionColor = ContextCompat.getColor(this, R.color.colorAccent);
        final BaseSnack snack = new DefaultRateSnack()
                .overrideTitleText(getString(R.string.snack_engage_rate_title))
                .overrideActionText(getString(R.string.snack_engage_rate_action));
        snack.setActionColor(actionColor);

        SnackEngageBuilder snackEngageBuilder = SnackEngage.from(this);

        if (VENUE_LEIPZIG_MESSE.equals(BuildConfig.VENUE)) {
            OpenURLSnack c3navSnack = new C3navSnack(this);
            c3navSnack.withConditions(
                    new NeverAgainWhenClickedOnce(),
                    new AfterNumberOfOpportunities(7));
            snackEngageBuilder.withSnack(c3navSnack);
        }

        snackEngageBuilder
                .withSnack(snack)
                .build()
                .engageWhenAppropriate();
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
        parser.parse(MyApp.fahrplan_xml, MyApp.meta.getETag());
    }

    public void onGotResponse(HTTP_STATUS status, String response, String eTagStr, String host) {
        MyApp.LogDebug(LOG_TAG, "Response... " + status);
        MyApp.task_running = TASKS.NONE;
        if (MyApp.meta.getNumDays() == 0) {
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
        MyApp.meta.setETag(eTagStr);
        parseFahrplan();
    }

    @Override
    public void onParseDone(Boolean result, String version) {
        MyApp.LogDebug(LOG_TAG, "parseDone: " + result + " , numDays=" + MyApp.meta.getNumDays());
        MyApp.task_running = TASKS.NONE;
        MyApp.fahrplan_xml = null;

        if (MyApp.meta.getNumDays() == 0) {
            if (progress != null) {
                progress.dismiss();
                progress = null;
            }
        }
        progressBar.setVisibility(View.INVISIBLE);
        showUpdateAction = true;
        supportInvalidateOptionsMenu();
        Fragment fragment = findFragment(FahrplanFragment.FRAGMENT_TAG);
        if (fragment != null && fragment instanceof FahrplanParser.OnParseCompleteListener) {
            ((FahrplanParser.OnParseCompleteListener) fragment).onParseDone(result, version);
        }
        fragment = findFragment(ChangeListFragment.FRAGMENT_TAG);
        if (fragment != null && fragment instanceof ChangeListFragment) {
            ((ChangeListFragment) fragment).onRefresh();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean(BundleKeys.PREFS_CHANGES_SEEN, true)) {
            showChangesDialog();
        }
    }

    public void showFetchingStatus() {
        if (MyApp.meta.getNumDays() == 0) {
            // initial load
            MyApp.LogDebug(LOG_TAG, "fetchFahrplan with numDays == 0");
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
        if (MyApp.meta.getNumDays() == 0) {
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

    public void fetchFahrplan(FetchFahrplan.OnDownloadCompleteListener completeListener) {
        if (MyApp.task_running == TASKS.NONE) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String defaultScheduleUrl = getString(R.string.preferences_schedule_url_default_value);
            String alternateURL = prefs.getString(BundleKeys.PREFS_SCHEDULE_URL, defaultScheduleUrl);
            String url;
            if (!TextUtils.isEmpty(alternateURL)) {
                url = alternateURL;
            } else {
                url = BuildConfig.SCHEDULE_URL;
            }

            MyApp.task_running = TASKS.FETCH;
            showFetchingStatus();
            fetcher.setListener(completeListener);
            fetcher.fetch(url, MyApp.meta.getETag());
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
        if (prefs.getBoolean(BundleKeys.PREFS_CHANGES_SEEN, true) == false) {
            showChangesDialog();
        }
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

    void showChangesDialog() {
        Fragment fragment = findFragment(ChangesDialog.FRAGMENT_TAG);
        if (fragment == null) {
            List<Lecture> changedLectures = FahrplanMisc.readChanges(this);
            DialogFragment about = ChangesDialog.newInstance(
                    MyApp.meta.getVersion(),
                    FahrplanMisc.getChangedLectureCount(changedLectures, false),
                    FahrplanMisc.getNewLectureCount(changedLectures, false),
                    FahrplanMisc.getCancelledLectureCount(changedLectures, false),
                    FahrplanMisc.getChangedLectureCount(changedLectures, true) +
                            FahrplanMisc.getNewLectureCount(changedLectures, true) +
                            FahrplanMisc.getCancelledLectureCount(changedLectures, true));
            about.show(getSupportFragmentManager(), ChangesDialog.FRAGMENT_TAG);
        }
    }

    void showAboutDialog() {
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
                showAboutDialog();
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
                openFavorites();
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    public void openLectureDetail(Lecture lecture, int mDay) {
        if (lecture == null) return;
        FrameLayout sidePane = findViewById(R.id.detail);
        MyApp.LogDebug(LOG_TAG, "openLectureDetail sidePane=" + sidePane);
        if (sidePane != null) {
            FragmentManager fm = getSupportFragmentManager();
            fm.popBackStack(EventDetailFragment.FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
            args.putString(BundleKeys.EVENT_SLUG, lecture.slug);
            args.putBoolean(BundleKeys.SIDEPANE, true);
            EventDetailFragment eventDetailFragment = new EventDetailFragment();
            eventDetailFragment.setArguments(args);
            replaceFragment(R.id.detail, eventDetailFragment,
                    EventDetailFragment.FRAGMENT_TAG, EventDetailFragment.FRAGMENT_TAG);
        } else {
            EventDetail.startForResult(this, lecture, mDay);
        }
    }

    @Override
    public void onSidePaneClose(@NonNull String fragmentTag) {
        View sidePane = findViewById(R.id.detail);
        if (sidePane != null) {
            sidePane.setVisibility(View.GONE);
        }
        removeFragment(fragmentTag);
    }

    public void reloadAlarms() {
        Fragment fragment = findFragment(FahrplanFragment.FRAGMENT_TAG);
        if (fragment != null) {
            ((FahrplanFragment) fragment).loadAlarms(this);
        }
    }

    @Override
    public void refreshEventMarkers() {
        Fragment fragment = findFragment(FahrplanFragment.FRAGMENT_TAG);
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
                if (resultCode == Activity.RESULT_OK) {
                    boolean defaultValue = getResources().getBoolean(R.bool.preferences_alternative_highlight_enabled_default_value);
                    if (intent.getBooleanExtra(BundleKeys.PREFS_ALTERNATIVE_HIGHLIGHT, defaultValue)) {
                        if (findViewById(R.id.schedule) != null) {
                            replaceFragment(R.id.schedule, new FahrplanFragment(),
                                    FahrplanFragment.FRAGMENT_TAG);
                        }
                    }
                }
        }
    }

    @Override
    public void onCertAccepted() {
        MyApp.LogDebug(LOG_TAG, "fetch on cert accepted.");
        fetchFahrplan(MainActivity.this);
    }

    @Override
    public void onLectureListClick(Lecture lecture) {
        if (lecture != null) {
            openLectureDetail(lecture, lecture.day);
        }
    }

    @Override
    public void onBackStackChanged() {
        FragmentManager manager = getSupportFragmentManager();
        int detailView = R.id.detail;
        toggleSidePaneVisibility(manager, detailView);
        supportInvalidateOptionsMenu();
    }

    private void toggleSidePaneVisibility(FragmentManager manager, @IdRes int detailView) {
        Fragment fragment = manager.findFragmentById(detailView);
        boolean found = fragment != null;
        View sidePane = findViewById(detailView);
        if (sidePane != null) {
            sidePane.setVisibility(found ? View.VISIBLE : View.GONE);
        }
    }

    public void refreshFavoriteList() {
        Fragment fragment = findFragment(StarredListFragment.FRAGMENT_TAG);
        if (fragment != null) {
            ((StarredListFragment) fragment).onRefresh(this);
        }
        ActivityCompat.invalidateOptionsMenu(this);
    }

    private void openFavorites() {
        FrameLayout sidePane = findViewById(R.id.detail);
        if (sidePane == null) {
            Intent intent = new Intent(this, StarredListActivity.class);
            startActivityForResult(intent, MyApp.STARRED);
        } else {
            sidePane.setVisibility(View.VISIBLE);
            replaceFragment(R.id.detail, StarredListFragment.newInstance(true),
                    StarredListFragment.FRAGMENT_TAG, StarredListFragment.FRAGMENT_TAG);
        }
    }

    public void openLectureChanges() {
        FrameLayout sidePane = findViewById(R.id.detail);
        if (sidePane == null) {
            Intent intent = new Intent(this, ChangeListActivity.class);
            startActivityForResult(intent, MyApp.CHANGELOG);
        } else {
            sidePane.setVisibility(View.VISIBLE);
            replaceFragment(R.id.detail, ChangeListFragment.newInstance(true),
                    ChangeListFragment.FRAGMENT_TAG, ChangeListFragment.FRAGMENT_TAG);
        }
    }

    @Override
    public void onAccepted(int dlgRequestCode) {
        switch (dlgRequestCode) {
            case StarredListFragment.DELETE_ALL_FAVORITES_REQUEST_CODE:
                Fragment fragment = findFragment(StarredListFragment.FRAGMENT_TAG);
                if (fragment != null) {
                    ((StarredListFragment) fragment).deleteAllFavorites();
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
