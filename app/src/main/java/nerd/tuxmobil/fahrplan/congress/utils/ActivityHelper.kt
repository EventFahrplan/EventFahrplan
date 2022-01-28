package nerd.tuxmobil.fahrplan.congress.utils

import android.app.Activity
import androidx.core.app.NavUtils
import androidx.core.app.TaskStackBuilder

object ActivityHelper {

    @JvmStatic
    fun Activity.navigateUp(): Boolean {
        val upIntent = NavUtils.getParentActivityIntent(this)!!
        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            // This activity is NOT part of this app's task, so create a new task
            // when navigating up, with a synthesized back stack.
            TaskStackBuilder.create(this)
                // Add all of this activity's parents to the back stack
                .addNextIntentWithParentStack(upIntent)
                // Navigate up to the closest parent
                .startActivities()
        } else {
            // This activity is part of this app's task, so simply
            // navigate up to the logical parent activity.
            NavUtils.navigateUpTo(this, upIntent)
        }
        return true
    }

}
