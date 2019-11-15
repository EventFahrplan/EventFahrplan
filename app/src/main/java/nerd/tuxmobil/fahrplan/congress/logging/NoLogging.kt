package nerd.tuxmobil.fahrplan.congress.logging

object NoLogging : Logging {

    override fun d(tag: String, message: String) = Unit

}
