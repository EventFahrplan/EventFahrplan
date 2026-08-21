package nerd.tuxmobil.fahrplan.congress.schedule

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.RoomData
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.schedule.PendingCardShakes.Companion.MAX_SHAKE_DELAY
import org.junit.jupiter.api.Test

class PendingCardShakesTest {

    private companion object {
        const val FIRED_AT = 1_000L
    }

    private var elapsedRealtime = FIRED_AT
    private val pendingCardShakes = PendingCardShakes(elapsedRealtime = { elapsedRealtime })

    @Test
    fun `isRequested returns false when no shake has been requested`() {
        assertThat(pendingCardShakes.isRequested("session-1", hasAlarm = false)).isFalse()
    }

    @Test
    fun `request requests a shake for a displayed session`() {
        pendingCardShakes.update(createScheduleData("session-1", "session-2"))

        pendingCardShakes.request("session-1", FIRED_AT)

        assertThat(pendingCardShakes.isRequested("session-1", hasAlarm = false)).isTrue()
        assertThat(pendingCardShakes.isRequested("session-2", hasAlarm = false)).isFalse()
    }

    @Test
    fun `isRequested returns false while the card still shows its alarm`() {
        pendingCardShakes.update(createScheduleData("session-1"))

        // The firing event arrived before the schedule update which removes the alarm.
        pendingCardShakes.request("session-1", FIRED_AT)

        assertThat(pendingCardShakes.isRequested("session-1", hasAlarm = true)).isFalse()
        assertThat(pendingCardShakes.isRequested("session-1", hasAlarm = false)).isTrue()
    }

    @Test
    fun `isRequested returns true until the maximum shake delay passed`() {
        pendingCardShakes.update(createScheduleData("session-1"))
        pendingCardShakes.request("session-1", FIRED_AT)

        elapsedRealtime = FIRED_AT + MAX_SHAKE_DELAY.inWholeMilliseconds

        assertThat(pendingCardShakes.isRequested("session-1", hasAlarm = false)).isTrue()
    }

    @Test
    fun `isRequested returns false once the maximum shake delay passed`() {
        pendingCardShakes.update(createScheduleData("session-1"))
        pendingCardShakes.request("session-1", FIRED_AT)

        // E.g. the card's room column was off-screen and the user scrolls to it much later.
        elapsedRealtime = FIRED_AT + MAX_SHAKE_DELAY.inWholeMilliseconds + 1

        assertThat(pendingCardShakes.isRequested("session-1", hasAlarm = false)).isFalse()
    }

    @Test
    fun `request ignores an alarm which fired longer ago than the maximum shake delay`() {
        pendingCardShakes.update(createScheduleData("session-1"))

        // E.g. the event was queued while the schedule was in the background.
        elapsedRealtime = FIRED_AT + MAX_SHAKE_DELAY.inWholeMilliseconds + 1
        pendingCardShakes.request("session-1", FIRED_AT)
        elapsedRealtime = FIRED_AT

        assertThat(pendingCardShakes.isRequested("session-1", hasAlarm = false)).isFalse()
    }

    @Test
    fun `request ignores a session which is not displayed`() {
        pendingCardShakes.update(createScheduleData("session-1"))

        pendingCardShakes.request("session-2", FIRED_AT)
        pendingCardShakes.update(createScheduleData("session-2"))

        assertThat(pendingCardShakes.isRequested("session-2", hasAlarm = false)).isFalse()
    }

    @Test
    fun `request ignores a session before any schedule is displayed`() {
        pendingCardShakes.request("session-1", FIRED_AT)
        pendingCardShakes.update(createScheduleData("session-1"))

        assertThat(pendingCardShakes.isRequested("session-1", hasAlarm = false)).isFalse()
    }

    @Test
    fun `update keeps an unfinished request for a displayed session`() {
        pendingCardShakes.update(createScheduleData("session-1"))
        pendingCardShakes.request("session-1", FIRED_AT)

        pendingCardShakes.update(createScheduleData("session-1"))

        assertThat(pendingCardShakes.isRequested("session-1", hasAlarm = false)).isTrue()
    }

    @Test
    fun `update drops a request for a session which is no longer displayed`() {
        pendingCardShakes.update(createScheduleData("session-1"))
        pendingCardShakes.request("session-1", FIRED_AT)

        pendingCardShakes.update(createScheduleData("session-2"))

        assertThat(pendingCardShakes.isRequested("session-1", hasAlarm = false)).isFalse()
    }

    @Test
    fun `update drops an expired request`() {
        pendingCardShakes.update(createScheduleData("session-1"))
        pendingCardShakes.request("session-1", FIRED_AT)

        elapsedRealtime = FIRED_AT + MAX_SHAKE_DELAY.inWholeMilliseconds + 1
        pendingCardShakes.update(createScheduleData("session-1"))
        // Turning the clock back reveals whether the request is still stored.
        elapsedRealtime = FIRED_AT

        assertThat(pendingCardShakes.isRequested("session-1", hasAlarm = false)).isFalse()
    }

    @Test
    fun `onShakeFinished removes only the request of the given session`() {
        pendingCardShakes.update(createScheduleData("session-1", "session-2"))
        pendingCardShakes.request("session-1", FIRED_AT)
        pendingCardShakes.request("session-2", FIRED_AT)

        pendingCardShakes.onShakeFinished("session-1")

        assertThat(pendingCardShakes.isRequested("session-1", hasAlarm = false)).isFalse()
        assertThat(pendingCardShakes.isRequested("session-2", hasAlarm = false)).isTrue()
    }

    private fun createScheduleData(vararg sessionIds: String) = ScheduleData(
        dayIndex = 0,
        roomDataList = listOf(
            RoomData(
                roomName = "room",
                sessions = sessionIds.map { Session(sessionId = it) },
            )
        ),
    )

}
