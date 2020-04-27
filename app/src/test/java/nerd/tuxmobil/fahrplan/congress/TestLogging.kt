package nerd.tuxmobil.fahrplan.congress

import info.metadude.android.eventfahrplan.commons.logging.Logging

object NoLogging : Logging {
    override fun d(tag: String, message: String) = Unit
    override fun e(tag: String, message: String) = Unit
    override fun report(tag: String, message: String) = Unit
}
