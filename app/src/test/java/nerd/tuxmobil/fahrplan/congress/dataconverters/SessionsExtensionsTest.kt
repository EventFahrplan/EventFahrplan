package nerd.tuxmobil.fahrplan.congress.dataconverters

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import info.metadude.android.eventfahrplan.network.models.Session as SessionNetworkModel

class SessionsExtensionsTest {

    @Test
    fun `toDayIndices returns an empty set if list of sessions is empty`() {
        assertThat(emptyList<SessionNetworkModel>().toDayIndices()).isEqualTo(emptySet<Int>())
    }

    @Test
    fun `toDayIndices returns a set of day indices if list of sessions contains items`() {
        val sessions = listOf(
                SessionNetworkModel(dayIndex = 0),
                SessionNetworkModel(dayIndex = 1),
                SessionNetworkModel(dayIndex = 2)
        )
        assertThat(sessions.toDayIndices()).isEqualTo(setOf(0, 1, 2))
    }

    @Test
    fun toDayRanges() {
        val session0 = SessionNetworkModel(date = "2019-08-02", dayIndex = 2)
        val session1 = SessionNetworkModel(date = "2019-08-01", dayIndex = 1)
        val session1Copy = SessionNetworkModel(date = "2019-08-01", dayIndex = 2)

        val sessions = listOf(session0, session1, session1Copy)
        val dayRanges = sessions.toDayRanges()

        assertThat(dayRanges.size).isEqualTo(2)
        assertThat(dayRanges[0].startsAt.dayOfMonth).isEqualTo(1)
        assertThat(dayRanges[0].startsAt.hour).isEqualTo(0)
        assertThat(dayRanges[1].startsAt.dayOfMonth).isEqualTo(2)
        assertThat(dayRanges[1].startsAt.hour).isEqualTo(0)

        assertThat(dayRanges[0].endsAt.dayOfMonth).isEqualTo(1)
        assertThat(dayRanges[0].endsAt.hour).isEqualTo(23)
        assertThat(dayRanges[1].endsAt.dayOfMonth).isEqualTo(2)
        assertThat(dayRanges[1].endsAt.hour).isEqualTo(23)
    }

}
