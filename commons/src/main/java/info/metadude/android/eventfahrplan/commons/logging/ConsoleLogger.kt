package info.metadude.android.eventfahrplan.commons.logging

import android.util.Log

object ConsoleLogger : Logging {

    override fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun e(tag: String, message: String) {
        Log.e(tag, message)
    }

    override fun report(tag: String, message: String) {
        org.ligi.tracedroid.logging.Log.e(tag, message)
    }

}
