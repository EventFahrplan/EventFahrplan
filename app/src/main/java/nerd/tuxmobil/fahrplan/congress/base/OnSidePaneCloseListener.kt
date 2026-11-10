package nerd.tuxmobil.fahrplan.congress.base

import androidx.fragment.app.Fragment
import nerd.tuxmobil.fahrplan.congress.utils.ActivityHelper.navigateUp

/**
 * Callback to be implemented by activities which show fragments in a side pane.
 */
fun interface OnSidePaneCloseListener {

    fun onSidePaneClose(fragmentTag: String)

}

/**
 * Closes the side pane hosting this fragment if the activity implements [OnSidePaneCloseListener].
 * Otherwise, navigates up from the activity.
 */
fun Fragment.closeSidePaneOrNavigateUp(fragmentTag: String) {
    val activity = requireActivity()
    when (val listener = activity as? OnSidePaneCloseListener) {
        null -> activity.navigateUp()
        else -> listener.onSidePaneClose(fragmentTag)
    }
}
