package info.metadude.android.eventfahrplan.network.models

import info.metadude.android.eventfahrplan.network.models.Session.Companion.parseDuration
import info.metadude.android.eventfahrplan.network.models.Session.Companion.parseStartTime
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SessionTest {

    @Test
    fun parseStartTimeWith00_00() {
        assertThat(parseStartTime("00:00")).isEqualTo(0)
    }

    @Test
    fun parseStartTimeWith09_00_00() {
        // <day_change> value from Pentabarf schedule.xml
        assertThat(parseStartTime("09:00:00")).isEqualTo(540)
    }

    @Test
    fun parseStartTimeWith17_00() {
        assertThat(parseStartTime("17:00")).isEqualTo(1020)
    }

    @Test
    fun parseStartTimeWith24_00() {
        assertThat(parseStartTime("24:00")).isEqualTo(1440)
    }

    @Test
    fun parseDurationWith00_00() {
        assertThat(parseDuration("00:00")).isEqualTo(0)
    }

    @Test
    fun parseDurationWith00_30() {
        assertThat(parseDuration("00:30")).isEqualTo(30)
    }

    @Test
    fun parseDurationWith05_00() {
        assertThat(parseDuration("05:00")).isEqualTo(300)
    }

}
