package nerd.tuxmobil.fahrplan.congress.search

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.search.TenseType.FUTURE
import nerd.tuxmobil.fahrplan.congress.search.TenseType.PAST
import org.junit.jupiter.api.Test

class TenseTypeProviderTest {

    private companion object {
        val SOME_MOMENT = Moment.ofEpochMilli(1766876400000) // 27.12.2025 11:00:00 PM
    }

    private val provider = TenseTypeProvider(SOME_MOMENT)

    @Test
    fun `getTenseType returns FUTURE for current session`() {
        val session = createSession(SOME_MOMENT.minusMinutes(30))
        assertThat(provider.getTenseType(session)).isEqualTo(FUTURE)
    }

    @Test
    fun `getTenseType returns PAST for past session`() {
        val session = createSession(SOME_MOMENT.minusMinutes(31))
        assertThat(provider.getTenseType(session)).isEqualTo(PAST)
    }

    @Test
    fun `getTenseType returns FUTURE for future session`() {
        val session = createSession(SOME_MOMENT.minusMinutes(29))
        assertThat(provider.getTenseType(session)).isEqualTo(FUTURE)
    }

    private fun createSession(startsAt: Moment) = Session(
        sessionId = "",
        dateUTC = startsAt.toMilliseconds(),
        duration = Duration.ofMinutes(30),
    )

}
