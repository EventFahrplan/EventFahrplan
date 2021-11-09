package nerd.tuxmobil.fahrplan.congress.extensions

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit

/**
 * Replaces the [containerViewId] with the given [fragment] and adds
 * it to the [backstack][FragmentTransaction.addToBackStack].
 */
fun FragmentManager.replaceFragment(
    @IdRes containerViewId: Int,
    fragment: Fragment,
    fragmentTag: String,
    backStackStateName: String
) {
    commit {
        replace(containerViewId, fragment, fragmentTag)
        addToBackStack(backStackStateName)
    }
}

/**
 * Replaces the [containerViewId] with the given [fragment].
 */
fun FragmentManager.replaceFragment(
    @IdRes containerViewId: Int,
    fragment: Fragment,
    fragmentTag: String
) {
    commit {
        replace(containerViewId, fragment, fragmentTag)
    }
}
