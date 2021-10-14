package nerd.tuxmobil.fahrplan.congress.base;

import android.content.Context;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.fragment.app.ListFragment;

import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnSessionListClick}
 * interface.
 */
public abstract class AbstractListFragment extends ListFragment {

    public interface OnSessionListClick {

        /**
         * This interface must be implemented by activities that contain this
         * fragment to allow an interaction in this fragment to be communicated
         * to the activity and potentially other fragments contained in that
         * activity.
         *
         * @param sessionId The ID of the session which was clicked.
         */
        void onSessionListClick(@NonNull String sessionId);
    }

    protected AppRepository appRepository;

    @MainThread
    @CallSuper
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appRepository = AppRepository.INSTANCE;
    }

}
