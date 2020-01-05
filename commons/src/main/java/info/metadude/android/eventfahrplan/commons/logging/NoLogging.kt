package info.metadude.android.eventfahrplan.commons.logging

object NoLogging : Logging {

    override fun d(tag: String, message: String) = Unit

    override fun e(tag: String, message: String) = Unit

}
