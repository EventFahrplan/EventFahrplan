package nerd.tuxmobil.fahrplan.congress.changes;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment;
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;
import nerd.tuxmobil.fahrplan.congress.models.Meta;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link AbstractListFragment.OnLectureListClick}
 * interface.
 */
public class ChangeListFragment extends AbstractListFragment {

    private static final String LOG_TAG = "ChangeListFragment";
    public static final String FRAGMENT_TAG = "changes";
    private OnLectureListClick mListener;
    private List<Lecture> changesList;
    private boolean sidePane = false;
    private boolean requiresScheduleReload = false;

    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private LectureChangesArrayAdapter mAdapter;

    public static ChangeListFragment newInstance(boolean sidePane, boolean requiresScheduleReload) {
        ChangeListFragment fragment = new ChangeListFragment();
        Bundle args = new Bundle();
        args.putBoolean(BundleKeys.SIDEPANE, sidePane);
        args.putBoolean(BundleKeys.REQUIRES_SCHEDULE_RELOAD, requiresScheduleReload);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChangeListFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            sidePane = args.getBoolean(BundleKeys.SIDEPANE);
            requiresScheduleReload = args.getBoolean(BundleKeys.REQUIRES_SCHEDULE_RELOAD);
        }

        Context context = requireContext();
        changesList = appRepository.loadChangedLectures();
        Meta meta = appRepository.readMeta();
        mAdapter = new LectureChangesArrayAdapter(context, changesList, meta.getNumDays());
        MyApp.LogDebug(LOG_TAG, "onCreate, " + changesList.size() + " changes");
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
            view = localInflater.inflate(R.layout.fragment_lecture_list_narrow, container, false);
            mListView = view.findViewById(android.R.id.list);
            header = localInflater.inflate(R.layout.changes_header, null, false);
        } else {
            view = localInflater.inflate(R.layout.fragment_lecture_list, container, false);
            mListView = view.findViewById(android.R.id.list);
            header = localInflater.inflate(R.layout.header_empty, null, false);
        }
        mListView.addHeaderView(header, null, false);
        mListView.setHeaderDividersEnabled(false);

        // Set the adapter
        mListView.setAdapter(mAdapter);

        return view;
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
        List<Lecture> updatedChanges = appRepository.loadChangedLectures();
        if (changesList != null) {
            changesList.clear();
            changesList.addAll(updatedChanges);
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
            Lecture clicked = changesList.get(mAdapter.getItemIndex(position));
            if (clicked.changedIsCanceled) return;
            mListener.onLectureListClick(clicked, requiresScheduleReload);
        }
    }
}
