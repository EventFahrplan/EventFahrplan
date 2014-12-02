package nerd.tuxmobil.fahrplan.congress;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

import nerd.tuxmobil.fahrplan.congress.R;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnLectureListClick}
 * interface.
 */
public class ChangeListFragment extends SherlockListFragment {

    private static final String LOG_TAG = "ChangeListFragment";
    private OnLectureListClick mListener;
    private LectureList changesList;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private LectureArrayAdapter mAdapter;

    public static ChangeListFragment newInstance() {
        ChangeListFragment fragment = new ChangeListFragment();
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

        changesList = FahrplanMisc.readChanges(getSherlockActivity());

        mAdapter = new LectureArrayAdapter(getActivity(), changesList);
        MyApp.LogDebug(LOG_TAG, "onCreate, " + changesList.size() + " changes");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lecture_list, container, false);

        if (getSherlockActivity() instanceof MainActivity) {
            TextView title = (TextView) view.findViewById(R.id.title);
            title.setVisibility(View.VISIBLE);
            title.setText(R.string.schedule_changes);
        }

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
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
        LectureList updatedChanges = FahrplanMisc.readChanges(getSherlockActivity());
        changesList.clear();
        changesList.addAll(updatedChanges);
        mAdapter.notifyDataSetChanged();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnLectureListClick {
        public void onLectureListClick(Lecture lecture);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        MyApp.LogDebug(LOG_TAG, "onItemClick");
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            Lecture clicked = changesList.get(position);
            if (clicked.changed_isCanceled) return;
            mListener.onLectureListClick(clicked);
        }
    }
}
