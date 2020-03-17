package info.metadude.android.eventfahrplan.network.validation

import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.DayRange
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.network.models.Lecture
import org.threeten.bp.ZoneOffset
import java.util.ArrayList

internal class DateFieldValidation constructor(
        private val logging: Logging
) {
    private val validationErrors: MutableList<ValidationError>

    init {
        validationErrors = ArrayList()
    }

    fun printValidationErrors() {
        for (validationError in validationErrors) {
            logging.d(javaClass.simpleName, validationError.toString())
        }
    }

    /**
     * Returns true if the timestamps in the [Lecture.date] fields of each event are within a valid time range.
     * The time range is defined by the [Lecture.date] fields of first and last event (which are sorted by [Lecture.dateUTC]).
     */
    fun validate(lectures: List<Lecture>): Boolean {
        val sortedLectures = lectures.sortedBy { it.dateUTC }

        if (sortedLectures.isEmpty()) {
            return true
        }

        val firstDateString = sortedLectures[0].date
        val lastDateString = sortedLectures.last().date
        val range = DayRange(Moment(firstDateString), Moment(lastDateString))

        // Check if the time stamp in <date> is within the time range (first : last day)
        // defined by "date" attribute in the <day> nodes.
        sortedLectures.forEach { validateEvent(it, range) }

        logging.d(javaClass.simpleName, "Validation result for <date> field: " + validationErrors.size + " errors.")
        return validationErrors.isEmpty()
    }

    private fun validateEvent(lecture: Lecture, dateRange: DayRange) {
        val dateUtcInMilliseconds = lecture.dateUTC
        val lectureDate = Moment(dateUtcInMilliseconds).toZonedDateTime(ZoneOffset.UTC)
        if (!dateRange.contains(lectureDate)) {
            val eventId = lecture.eventId
            val errorMessage = ("Field <date> $lectureDate of event $eventId exceeds range: [ ${dateRange.startsAt} : ${dateRange.endsAt} ]")
            val error = ValidationError(errorMessage)
            validationErrors.add(error)
        }
    }
}