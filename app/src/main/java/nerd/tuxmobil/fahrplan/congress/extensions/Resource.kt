@file:JvmName("Resource")

package nerd.tuxmobil.fahrplan.congress.extensions

import android.content.res.Configuration
import android.content.res.Resources
import nerd.tuxmobil.fahrplan.congress.MyApp
import nerd.tuxmobil.fahrplan.congress.R

fun Resources.getNormalizedBoxHeight(scale: Float, className: String): Int {
    logOrientation({
        MyApp.LogDebug(className, it)
    })
    return (getInteger(R.integer.box_height) * scale).toInt()
}

private fun Resources.logOrientation(logger: (String) -> Unit) {
    val orientation = when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> "landscape"
        else -> "other orientation"
    }
    logger.invoke(orientation)
}
