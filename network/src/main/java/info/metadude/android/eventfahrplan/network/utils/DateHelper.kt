@file:JvmName("DateHelper")

package info.metadude.android.eventfahrplan.network.utils

import info.metadude.android.eventfahrplan.commons.temporal.Moment
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

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
    val moment = Moment(timeUTC)
    return moment.hour * 60 + moment.minute
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
