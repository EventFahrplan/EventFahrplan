package nerd.tuxmobil.fahrplan.congress.schedule;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.ligi.tracedroid.logging.Log;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.MyApp.TASKS;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.about.AboutDialog;
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmList;
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment;
import nerd.tuxmobil.fahrplan.congress.base.BaseActivity;
import nerd.tuxmobil.fahrplan.congress.changes.ChangeListActivity;
import nerd.tuxmobil.fahrplan.congress.changes.ChangeListFragment;
import nerd.tuxmobil.fahrplan.congress.changes.ChangeStatistic;
import nerd.tuxmobil.fahrplan.congress.changes.ChangesDialog;
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys;
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsActivity;
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsFragment;
import nerd.tuxmobil.fahrplan.congress.engagements.Engagements;
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListActivity;
import nerd.tuxmobil.fahrplan.congress.favorites.StarredListFragment;
import nerd.tuxmobil.fahrplan.congress.models.Meta;
import nerd.tuxmobil.fahrplan.congress.models.Session;
import nerd.tuxmobil.fahrplan.congress.net.CertificateErrorFragment;
import nerd.tuxmobil.fahrplan.congress.net.CustomHttpClient;
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult;
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus;
import nerd.tuxmobil.fahrplan.congress.net.LoadShiftsResult;
import nerd.tuxmobil.fahrplan.congress.net.ParseResult;
import nerd.tuxmobil.fahrplan.congress.net.ParseScheduleResult;
import nerd.tuxmobil.fahrplan.congress.net.ParseShiftsResult;
import nerd.tuxmobil.fahrplan.congress.reporting.TraceDroidEmailSender;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;
import nerd.tuxmobil.fahrplan.congress.settings.SettingsActivity;
import nerd.tuxmobil.fahrplan.congress.sidepane.OnSidePaneCloseListener;
import nerd.tuxmobil.fahrplan.congress.utils.ConfirmationDialog;
import nerd.tuxmobil.fahrplan.congress.utils.FahrplanMisc;
import okhttp3.OkHttpClient;

import static nerd.tuxmobil.fahrplan.congress.utils.LockScreenHelper.showWhenLockedCompat;

public class MainActivity extends BaseActivity implements
        OnSidePaneCloseListener,
        AbstractListFragment.OnSessionListClick,
        FragmentManager.OnBackStackChangedListener,
        ConfirmationDialog.OnConfirmationDialogClicked {

    private static final String LOG_TAG = "MainActivity";
    private static final int INVALID_NOTIFICATION_ID = -1;

    private ProgressDialog progress = null;

    private KeyguardManager keyguardManager = null;

    protected AppRepository appRepository;

    private ProgressBar progressBar = null;
    private boolean shouldScrollToCurrent = true;
    private boolean showUpdateAction = true;
    private boolean isScreenLocked = false;
    private boolean isFavoritesInSidePane = false;
    private static MainActivity instance;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        showWhenLockedCompat(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;

        MyApp.LogDebug(LOG_TAG, "onCreate");
        setContentView(R.layout.main_layout);
        appRepository = AppRepository.INSTANCE;
        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        Toolbar toolbar = requireViewByIdCompat(R.id.toolbar);
        progressBar = requireViewByIdCompat(R.id.progress);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.fahrplan);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        int actionBarColor = ContextCompat.getColor(this, R.color.colorActionBar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionBarColor));

        TraceDroidEmailSender.sendStackTraces(this);

        resetProgressDialog();

        MyApp.meta = appRepository.readMeta();
        FahrplanMisc.loadDays(appRepository);

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
                if (MyApp.meta.getNumDays() == 0 && savedInstanceState == null) {
                    Log.d(LOG_TAG, "Fetching schedule in onCreate bc. numDays==0");
                    fetchFahrplan();
                }
                break;
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (findViewById(R.id.schedule) != null && findFragment(FahrplanFragment.FRAGMENT_TAG) == null) {
            replaceFragment(R.id.schedule, new FahrplanFragment(),
                    FahrplanFragment.FRAGMENT_TAG);
        }

        if (findViewById(R.id.detail) == null) {
            removeFragment(SessionDetailsFragment.FRAGMENT_TAG);
        }

        Engagements.initUserEngagement(this);

        onSessionAlarmNotificationTapped(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        MyApp.LogDebug(LOG_TAG, "onNewIntent");
        setIntent(intent);
        onSessionAlarmNotificationTapped(intent);
    }

    private void onSessionAlarmNotificationTapped(@NonNull Intent intent) {
        int notificationId = intent.getIntExtra(BundleKeys.BUNDLE_KEY_SESSION_ALARM_NOTIFICATION_ID, INVALID_NOTIFICATION_ID);
        if (notificationId != INVALID_NOTIFICATION_ID) {
            appRepository.deleteSessionAlarmNotificationId(notificationId);
        }
    }

    public void onGotResponse(@NonNull FetchScheduleResult fetchScheduleResult) {
        HttpStatus status = fetchScheduleResult.getHttpStatus();
        MyApp.LogDebug(LOG_TAG, "Response... " + status);
        MyApp.task_running = TASKS.NONE;
        if (MyApp.meta.getNumDays() == 0) {
            hideProgressDialog();
        }
        if (status != HttpStatus.HTTP_OK) {
            showErrorDialog(fetchScheduleResult.getExceptionMessage(), fetchScheduleResult.getHostName(), status);
            progressBar.setVisibility(View.INVISIBLE);
            showUpdateAction = true;
            invalidateOptionsMenu();
            return;
        }
        MyApp.LogDebug(LOG_TAG, "yehhahh");
        progressBar.setVisibility(View.INVISIBLE);
        showUpdateAction = true;
        invalidateOptionsMenu();

        // Parser is automatically invoked when response has been received.
        showParsingStatus();
        MyApp.task_running = TASKS.PARSE;
    }

    private void showErrorDialog(@NonNull String exceptionMessage, @NonNull String hostName, HttpStatus status) {
        if (HttpStatus.HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE == status) {
            CertificateErrorFragment.showDialog(getSupportFragmentManager(), exceptionMessage);
        }
        CustomHttpClient.showHttpError(this, status, hostName);
    }

    public void onParseDone(@NonNull ParseResult result) {
        if (result instanceof ParseScheduleResult) {
            MyApp.LogDebug(LOG_TAG, "Parsing schedule done successfully: " + result.isSuccess() + " , numDays=" + MyApp.meta.getNumDays());
        }
        if (result instanceof ParseShiftsResult) {
            MyApp.LogDebug(LOG_TAG, "Parsing Engelsystem shifts done successfully: " + result.isSuccess());
        }
        MyApp.task_running = TASKS.NONE;

        if (MyApp.meta.getNumDays() == 0) {
            hideProgressDialog();
        }
        progressBar.setVisibility(View.INVISIBLE);
        showUpdateAction = true;
        invalidateOptionsMenu();
        Fragment fragment = findFragment(FahrplanFragment.FRAGMENT_TAG);
        if (fragment != null) {
            ((FahrplanFragment) fragment).onParseDone(result);
        }
        fragment = findFragment(ChangeListFragment.FRAGMENT_TAG);
        if (fragment instanceof ChangeListFragment) {
            ((ChangeListFragment) fragment).onRefresh();
        }

        if (!appRepository.readScheduleChangesSeen()) {
            showChangesDialog();
        }
    }

    private void onLoadShiftsDone(@NonNull LoadShiftsResult result) {
        Fragment fragment = findFragment(FahrplanFragment.FRAGMENT_TAG);
        if (fragment != null) {
            ((FahrplanFragment) fragment).onParseDone(ParseShiftsResult.of(result));
        }
        fragment = findFragment(ChangeListFragment.FRAGMENT_TAG);
        if (fragment instanceof ChangeListFragment) {
            ((ChangeListFragment) fragment).onRefresh();
        }
    }

    public void showFetchingStatus() {
        if (MyApp.meta.getNumDays() == 0) {
            // initial load
            MyApp.LogDebug(LOG_TAG, "fetchFahrplan with numDays == 0");
            showProgressDialog(R.string.progress_loading_data);
        } else {
            MyApp.LogDebug(LOG_TAG, "show fetch status");
            progressBar.setVisibility(View.VISIBLE);
            showUpdateAction = false;
            invalidateOptionsMenu();
        }
    }

    public void showParsingStatus() {
        if (MyApp.meta.getNumDays() == 0) {
            // initial load
            showProgressDialog(R.string.progress_processing_data);
        } else {
            MyApp.LogDebug(LOG_TAG, "show parse status");
            progressBar.setVisibility(View.VISIBLE);
            showUpdateAction = false;
            invalidateOptionsMenu();
        }
    }

    public void fetchFahrplan() {
        if (MyApp.task_running == TASKS.NONE) {
            MyApp.task_running = TASKS.FETCH;
            showFetchingStatus();
            String url = appRepository.readScheduleUrl();
            OkHttpClient okHttpClient = CustomHttpClient.createHttpClient();
            appRepository.loadSchedule(url,
                    okHttpClient,
                    fetchScheduleResult -> {
                        onGotResponse(fetchScheduleResult);
                        return Unit.INSTANCE;
                    },
                    parseScheduleResult -> {
                        onParseDone(parseScheduleResult);
                        return Unit.INSTANCE;
                    },
                    loadShiftsResult -> {
                        onLoadShiftsDone(loadShiftsResult);
                        return Unit.INSTANCE;
                    });
        } else {
            Log.d(LOG_TAG, "Fetching schedule already in progress.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        appRepository.cancelLoading();
        hideProgressDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isScreenLocked = keyguardManager.isKeyguardLocked();

        FrameLayout sidePane = findViewById(R.id.detail);
        if (sidePane != null && isFavoritesInSidePane) {
            sidePane.setVisibility(isScreenLocked ? View.GONE : View.VISIBLE);
        }

        if (!appRepository.readScheduleChangesSeen()) {
            showChangesDialog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.mainmenu, menu);
        MenuItem item = menu.findItem(R.id.menu_item_refresh);
        if (item != null) {
            item.setVisible(showUpdateAction);
        }
        return true;
    }

    void showChangesDialog() {
        Fragment fragment = findFragment(ChangesDialog.FRAGMENT_TAG);
        if (fragment == null) {
            List<Session> sessions = appRepository.loadChangedSessions();
            Meta meta = appRepository.readMeta();
            String scheduleVersion = meta.getVersion();
            ChangeStatistic statistic = ChangeStatistic.of(sessions);
            DialogFragment changesDialog = ChangesDialog.newInstance(scheduleVersion, statistic);
            changesDialog.show(getSupportFragmentManager(), ChangesDialog.FRAGMENT_TAG);
        }
    }

    void showAboutDialog() {
        Meta meta = appRepository.readMeta();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        AboutDialog.newInstance(
                meta.getVersion(),
                meta.getSubtitle(),
                meta.getTitle()
        ).show(ft, "about");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_item_refresh:
                Log.d(LOG_TAG, "Menu item: Refresh");
                fetchFahrplan();
                return true;
            case R.id.menu_item_about:
                showAboutDialog();
                return true;
            case R.id.menu_item_alarms:
                intent = new Intent(this, AlarmList.class);
                startActivityForResult(intent, MyApp.ALARMLIST);
                return true;
            case R.id.menu_item_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, MyApp.SETTINGS);
                return true;
            case R.id.menu_item_schedule_changes:
                openSessionChanges();
                return true;
            case R.id.menu_item_favorites:
                openFavorites();
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    public void openSessionDetails(@NonNull Session session) {
        FrameLayout sidePane = findViewById(R.id.detail);
        MyApp.LogDebug(LOG_TAG, "openSessionDetails sidePane=" + sidePane);
        if (sidePane != null) {
            FragmentManager fm = getSupportFragmentManager();
            fm.popBackStack(SessionDetailsFragment.FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            Bundle args = new Bundle();
            args.putString(BundleKeys.SESSION_ID, session.sessionId);
            args.putBoolean(BundleKeys.SIDEPANE, true);
            SessionDetailsFragment fragment = new SessionDetailsFragment();
            fragment.setArguments(args);
            replaceFragment(R.id.detail, fragment,
                    SessionDetailsFragment.FRAGMENT_TAG, SessionDetailsFragment.FRAGMENT_TAG);
        } else {
            SessionDetailsActivity.startForResult(this, session);
        }
    }

    @Override
    public void onSidePaneClose(@NonNull String fragmentTag) {
        View sidePane = findViewById(R.id.detail);
        if (sidePane != null) {
            sidePane.setVisibility(View.GONE);
        }
        if (fragmentTag.equals(StarredListFragment.FRAGMENT_TAG)) {
            isFavoritesInSidePane = false;
        }
        removeFragment(fragmentTag);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case MyApp.ALARMLIST:
            case MyApp.SESSION_VIEW:
                if (resultCode == Activity.RESULT_CANCELED) {
                    shouldScrollToCurrent = false;
                }
                break;
            case MyApp.SETTINGS:
                if (resultCode == Activity.RESULT_OK && intent != null) {
                    boolean isAlternativeHighlightingUpdated = intent.getBooleanExtra(BundleKeys.BUNDLE_KEY_ALTERNATIVE_HIGHLIGHTING_UPDATED, false);
                    boolean isUseDeviceTimeZoneUpdated = intent.getBooleanExtra(BundleKeys.BUNDLE_KEY_USE_DEVICE_TIME_ZONE_UPDATED, false);
                    if (isAlternativeHighlightingUpdated || isUseDeviceTimeZoneUpdated) {
                        if (findViewById(R.id.schedule) != null && findFragment(FahrplanFragment.FRAGMENT_TAG) == null) {
                            replaceFragment(R.id.schedule, new FahrplanFragment(),
                                    FahrplanFragment.FRAGMENT_TAG);
                        }
                    }
                    boolean shouldFetchFahrplan = false;
                    boolean isScheduleUrlUpdated = getResources().getBoolean(R.bool.bundle_key_schedule_url_updated_default_value);
                    if (intent.getBooleanExtra(BundleKeys.BUNDLE_KEY_SCHEDULE_URL_UPDATED, isScheduleUrlUpdated)) {
                        shouldFetchFahrplan = true;
                    }
                    boolean isEngelsystemShiftsUrlUpdated = getResources().getBoolean(R.bool.bundle_key_engelsystem_shifts_url_updated_default_value);
                    if (intent.getBooleanExtra(BundleKeys.BUNDLE_KEY_ENGELSYSTEM_SHIFTS_URL_UPDATED, isEngelsystemShiftsUrlUpdated)) {
                        shouldFetchFahrplan = true;
                    }
                    if (shouldFetchFahrplan) {
                        fetchFahrplan();
                    }
                }
        }
    }

    @Override
    public void onSessionListClick(Session session) {
        if (session != null) {
            openSessionDetails(session);
        }
    }

    @Override
    public void onBackStackChanged() {
        FragmentManager manager = getSupportFragmentManager();
        int detailView = R.id.detail;
        toggleSidePaneVisibility(manager, detailView);
        invalidateOptionsMenu();
    }

    private void showProgressDialog(@StringRes int message) {
        progress = ProgressDialog.show(this, "", getResources().getString(message), true);
    }

    private void hideProgressDialog() {
        if (progress != null) {
            progress.dismiss();
            resetProgressDialog();
        }
    }

    private void resetProgressDialog() {
        progress = null;
    }

    private void toggleSidePaneVisibility(FragmentManager manager, @IdRes int detailView) {
        Fragment fragment = manager.findFragmentById(detailView);
        boolean found = fragment != null;
        View sidePane = findViewById(detailView);
        if (sidePane != null) {
            isFavoritesInSidePane = found && fragment instanceof StarredListFragment;
            sidePane.setVisibility(isFavoritesInSidePane && isScreenLocked || !found
                    ? View.GONE : View.VISIBLE);
        }
    }

    public void refreshFavoriteList() {
        Fragment fragment = findFragment(StarredListFragment.FRAGMENT_TAG);
        if (fragment != null) {
            ((StarredListFragment) fragment).onRefresh();
        }
        invalidateOptionsMenu();
    }

    private void openFavorites() {
        FrameLayout sidePane = findViewById(R.id.detail);
        if (sidePane == null) {
            Intent intent = new Intent(this, StarredListActivity.class);
            startActivity(intent);
        } else if (!isScreenLocked) {
            sidePane.setVisibility(View.VISIBLE);
            isFavoritesInSidePane = true;
            replaceFragment(R.id.detail, StarredListFragment.newInstance(true),
                    StarredListFragment.FRAGMENT_TAG, StarredListFragment.FRAGMENT_TAG);
        }
    }

    public void openSessionChanges() {
        FrameLayout sidePane = findViewById(R.id.detail);
        if (sidePane == null) {
            Intent intent = new Intent(this, ChangeListActivity.class);
            startActivity(intent);
        } else {
            sidePane.setVisibility(View.VISIBLE);
            replaceFragment(R.id.detail, ChangeListFragment.newInstance(true),
                    ChangeListFragment.FRAGMENT_TAG, ChangeListFragment.FRAGMENT_TAG);
        }
    }

    @Override
    public void onAccepted(int dlgRequestCode) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (dlgRequestCode) {
            case StarredListFragment.DELETE_ALL_FAVORITES_REQUEST_CODE:
                Fragment fragment = findFragment(StarredListFragment.FRAGMENT_TAG);
                if (fragment != null) {
                    ((StarredListFragment) fragment).deleteAllFavorites();
                }
                break;
        }
    }

    public void shouldScheduleScrollToCurrentTimeSlot(Function0<Unit> scrollToInstructions) {
        if (shouldScrollToCurrent) {
            scrollToInstructions.invoke();
        }
        shouldScrollToCurrent = true;
    }

    public static MainActivity getInstance() {
        return instance;
    }

    /**
     * Returns a unique intent to be used to launch this activity.
     * The given parameters {@code sessionId} and {@code dayIndex} are passed along as bundle extras.
     * The {@code sessionId} is also used to ensure this intent is unique by definition of
     * {@link Intent#filterEquals(Intent)}.
     */
    public static Intent createLaunchIntent(
            @NonNull Context context,
            @NonNull String sessionId,
            int dayIndex,
            int notificationId
    ) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setData(Uri.parse("fake://" + sessionId));
        intent.putExtra(BundleKeys.BUNDLE_KEY_SESSION_ALARM_SESSION_ID, sessionId);
        intent.putExtra(BundleKeys.BUNDLE_KEY_SESSION_ALARM_DAY_INDEX, dayIndex);
        intent.putExtra(BundleKeys.BUNDLE_KEY_SESSION_ALARM_NOTIFICATION_ID, notificationId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        return intent;
    }

}
