package info.metadude.android.eventfahrplan.commons.logging

object AlmostNoLogging : Logging {

    override fun d(tag: String, message: String) = Unit

    override fun e(tag: String, message: String) = Unit

    override fun report(tag: String, message: String) {
        org.ligi.tracedroid.logging.Log.e(tag, message)
    }

}
