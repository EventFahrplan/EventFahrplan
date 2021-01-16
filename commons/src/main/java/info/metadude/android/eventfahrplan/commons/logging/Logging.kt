package info.metadude.android.eventfahrplan.commons.logging

import info.metadude.android.eventfahrplan.commons.BuildConfig

interface Logging {

    fun d(tag: String, message: String)

    fun e(tag: String, message: String)

    fun report(tag: String, message: String)

    companion object {

        @JvmStatic
        fun get(): Logging {
            return if (BuildConfig.DEBUG) ConsoleLogger else AlmostNoLogging
        }

    }

}
