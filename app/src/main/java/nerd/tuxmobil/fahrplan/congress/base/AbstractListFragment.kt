package nerd.tuxmobil.fahrplan.congress.base

import android.content.Context
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.fragment.app.ListFragment
import nerd.tuxmobil.fahrplan.congress.base.AbstractListFragment.OnSessionListClick
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository

/**
 * A fragment representing a list of Items.
 *
 *
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 *
 *
 * Activities containing this fragment MUST implement the [OnSessionListClick]
 * interface.
 */
abstract class AbstractListFragment : ListFragment() {

    fun interface OnSessionListClick {
        /**
         * This interface must be implemented by activities that contain this
         * fragment to allow an interaction in this fragment to be communicated
         * to the activity and potentially other fragments contained in that
         * activity.
         *
         * @param sessionId The ID of the session which was clicked.
         */
        fun onSessionListClick(sessionId: String)
    }

    protected lateinit var appRepository: AppRepository

    @MainThread
    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
        appRepository = AppRepository
    }

}
