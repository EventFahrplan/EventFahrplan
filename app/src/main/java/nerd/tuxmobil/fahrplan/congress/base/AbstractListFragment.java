package nerd.tuxmobil.fahrplan.congress.base;

import android.support.v4.app.ListFragment;

import nerd.tuxmobil.fahrplan.congress.models.Lecture;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnLectureListClick}
 * interface.
 */
public abstract class AbstractListFragment extends ListFragment {

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     * @param lecture                The lecture which was clicked.
     * @param requiresScheduleReload Boolean flag to indicate whether the schedule
     *                               must be reload from the data source or not.
     */
    public interface OnLectureListClick {
        void onLectureListClick(Lecture lecture, boolean requiresScheduleReload);
    }

}
