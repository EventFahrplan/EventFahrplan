package nerd.tuxmobil.fahrplan.congress.favorites;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

import java.util.List;

import info.metadude.android.eventfahrplan.commons.temporal.Moment;
import nerd.tuxmobil.fahrplan.congress.BuildConfig;
import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment;
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;
import nerd.tuxmobil.fahrplan.congress.models.Meta;
import nerd.tuxmobil.fahrplan.congress.sharing.JsonLectureFormat;
import nerd.tuxmobil.fahrplan.congress.sharing.LectureSharer;
import nerd.tuxmobil.fahrplan.congress.sharing.SimpleLectureFormat;
import nerd.tuxmobil.fahrplan.congress.utils.ActivityHelper;
import nerd.tuxmobil.fahrplan.congress.utils.ConfirmationDialog;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link AbstractListFragment.OnLectureListClick}
 * interface.
 */
public class StarredListFragment extends AbstractListFragment implements AbsListView
        .MultiChoiceModeListener {

    private static final String LOG_TAG = "StarredListFragment";
    public static final String FRAGMENT_TAG = "starred";
    private OnLectureListClick mListener;
    private List<Lecture> starredList;
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
    private LectureArrayAdapter mAdapter;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final Context contextThemeWrapper = new ContextThemeWrapper(requireContext(),
                R.style.Theme_AppCompat_Light);

        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        View view;
        View header;
        if (sidePane) {
            view = localInflater.inflate(R.layout.fragment_favorites_list_narrow, container, false);
            mListView = view.findViewById(android.R.id.list);
            header = localInflater.inflate(R.layout.starred_header, null, false);
        } else {
            view = localInflater.inflate(R.layout.fragment_favorites_list, container, false);
            mListView = view.findViewById(android.R.id.list);
            header = localInflater.inflate(R.layout.header_empty, null, false);
        }
        mListView.addHeaderView(header, null, false);
        mListView.setHeaderDividersEnabled(false);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListView.setMultiChoiceModeListener(this);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initStarredList();
        jumpOverPastLectures();
    }

    private void initStarredList() {
        Context context = requireContext();
        starredList = appRepository.loadStarredLectures();
        Meta meta = appRepository.readMeta();
        mAdapter = new LectureArrayAdapter(context, starredList, meta.getNumDays());
        MyApp.LogDebug(LOG_TAG, "initStarredList: " + starredList.size() + " favorites");
        mListView.setAdapter(mAdapter);
    }

    private void jumpOverPastLectures() {
        if (starredList == null) return;
        long nowMillis = new Moment().toMilliseconds();

        int i;
        int numSeparators = 0;
        for (i = 0; i < starredList.size(); i++) {
            Lecture lecture = starredList.get(i);
            if (lecture.dateUTC + lecture.duration * 60000 > nowMillis) {
                numSeparators = lecture.day;
                break;
            }
        }
        if (i > 0 && i < starredList.size()) {
            mListView.setSelection(i + 1 + numSeparators);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnLectureListClick) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnLectureListClick");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onRefresh() {
        List<Lecture> starred = appRepository.loadStarredLectures();
        if (starredList != null) {
            starredList.clear();
            starredList.addAll(starred);
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        MyApp.LogDebug(LOG_TAG, "onItemClick");
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            position--;
            Lecture clicked = starredList.get(mAdapter.getItemIndex(position));
            mListener.onLectureListClick(clicked, false);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share_favorites:
            case R.id.menu_item_share_favorites_text:
                shareLectures();
                return true;
            case R.id.menu_item_share_favorites_json:
                shareLecturesToChaosflix();
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
        Lecture starredLecture = starredList.get(index);
        starredLecture.highlight = false;
        appRepository.updateHighlight(starredLecture);
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
        for (Lecture starredLecture : starredList) {
            starredLecture.highlight = false;
        }
        starredList.clear();
        Activity activity = requireActivity();
        activity.invalidateOptionsMenu();
        refreshViews(activity);
    }

    private void shareLectures() {
        String formattedLectures = SimpleLectureFormat.format(starredList);
        if (formattedLectures != null) {
            Context context = requireContext();
            if (!LectureSharer.shareSimple(context, formattedLectures)) {
                Toast.makeText(context, R.string.share_error_activity_not_found, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void shareLecturesToChaosflix() {
        String formattedLectures = JsonLectureFormat.format(starredList);
        if (formattedLectures != null) {
            Context context = requireContext();
            if (!LectureSharer.shareJson(context, formattedLectures)) {
                Toast.makeText(context, R.string.share_error_activity_not_found, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
