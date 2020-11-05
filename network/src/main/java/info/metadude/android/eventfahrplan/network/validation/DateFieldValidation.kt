package info.metadude.android.eventfahrplan.network.validation

import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.DayRange
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.network.models.Session
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
     * Returns true if the timestamps in the [Session.date] fields of each session are within a valid time range.
     * The time range is defined by the [Session.date] fields of first and last session (which are sorted by [Session.dateUTC]).
     */
    fun validate(sessions: List<Session>): Boolean {
        val sortedSessions = sessions.sortedBy { it.dateUTC }

        if (sortedSessions.isEmpty()) {
            return true
        }

        val firstDateString = sortedSessions[0].date
        val lastDateString = sortedSessions.last().date
        val range = DayRange(Moment.parseDate(firstDateString), Moment.parseDate(lastDateString))

        // Check if the time stamp in <date> is within the time range (first : last day)
        // defined by "date" attribute in the <day> nodes.
        sortedSessions.forEach { validateSession(it, range) }

        logging.d(javaClass.simpleName, "Validation result for <date> field: ${validationErrors.size} errors.")
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
