package nerd.tuxmobil.fahrplan.congress.logging

import android.util.Log
import nerd.tuxmobil.fahrplan.congress.MyApp

object ConsoleLogger : Logging {

    override fun d(tag: String, message: String) {
        MyApp.LogDebug(tag, message)
    }

    override fun e(tag: String, message: String) {
        Log.e(tag, message)
    }

}
