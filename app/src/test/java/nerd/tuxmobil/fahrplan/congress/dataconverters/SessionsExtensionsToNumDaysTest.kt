package nerd.tuxmobil.fahrplan.congress.dataconverters

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.jupiter.api.Test

/**
 * Covers [SessionsExtensions.toNumDays][toNumDays].
 */
class SessionsExtensionsToNumDaysTest {

    @Test
    fun `toNumDays returns zero`() {
        val virtualDays = emptyList<Session>().toNumDays()
        assertThat(virtualDays).isEqualTo(0)
    }

    @Test
    fun `toNumDays returns count of virtual days`() {
        val sessions = listOf(
            createSession("2023-12-27"),
            createSession("2023-12-27"),
            createSession("2023-12-28"),
            createSession("2023-12-29"),
            createSession("2023-12-30"),
        )
        val numDays = sessions.toNumDays()
        assertThat(numDays).isEqualTo(4)
    }

    private fun createSession(dateText: String) =
        Session(
            sessionId = "",
            dateText = dateText,
        )

}
