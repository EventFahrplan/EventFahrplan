package nerd.tuxmobil.fahrplan.congress.models

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import org.junit.jupiter.api.Test

class NextFetchTest {

    @Test
    fun `isValid returns true`() {
        val nextFetch = NextFetch(Moment.ofEpochMilli(1), Duration.ofMilliseconds(1))
        assertThat(nextFetch.isValid()).isTrue()
    }

    @Test
    fun `isValid returns false if nextFetchAt is epoch and interval is zero`() {
        val nextFetch = NextFetch(Moment.ofEpochMilli(0), Duration.ZERO)
        assertThat(nextFetch.isValid()).isFalse()
    }

    @Test
    fun `isValid returns false if nextFetchAt is epoch`() {
        val nextFetch = NextFetch(Moment.ofEpochMilli(0), Duration.ofMilliseconds(1))
        assertThat(nextFetch.isValid()).isFalse()
    }

    @Test
    fun `isValid returns false if interval is zero`() {
        val nextFetch = NextFetch(Moment.ofEpochMilli(1), Duration.ZERO)
        assertThat(nextFetch.isValid()).isFalse()
    }

}
