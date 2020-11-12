package nerd.tuxmobil.fahrplan.congress.favorites;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.threeten.bp.ZoneId;

import java.util.List;

import info.metadude.android.eventfahrplan.commons.temporal.Moment;
import nerd.tuxmobil.fahrplan.congress.BuildConfig;
import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment;
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys;
import nerd.tuxmobil.fahrplan.congress.models.Meta;
import nerd.tuxmobil.fahrplan.congress.models.Session;
import nerd.tuxmobil.fahrplan.congress.sharing.JsonSessionFormat;
import nerd.tuxmobil.fahrplan.congress.sharing.SessionSharer;
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleSessionFormat;
import nerd.tuxmobil.fahrplan.congress.utils.ActivityHelper;
import nerd.tuxmobil.fahrplan.congress.utils.ConfirmationDialog;

import static info.metadude.android.eventfahrplan.commons.temporal.Moment.MILLISECONDS_OF_ONE_MINUTE;
import static nerd.tuxmobil.fahrplan.congress.extensions.ViewExtensions.requireViewByIdCompat;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnSessionListClick}
 * interface.
 */
public class StarredListFragment extends AbstractListFragment implements AbsListView
        .MultiChoiceModeListener {

    private static final String LOG_TAG = "StarredListFragment";
    public static final String FRAGMENT_TAG = "starred";
    private OnSessionListClick mListener;
    private List<Session> starredList;
    private boolean sidePane = false;

    public static final int DELETE_ALL_FAVORITES_REQUEST_CODE = 19126;

    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private StarredListAdapter mAdapter;

    public static StarredListFragment newInstance(boolean sidePane) {
        StarredListFragment fragment = new StarredListFragment();
        Bundle args = new Bundle();
        args.putBoolean(BundleKeys.SIDEPANE, sidePane);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StarredListFragment() {
    }

    @MainThread
    @CallSuper
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            sidePane = args.getBoolean(BundleKeys.SIDEPANE);
        }
        setHasOptionsMenu(true);
    }

    @Override
    @Nullable
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        final Context contextThemeWrapper = new ContextThemeWrapper(requireContext(),
                R.style.Theme_AppCompat_Light);

        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        View view;
        View header;
        if (sidePane) {
            view = localInflater.inflate(R.layout.fragment_favorites_list_narrow, container, false);
            mListView = requireViewByIdCompat(view, android.R.id.list);
            header = localInflater.inflate(R.layout.starred_header, null, false);
        } else {
            view = localInflater.inflate(R.layout.fragment_favorites_list, container, false);
            mListView = requireViewByIdCompat(view, android.R.id.list);
            header = localInflater.inflate(R.layout.header_empty, null, false);
        }
        mListView.addHeaderView(header, null, false);
        mListView.setHeaderDividersEnabled(false);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(this);
        return view;
    }

    @MainThread
    @CallSuper
    @Override
    public void onResume() {
        super.onResume();
        initStarredList();
        jumpOverPastSessions();
    }

    private void initStarredList() {
        Context context = requireContext();
        starredList = appRepository.loadStarredSessions();
        Meta meta = appRepository.readMeta();
        mAdapter = new StarredListAdapter(context, starredList, meta.getNumDays());
        MyApp.LogDebug(LOG_TAG, "initStarredList: " + starredList.size() + " favorites");
        mListView.setAdapter(mAdapter);
    }

    private void jumpOverPastSessions() {
        if (starredList == null) return;
        long nowMillis = Moment.now().toMilliseconds();

        int i;
        int numSeparators = 0;
        for (i = 0; i < starredList.size(); i++) {
            Session session = starredList.get(i);
            if (session.dateUTC + session.duration * MILLISECONDS_OF_ONE_MINUTE > nowMillis) {
                numSeparators = session.day;
                break;
            }
        }
        if (i > 0 && i < starredList.size()) {
            mListView.setSelection(i + 1 + numSeparators);
        }
    }

    @MainThread
    @CallSuper
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (OnSessionListClick) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnSessionListClick");
        }
    }

    @MainThread
    @CallSuper
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onRefresh() {
        List<Session> starred = appRepository.loadStarredSessions();
        if (starredList != null) {
            starredList.clear();
            starredList.addAll(starred);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(@NonNull ListView listView, @NonNull View itemView, int position, long id) {
        MyApp.LogDebug(LOG_TAG, "onItemClick");
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            position--;
            Session clicked = starredList.get(mAdapter.getItemIndex(position));
            mListener.onSessionListClick(clicked);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.starred_list_menu, menu);
        MenuItem item = menu.findItem(R.id.menu_item_delete_all_favorites);
        if (item != null && (starredList == null || starredList.isEmpty())) {
            item.setVisible(false);
        }
        if (BuildConfig.ENABLE_CHAOSFLIX_EXPORT) {
            item = menu.findItem(R.id.menu_item_share_favorites_menu);
        } else {
            item = menu.findItem(R.id.menu_item_share_favorites);
        }
        if (item != null) {
            item.setVisible(starredList != null && !starredList.isEmpty());
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share_favorites:
            case R.id.menu_item_share_favorites_text:
                shareSessions();
                return true;
            case R.id.menu_item_share_favorites_json:
                shareSessionsToChaosflix();
                return true;
            case R.id.menu_item_delete_all_favorites:
                askToDeleteAllFavorites();
                return true;
            case android.R.id.home:
                return ActivityHelper.navigateUp(requireActivity());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.starred_list_context_menu, menu);
        mode.setTitle(getString(R.string.choose_to_delete));
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.menu_item_delete_favorite:
                deleteItems(mListView.getCheckedItemPositions());
                Activity activity = requireActivity();
                activity.invalidateOptionsMenu();
                refreshViews(activity);
                mode.finish();
                return true;
            default:
                return false;
        }
    }

    private void deleteItem(int index) {
        Session starredSession = starredList.get(index);
        starredSession.highlight = false;
        appRepository.updateHighlight(starredSession);
        appRepository.notifyHighlightsChanged();
        starredList.remove(index);
    }

    private void deleteItems(SparseBooleanArray checkedItemPositions) {
        for (int id = mListView.getAdapter().getCount() - 1; id >= 0; id--) {
            if (checkedItemPositions.get(id)) {
                deleteItem(mAdapter.getItemIndex(id - 1));
            }
        }
    }

    private void refreshViews(@NonNull Activity activity) {
        mAdapter.notifyDataSetChanged();
        activity.setResult(Activity.RESULT_OK);
        activity.invalidateOptionsMenu();
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    private void askToDeleteAllFavorites() {
        FragmentManager fm = requireActivity().getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(ConfirmationDialog.FRAGMENT_TAG);
        if (fragment == null) {
            ConfirmationDialog confirm = ConfirmationDialog.newInstance(
                    R.string.dlg_delete_all_favorites,
                    DELETE_ALL_FAVORITES_REQUEST_CODE);
            confirm.show(fm, ConfirmationDialog.FRAGMENT_TAG);
        }
    }

    public void deleteAllFavorites() {
        MyApp.LogDebug(LOG_TAG, "deleteAllFavorites");
        if (starredList == null || starredList.isEmpty()) {
            return;
        }
        appRepository.deleteAllHighlights();
        appRepository.notifyHighlightsChanged();
        for (Session starredSession : starredList) {
            starredSession.highlight = false;
        }
        starredList.clear();
        Activity activity = requireActivity();
        activity.invalidateOptionsMenu();
        refreshViews(activity);
    }

    private void shareSessions() {
        ZoneId timeZoneId = appRepository.readMeta().getTimeZoneId();
        String formattedSession = SimpleSessionFormat.format(starredList, timeZoneId);
        if (formattedSession != null) {
            Context context = requireContext();
            SessionSharer.shareSimple(context, formattedSession);
        }
    }

    private void shareSessionsToChaosflix() {
        String formattedSession = JsonSessionFormat.format(starredList);
        if (formattedSession != null) {
            Context context = requireContext();
            if (!SessionSharer.shareJson(context, formattedSession)) {
                Toast.makeText(context, R.string.share_error_activity_not_found, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
