@file:JvmName("DateHelper")

package info.metadude.android.eventfahrplan.network.utils

import android.text.format.Time
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

fun dateIsWithinRange(date: Date, dateRange: Array<Date>): Boolean {
    val oldest = dateRange[0]
    val newest = dateRange[1]
    return (date == oldest || date.after(oldest)) && (date == newest || date
            .before(newest))
}

/**
 * Returns a formatted date string.
 * This pattern is used: yyyy-MM-dd'T'HH:mm:ssZ
 */
fun getFormattedDate(date: Date): String {
    return getFormattedDate(date, "yyyy-MM-dd'T'HH:mm:ssZ")
}

/**
 * Returns a formatted date string.
 */
fun getFormattedDate(date: Date, pattern: String): String {
    val dateFormat = SimpleDateFormat(pattern, Locale.US)
    return dateFormat.format(date)
}

fun getDateTime(text: String): Long {
    val pattern = if (text.length > 10) "yyyy-MM-dd'T'HH:mm:ssZ" else "yyyy-MM-dd"
    val date = getDate(text, pattern)
    return date?.time ?: 0
}

fun getDayChange(attributeValue: String): Int {
    val pattern = if (attributeValue.length > 10) "yyyy-MM-dd'T'HH:mm:ssZ" else "yyyy-MM-dd"
    val date = getDate(attributeValue, pattern)
            ?: return 600         // default
    val timeUTC = date.time
    val time = Time()
    time.set(timeUTC)
    return time.hour * 60 + time.minute
}

fun getDate(text: String, pattern: String): Date? {
    val dateFormat = SimpleDateFormat(pattern, Locale.US)
    var date: Date? = null
    try {
        date = dateFormat.parse(text)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
    return date
}

fun shiftByDays(date: Date, numberOfDays: Int): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.add(Calendar.DAY_OF_MONTH, numberOfDays)
    return calendar.time
}
