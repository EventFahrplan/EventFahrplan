package info.metadude.android.eventfahrplan.network.validation

import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.network.models.Session
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DateFieldValidationTest {

    @Test
    fun `validate - is invalid if one of two lectures is invalid`() {
        val validation = DateFieldValidation(TestLogger)

        val start = Moment("2019-01-01")
        val end = Moment("2019-01-01")

        val lectures = listOf(
                // date and dateUTC do not represent the same
                Session(date = "2019-01-02", dateUTC = start.toMilliseconds(), eventId = "1"),
                Session(date = "2019-01-01", dateUTC = end.toMilliseconds(), eventId = "2")
        )

        val isValid = validation.validate(lectures)

        assertThat(isValid).isFalse()

        validation.printValidationErrors()
    }

    @Test
    fun `validate - is invalid if any event outside range`() {
        val validation = DateFieldValidation(TestLogger)

        val start = Moment("2019-01-01")
        val end = Moment("2019-01-03")

        val lectures = listOf(
                // date=jan 2 does not correspond to dateUTC field value
                // since event 1 defines range start, this leads to a problem, if another event has valid data, but is before event 1.
                Session(date = "2019-01-02", dateUTC = start.toMilliseconds(), eventId = "1"),
                // range starts with event 1 => event 2 is outside of the range
                Session(date = "2019-01-01", dateUTC = Moment("2019-01-01").toMilliseconds(), eventId = "2"),
                Session(date = "2019-01-03", dateUTC = end.toMilliseconds(), eventId = "3")
        )

        val isValid = validation.validate(lectures)

        assertThat(isValid).isFalse()

        validation.printValidationErrors()
    }

    @Test
    fun `validate - all data integer`() {
        val validation = DateFieldValidation(TestLogger)

        val start = Moment("2019-01-01")
        val end = Moment("2019-01-03")

        val lectures = listOf(
                Session(date = "2019-01-01", dateUTC = start.toMilliseconds(), eventId = "1"),
                Session(date = "2019-01-02", dateUTC = Moment("2019-01-02").toMilliseconds(), eventId = "2"),
                Session(date = "2019-01-03", dateUTC = end.toMilliseconds(), eventId = "3")
        )

        val isValid = validation.validate(lectures)

        assertThat(isValid).isTrue()

        validation.printValidationErrors()
    }

    @Test
    fun `validate - no lectures`() {
        val validation = DateFieldValidation(TestLogger)

        val lectures = emptyList<Session>()

        val isValid = validation.validate(lectures)

        assertThat(isValid).isTrue()

        validation.printValidationErrors()
    }

    @Test
    fun `validate - two lectures at same day`() {
        val validation = DateFieldValidation(TestLogger)

        val start = Moment("2019-01-01")
        val end = Moment("2019-01-01")

        val lectures = listOf(
                Session(date = "2019-01-01", dateUTC = start.toMilliseconds(), eventId = "1"),
                Session(date = "2019-01-01", dateUTC = end.toMilliseconds(), eventId = "2")
        )

        val isValid = validation.validate(lectures)

        assertThat(isValid).isTrue()

        validation.printValidationErrors()
    }

    @Test
    fun `validate - two lectures on consecutive days`() {
        val validation = DateFieldValidation(TestLogger)

        val start = Moment("2019-01-01")
        val end = Moment("2019-01-02")

        val lectures = listOf(
                Session(date = "2019-01-01", dateUTC = start.toMilliseconds(), eventId = "1"),
                Session(date = "2019-01-02", dateUTC = end.toMilliseconds(), eventId = "2")
        )

        val isValid = validation.validate(lectures)

        assertThat(isValid).isTrue()

        validation.printValidationErrors()
    }

    object TestLogger : Logging {
        override fun d(tag: String, message: String) = println("$tag $message")

        override fun e(tag: String, message: String) = println("$tag $message")

        override fun report(tag: String, message: String) = println("$tag $message")
    }
}