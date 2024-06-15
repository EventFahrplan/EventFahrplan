package info.metadude.android.eventfahrplan.network.validation

import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.DayRange
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.network.models.Session
import org.threeten.bp.ZoneOffset

internal class DateFieldValidation(

    private val logging: Logging

) {

    private companion object {
        const val LOG_TAG = "DateFieldValidation"
    }

    private val validationErrors: MutableList<ValidationError> = ArrayList()

    fun printValidationErrors() {
        for (validationError in validationErrors) {
            logging.d(LOG_TAG, validationError.toString())
        }
    }

    /**
     * Returns true if the timestamps in the [Session.dateText] fields of each session are within a valid time range.
     * The time range is defined by the [Session.dateText] fields of first and last session (which are sorted by [Session.dateUTC]).
     */
    fun validate(sessions: List<Session>): Boolean {
        val sortedSessions = sessions.sortedBy { it.dateUTC }

        if (sortedSessions.isEmpty()) {
            return true
        }

        val firstDateString = sortedSessions[0].dateText
        val lastDateString = sortedSessions.last().dateText
        val range = DayRange(Moment.parseDate(firstDateString), Moment.parseDate(lastDateString))

        // Check if the time stamp in <date> is within the time range (first : last day)
        // defined by "date" attribute in the <day> nodes.
        sortedSessions.forEach { validateSession(it, range) }

        logging.d(LOG_TAG, "Validation result for <date> field: ${validationErrors.size} errors.")
        return validationErrors.isEmpty()
    }

    private fun validateSession(session: Session, dateRange: DayRange) {
        val dateUtcInMilliseconds = session.dateUTC
        val sessionDate = Moment.ofEpochMilli(dateUtcInMilliseconds).toZonedDateTime(ZoneOffset.UTC)
        if (!dateRange.contains(sessionDate)) {
            val sessionId = session.sessionId
            val errorMessage = ("Field <date> '$sessionDate' of session '$sessionId' exceeds range: [ ${dateRange.startsAt} : ${dateRange.endsAt} ]")
            val error = ValidationError(errorMessage)
            validationErrors.add(error)
        }
    }
}
