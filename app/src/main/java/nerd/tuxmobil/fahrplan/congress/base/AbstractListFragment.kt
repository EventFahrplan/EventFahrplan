package nerd.tuxmobil.fahrplan.congress.base

import android.content.Context
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment

abstract class AbstractListFragment : Fragment() {

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

    @MainThread
    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

}
