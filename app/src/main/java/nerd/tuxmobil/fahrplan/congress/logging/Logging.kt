package nerd.tuxmobil.fahrplan.congress.logging

import nerd.tuxmobil.fahrplan.congress.BuildConfig

interface Logging {

    fun d(tag: String, message: String)

    companion object {

        fun get(): Logging {
            return if (BuildConfig.DEBUG) ConsoleLogger else NoLogging
        }

    }

}
