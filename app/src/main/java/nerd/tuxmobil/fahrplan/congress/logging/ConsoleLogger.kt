package nerd.tuxmobil.fahrplan.congress.logging

import nerd.tuxmobil.fahrplan.congress.MyApp

object ConsoleLogger : Logging {

    override fun d(tag: String, message: String) {
        MyApp.LogDebug(tag, message)
    }

}
