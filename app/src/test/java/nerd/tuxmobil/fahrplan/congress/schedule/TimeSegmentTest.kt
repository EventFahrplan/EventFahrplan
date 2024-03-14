package nerd.tuxmobil.fahrplan.congress.schedule

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_MINUTE
import org.junit.jupiter.api.Test

class TimeSegmentTest {

    @Test
    fun `isMatched returns true for moment starting 15 minutes later`() {
        val minute = 25
        val actualMoment = Moment.ofEpochMilli((minute * MILLISECONDS_OF_ONE_MINUTE).toLong())
        val segment = TimeSegment.ofMoment(actualMoment)
        val moment = Moment.ofEpochMilli((minute * MILLISECONDS_OF_ONE_MINUTE).toLong())
        assertThat(segment.isMatched(moment, 15)).isEqualTo(true)
    }

}
