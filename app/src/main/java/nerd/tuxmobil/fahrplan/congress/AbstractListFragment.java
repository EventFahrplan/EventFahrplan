package nerd.tuxmobil.fahrplan.congress;

import android.support.v4.app.ListFragment;


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
     */
    public interface OnLectureListClick {
        public void onLectureListClick(Lecture lecture);
    }

}
