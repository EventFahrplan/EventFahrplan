package info.metadude.android.eventfahrplan.network.validation

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.network.models.Session
import org.junit.jupiter.api.Test

class DateFieldValidationTest {

    @Test
    fun `validate returns false if one session is after the end`() {
        val validation = createValidation()

        val start = Moment.parseDate("2019-01-01")
        val end = Moment.parseDate("2019-01-01")

        val sessions = listOf(
                // date and dateUTC do not represent the same
                Session(date = "2019-01-02", dateUTC = start.toMilliseconds(), sessionId = "1"),
                Session(date = "2019-01-01", dateUTC = end.toMilliseconds(), sessionId = "2")
        )

        val isValid = validation.validate(sessions)

        assertThat(isValid).isFalse()

        validation.printValidationErrors()
    }

    @Test
    fun `validate returns false if any session is outside the range`() {
        val validation = createValidation()

        val start = Moment.parseDate("2019-01-01")
        val end = Moment.parseDate("2019-01-03")

        val sessions = listOf(
                // date=jan 2 does not correspond to dateUTC field value
                // since session 1 defines range start, this leads to a problem, if another session has valid data, but is before session 1.
                Session(date = "2019-01-02", dateUTC = start.toMilliseconds(), sessionId = "1"),
                // range starts with session 1 => session 2 is outside of the range
                Session(date = "2019-01-01", dateUTC = Moment.parseDate("2019-01-01").toMilliseconds(), sessionId = "2"),
                Session(date = "2019-01-03", dateUTC = end.toMilliseconds(), sessionId = "3")
        )

        val isValid = validation.validate(sessions)

        assertThat(isValid).isFalse()

        validation.printValidationErrors()
    }

    @Test
    fun `validate returns true if session is between start and end`() {
        val validation = createValidation()

        val start = Moment.parseDate("2019-01-01")
        val end = Moment.parseDate("2019-01-03")

        val sessions = listOf(
                Session(date = "2019-01-01", dateUTC = start.toMilliseconds(), sessionId = "1"),
                Session(date = "2019-01-02", dateUTC = Moment.parseDate("2019-01-02").toMilliseconds(), sessionId = "2"),
                Session(date = "2019-01-03", dateUTC = end.toMilliseconds(), sessionId = "3")
        )

        val isValid = validation.validate(sessions)

        assertThat(isValid).isTrue()

        validation.printValidationErrors()
    }

    @Test
    fun `validate returns true for no sessions`() {
        val validation = createValidation()

        val sessions = emptyList<Session>()

        val isValid = validation.validate(sessions)

        assertThat(isValid).isTrue()

        validation.printValidationErrors()
    }

    @Test
    fun `validate returns true for two sessions on the same day`() {
        val validation = createValidation()

        val start = Moment.parseDate("2019-01-01")
        val end = Moment.parseDate("2019-01-01")

        val sessions = listOf(
                Session(date = "2019-01-01", dateUTC = start.toMilliseconds(), sessionId = "1"),
                Session(date = "2019-01-01", dateUTC = end.toMilliseconds(), sessionId = "2")
        )

        val isValid = validation.validate(sessions)

        assertThat(isValid).isTrue()

        validation.printValidationErrors()
    }

    @Test
    fun `validate returns true for two sessions on consecutive days`() {
        val validation = createValidation()

        val start = Moment.parseDate("2019-01-01")
        val end = Moment.parseDate("2019-01-02")

        val sessions = listOf(
                Session(date = "2019-01-01", dateUTC = start.toMilliseconds(), sessionId = "1"),
                Session(date = "2019-01-02", dateUTC = end.toMilliseconds(), sessionId = "2")
        )

        val isValid = validation.validate(sessions)

        assertThat(isValid).isTrue()

        validation.printValidationErrors()
    }

    private fun createValidation() = DateFieldValidation(TestLogger)

    private object TestLogger : Logging {
        override fun d(tag: String, message: String) = println("$tag $message")

        override fun e(tag: String, message: String) = println("$tag $message")

        override fun report(tag: String, message: String) = println("$tag $message")
    }
}
