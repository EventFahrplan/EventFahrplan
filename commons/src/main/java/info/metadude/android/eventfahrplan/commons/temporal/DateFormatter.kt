package info.metadude.android.eventfahrplan.commons.temporal

import java.text.SimpleDateFormat
import java.util.*

/**
 * Format timestamps according to system locale and system time zone.
 * */
class DateFormatter private constructor() {
    private val timeShort = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
    private val dateShort = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)
    private val dateTimeShort = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT)
    private val dateTimeFull = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.FULL, SimpleDateFormat.SHORT)

    fun getFormattedTime(time: Long): String {
        return timeShort.format(Date(time))
    }

    fun getFormattedDate(time: Long): String {
        return dateShort.format(Date(time))
    }

    fun getFormattedDateTime(time: Long): String {
        return dateTimeFull.format(Date(time))
    }

    fun getFormattedDateTimeShort(time: Long): String {
        return dateTimeShort.format(Date(time))
    }

    companion object {

        @JvmStatic
        fun newInstance(): DateFormatter {
            return DateFormatter();
        }
    }
}
