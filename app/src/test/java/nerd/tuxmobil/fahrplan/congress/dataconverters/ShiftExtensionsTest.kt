package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.kotlin.library.engelsystem.models.Shift
import nerd.tuxmobil.fahrplan.congress.logging.Logging
import nerd.tuxmobil.fahrplan.congress.models.DayRange
import nerd.tuxmobil.fahrplan.congress.utils.DateHelper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class ShiftExtensionsTest {

    @Test
    fun oneBasedDayIndex() {
        val zoneOffset = ZoneOffset.ofHours(2)
        val startsAt = ZonedDateTime.of(2019, 8, 21, 0, 0, 0, 0, zoneOffset)
        val endsAt = DateHelper.getDayEndsAtDate(startsAt) // 23:59:59
        val dayRanges = listOf(DayRange(startsAt, endsAt))
        val dateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(1566338459), zoneOffset)
        val shift = Shift(startsAt = dateTime, endsAt = dateTime.plusSeconds(1))
        assertThat(shift.oneBasedDayIndex(NoLogging, dayRanges)).isEqualTo(1)
    }

    @Test
    fun descriptionTextWithEmptyShift() {
        assertThat(Shift().descriptionText).isEmpty()
    }

    @Test
    fun descriptionTextWithShiftWithLocationName() {
        assertThat(Shift(locationName = "Room 23").descriptionText).isEqualTo("Room 23")
    }

    @Test
    fun descriptionTextWithShiftWithLocationUrl() {
        assertThat(Shift(locationUrl = "https://example.com").descriptionText).isEqualTo("<a href=\"https://example.com\">https://example.com</a>")
    }

    @Test
    fun descriptionTextWithShiftWithLocationDescription() {
        assertThat(Shift(locationDescription = "The large green room.").descriptionText).isEqualTo("The large green room.")
    }

    @Test
    fun descriptionTextWithShiftWithUserComment() {
        assertThat(Shift(userComment = "Don't forget a warm jacket.").descriptionText).isEqualTo("<em>Don't forget a warm jacket.</em>")
    }

    @Test
    fun descriptionTextWithShiftWithAllFields() {
        val shift = Shift(
                locationName = "Room 42",
                locationUrl = "https://conference.org",
                locationDescription = "The small orange room.",
                userComment = "Take a bottle of water with you"
        )
        val text = "Room 42<br /><a href=\"https://conference.org\">https://conference.org</a><br /><br />The small orange room.<br /><br /><em>Take a bottle of water with you</em>"
        assertThat(shift.descriptionText).isEqualTo(text)
    }

    object NoLogging : Logging {
        override fun d(tag: String, message: String) = Unit
        override fun e(tag: String, message: String) = Unit
    }

}
