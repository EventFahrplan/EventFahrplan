package nerd.tuxmobil.fahrplan.congress;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnLectureListClick}
 * interface.
 */
public class ChangeListFragment extends AbstractListFragment {

    private static final String LOG_TAG = "ChangeListFragment";
    private OnLectureListClick mListener;
    private LectureList changesList;
    private boolean sidePane = false;

    /**
     * The fragment's ListView/GridView.
     */
    private ListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private LectureChangesArrayAdapter mAdapter;

    public static ChangeListFragment newInstance(boolean sidePane) {
        ChangeListFragment fragment = new ChangeListFragment();
        Bundle args = new Bundle();
        args.putBoolean(BundleKeys.SIDEPANE, sidePane);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            sidePane = args.getBoolean(BundleKeys.SIDEPANE);
        }

        changesList = FahrplanMisc.readChanges(getActivity());
        if (changesList == null) changesList = new LectureList();

        mAdapter = new LectureChangesArrayAdapter(getActivity(), changesList);
        MyApp.LogDebug(LOG_TAG, "onCreate, " + changesList.size() + " changes");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(),
                R.style.Theme_AppCompat_Light);

        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        View view, header;
        if (sidePane) {
            view = localInflater.inflate(R.layout.fragment_lecture_list_narrow, container, false);
            mListView = (ListView) view.findViewById(android.R.id.list);
            header = localInflater.inflate(R.layout.changes_header, null, false);
        } else {
            view = localInflater.inflate(R.layout.fragment_lecture_list, container, false);
            mListView = (ListView) view.findViewById(android.R.id.list);
            header = localInflater.inflate(R.layout.header_empty, null, false);
        }
        mListView.addHeaderView(header, null, false);
        mListView.setHeaderDividersEnabled(false);

        // Set the adapter
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnLectureListClick) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLectureListClick");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onRefresh() {
        LectureList updatedChanges = FahrplanMisc.readChanges(getActivity());
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
            Lecture clicked = changesList.get(mAdapter.getMapper().get(position));
            if (clicked.changedIsCanceled) return;
            mListener.onLectureListClick(clicked);
        }
    }
}
