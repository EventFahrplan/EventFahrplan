package info.metadude.android.eventfahrplan.commons.temporal

import info.metadude.android.eventfahrplan.commons.temporal.DateFormatter.Companion.initialize
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Format timestamps according to system locale and system time zone.
 * Call [initialize] after system properties changed to make [DateFormatter] use current system defaults.
 * */
class DateFormatter {

    init {
        initialize()
    }

    companion object {
        private var timeShort: DateFormat? = null
        private var dateShort: DateFormat? = null
        private var dateTimeShort: DateFormat? = null
        private var dateTimeFull: DateFormat? = null

        @Synchronized
        fun initialize() {
            dateTimeFull = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.SHORT)
            dateTimeShort = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
            dateShort = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)
            timeShort = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
        }

        fun getFormattedTime(time: Long): String {
            return timeShort!!.format(Date(time))
        }

        fun getFormattedDate(time: Long): String {
            return dateShort!!.format(Date(time))
        }

        fun getFormattedDateTime(time: Long): String {
            return dateTimeFull!!.format(Date(time))
        }

        fun getFormattedDateTimeShort(time: Long): String {
            return dateTimeShort!!.format(Date(time))
        }

    }
}
