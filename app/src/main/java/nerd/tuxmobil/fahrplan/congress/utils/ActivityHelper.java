package nerd.tuxmobil.fahrplan.congress.utils;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;

public class ActivityHelper {

    public static boolean navigateUp(@NonNull final Activity activity) {
        Intent upIntent = NavUtils.getParentActivityIntent(activity);
        if (NavUtils.shouldUpRecreateTask(activity, upIntent)) {
            // This activity is NOT part of this app's task, so create a new task
            // when navigating up, with a synthesized back stack.
            TaskStackBuilder.create(activity)
                    // Add all of this activity's parents to the back stack
                    .addNextIntentWithParentStack(upIntent)
                    // Navigate up to the closest parent
                    .startActivities();
        } else {
            // This activity is part of this app's task, so simply
            // navigate up to the logical parent activity.
            NavUtils.navigateUpTo(activity, upIntent);
        }
        return true;
    }

}
