package nerd.tuxmobil.fahrplan.congress.base

import android.content.Context
import androidx.fragment.app.Fragment

/**
 * Callback to be implemented by activities hosting fragments which show sessions.
 * Lets the fragment communicate that a session was clicked so that the activity
 * can navigate to its details.
 */
fun interface OnSessionItemClickListener {

    /**
     * Called when the session with the given [sessionId] has been clicked.
     */
    fun onSessionItemClick(sessionId: String)

}

/**
 * Fails fast if the given [context] does not implement [OnSessionItemClickListener].
 * To be invoked from [Fragment.onAttach].
 */
fun requireOnSessionItemClickListener(context: Context) {
    check(context is OnSessionItemClickListener) { "$context must implement OnSessionItemClickListener" }
}

/**
 * Forwards the click on the session with the given [sessionId] to the hosting activity
 * which must implement [OnSessionItemClickListener].
 * Invoke [requireOnSessionItemClickListener] from [Fragment.onAttach] to fail fast otherwise.
 */
fun Fragment.forwardSessionItemClick(sessionId: String) {
    (requireActivity() as OnSessionItemClickListener).onSessionItemClick(sessionId)
}
