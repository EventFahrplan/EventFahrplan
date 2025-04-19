package nerd.tuxmobil.fahrplan.congress.repositories

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.database.models.Highlight
import info.metadude.android.eventfahrplan.database.repositories.AlarmsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.HighlightsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.SessionsDatabaseRepository
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.exceptions.ExceptionHandling
import nerd.tuxmobil.fahrplan.congress.models.RoomData
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.preferences.SharedPreferencesRepository
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.coroutines.CoroutineContext
import info.metadude.android.eventfahrplan.database.models.Alarm as AlarmDatabaseModel
import info.metadude.android.eventfahrplan.database.models.Session as SessionDatabaseModel
import nerd.tuxmobil.fahrplan.congress.models.Alarm as AlarmAppModel
import nerd.tuxmobil.fahrplan.congress.models.Session as SessionAppModel

/**
 * Covers [AppRepository.uncanceledSessionsForDayIndex].
 */
class AppRepositoryUncanceledSessionsForDayIndexTest {

    private val sessionId = "123"
    private val dayIndex = 1
    private val roomName = "Room 1"
    private val alarmsDatabaseRepository = InMemoryAlarmRepository()
    private val highlightsDatabaseRepository = InMemoryHighlightsDatabaseRepository()
    private val roomProvider = object : RoomProvider {
        override val prioritizedRooms: List<String> = emptyList()
        override val deprioritizedRooms: List<String> = emptyList()
    }
    private val sessionsTransformer = SessionsTransformer(roomProvider)

    private val testableAppRepository: AppRepository
        get() = with(AppRepository) {
            val executionContext = TestExecutionContext
            initialize(
                context = mock(),
                logging = mock(),
                executionContext = executionContext,
                databaseScope = DatabaseScope.of(executionContext, StandardOutputExceptionHandler),
                networkScope = mock(),
                okHttpClient = mock(),
                alarmsDatabaseRepository = alarmsDatabaseRepository,
                sessionsDatabaseRepository = createSessionsDatabaseRepository(),
                highlightsDatabaseRepository = highlightsDatabaseRepository,
                scheduleNetworkRepository = mock(),
                engelsystemNetworkRepository = mock(),
                sharedPreferencesRepository = createSharedPreferencesRepository(),
                sessionsTransformer = sessionsTransformer,
            )
            return this
        }

    @Nested
    inner class Alarm {

        @Test
        fun `uncanceledSessionsForDayIndex emits ScheduleData containing session with alarm when updateAlarm is invoked`() =
            runTest {
                testableAppRepository.uncanceledSessionsForDayIndex.test {
                    val expectedScheduleData1 = createScheduleData(createSession(
                        isHighlight = false,
                        hasAlarm = false, // initially no alarm
                    ))

                    val actualScheduleData1 = awaitItem()
                    assertThat(actualScheduleData1).isEqualTo(expectedScheduleData1)

                    // Simulate scheduling an alarm
                    testableAppRepository.updateAlarm(createAlarm())
                    val expectedScheduleData2 = createScheduleData(createSession(
                        isHighlight = false,
                        hasAlarm = true, // alarm is now set
                    ))
                    val actualScheduleData2 = awaitItem()
                    assertThat(actualScheduleData2).isEqualTo(expectedScheduleData2)
                }
            }

        @Test
        fun `uncanceledSessionsForDayIndex emits ScheduleData containing session without alarm when deleteAlarmForAlarmId is invoked`() =
            runTest {
                testableAppRepository.updateAlarm(createAlarm())

                testableAppRepository.uncanceledSessionsForDayIndex.test {
                    val expectedScheduleData1 = createScheduleData(createSession(
                        isHighlight = false,
                        hasAlarm = true, // initially alarm is set
                    ))

                    val actualScheduleData1 = awaitItem()
                    assertThat(actualScheduleData1).isEqualTo(expectedScheduleData1)

                    // Simulate canceling specific alarm
                    testableAppRepository.deleteAlarmForAlarmId(0)
                    val expectedScheduleData2 = createScheduleData(createSession(
                        isHighlight = false,
                        hasAlarm = false, // alarm is now canceled
                    ))
                    val actualScheduleData2 = awaitItem()
                    assertThat(actualScheduleData2).isEqualTo(expectedScheduleData2)
                }
            }

        @Test
        fun `uncanceledSessionsForDayIndex emits ScheduleData containing session without alarm when deleteAlarmForSessionId is invoked`() =
            runTest {
                testableAppRepository.updateAlarm(createAlarm())

                testableAppRepository.uncanceledSessionsForDayIndex.test {
                    val expectedScheduleData1 = createScheduleData(createSession(
                        isHighlight = false,
                        hasAlarm = true, // initially alarm is set
                    ))

                    val actualScheduleData1 = awaitItem()
                    assertThat(actualScheduleData1).isEqualTo(expectedScheduleData1)

                    // Simulate canceling specific alarm
                    testableAppRepository.deleteAlarmForSessionId(sessionId)
                    val expectedScheduleData2 = createScheduleData(createSession(
                        isHighlight = false,
                        hasAlarm = false, // alarm is now canceled
                    ))
                    val actualScheduleData2 = awaitItem()
                    assertThat(actualScheduleData2).isEqualTo(expectedScheduleData2)
                }
            }

        @Test
        fun `uncanceledSessionsForDayIndex emits ScheduleData containing session without alarm when deleteAllAlarms is invoked`() =
            runTest {
                testableAppRepository.updateAlarm(createAlarm())

                testableAppRepository.uncanceledSessionsForDayIndex.test {
                    val expectedScheduleData1 = createScheduleData(createSession(
                        isHighlight = false,
                        hasAlarm = true, // initially alarm is set
                    ))

                    val actualScheduleData1 = awaitItem()
                    assertThat(actualScheduleData1).isEqualTo(expectedScheduleData1)

                    // Simulate canceling all alarms
                    testableAppRepository.deleteAllAlarms()
                    val expectedScheduleData2 = createScheduleData(createSession(
                        isHighlight = false,
                        hasAlarm = false, // alarm is now canceled
                    ))
                    val actualScheduleData2 = awaitItem()
                    assertThat(actualScheduleData2).isEqualTo(expectedScheduleData2)
                }
            }

    }

    @Nested
    inner class Highlight {

        @Test
        fun `uncanceledSessionsForDayIndex emits ScheduleData containing session with highlight when updateHighlight is invoked`() =
            runTest {
                testableAppRepository.uncanceledSessionsForDayIndex.test {
                    val expectedScheduleData1 = createScheduleData(createSession(
                        isHighlight = false, // initially no highlight
                        hasAlarm = false,
                    ))
                    val actualScheduleData1 = awaitItem()
                    assertThat(actualScheduleData1).isEqualTo(expectedScheduleData1)

                    // Simulate highlighting a session
                    testableAppRepository.updateHighlight(SessionAppModel(sessionId))
                    val expectedScheduleData2 = createScheduleData(createSession(
                        isHighlight = true, // highlight is now set
                        hasAlarm = false,
                    ))
                    val actualScheduleData2 = awaitItem()
                    assertThat(actualScheduleData2).isEqualTo(expectedScheduleData2)
                }
            }

        @Test
        fun `uncanceledSessionsForDayIndex emits ScheduleData containing session without highlight when deleteHighlight is invoked`() =
            runTest {
                testableAppRepository.updateHighlight(createSession(isHighlight = true, hasAlarm = false))

                testableAppRepository.uncanceledSessionsForDayIndex.test {
                    val expectedScheduleData1 = createScheduleData(createSession(
                        isHighlight = true, // initially with highlight
                        hasAlarm = false,
                    ))
                    val actualScheduleData1 = awaitItem()
                    assertThat(actualScheduleData1).isEqualTo(expectedScheduleData1)

                    // Simulate removing highlight from session
                    testableAppRepository.deleteHighlight(sessionId)
                    val expectedScheduleData2 = createScheduleData(createSession(
                        isHighlight = false, // highlight is now removed
                        hasAlarm = false,
                    ))
                    val actualScheduleData2 = awaitItem()
                    assertThat(actualScheduleData2).isEqualTo(expectedScheduleData2)
                }
            }

        @Test
        fun `uncanceledSessionsForDayIndex emits ScheduleData containing session without highlight when deleteAllHighlights is invoked`() =
            runTest {
                testableAppRepository.updateHighlight(createSession(isHighlight = true, hasAlarm = false))

                testableAppRepository.uncanceledSessionsForDayIndex.test {
                    val expectedScheduleData1 = createScheduleData(createSession(
                        isHighlight = true, // initially with highlight
                        hasAlarm = false,
                    ))
                    val actualScheduleData1 = awaitItem()
                    assertThat(actualScheduleData1).isEqualTo(expectedScheduleData1)

                    // Simulate removing highlights from all sessions
                    testableAppRepository.deleteAllHighlights()
                    val expectedScheduleData2 = createScheduleData(createSession(
                        isHighlight = false, // highlight is now removed
                        hasAlarm = false,
                    ))
                    val actualScheduleData2 = awaitItem()
                    assertThat(actualScheduleData2).isEqualTo(expectedScheduleData2)
                }
            }

    }

    private fun createSharedPreferencesRepository(): SharedPreferencesRepository {
        val repository = mock<SharedPreferencesRepository>()
        whenever(repository.getDisplayDayIndex())
            .thenReturn(dayIndex)
        return repository
    }

    private fun createSessionsDatabaseRepository(): SessionsDatabaseRepository {
        val repository = mock<SessionsDatabaseRepository>()
        val session = SessionDatabaseModel(sessionId, dayIndex = dayIndex, roomName = roomName)
        whenever(repository.querySessionsForDayIndexOrderedByDateUtc(any()))
            .thenReturn(listOf(session))
        return repository
    }

    private fun createAlarm() = AlarmAppModel(
        alarmTimeInMin = 0,
        day = dayIndex,
        displayTime = 0,
        sessionId = sessionId,
        sessionTitle = "",
        startTime = 0,
        timeText = "",
    )

    private fun createScheduleData(session: SessionAppModel): ScheduleData {
        return ScheduleData(
            dayIndex = dayIndex,
            roomDataList = listOf(
                RoomData(
                    roomName = roomName,
                    sessions = listOf(session),
                )
            )
        )
    }

    private fun createSession(isHighlight: Boolean, hasAlarm: Boolean) = SessionAppModel(
        sessionId = sessionId,
        dayIndex = dayIndex,
        roomName = roomName,
        isHighlight = isHighlight,
        hasAlarm = hasAlarm,
    )

}

private class InMemoryAlarmRepository : AlarmsDatabaseRepository {

    private var alarms = listOf<AlarmDatabaseModel>()

    override fun update(values: ContentValues, sessionId: String): Long {
        val alarm = AlarmDatabaseModel(sessionId = sessionId)
        alarms = listOf(alarm)
        return 0L
    }

    override fun query(): List<AlarmDatabaseModel> {
        return alarms
    }

    override fun query(sessionId: String): List<AlarmDatabaseModel> {
        throw NotImplementedError("Not needed for this test.")
    }

    override fun query(query: SQLiteDatabase.() -> Cursor): List<AlarmDatabaseModel> {
        throw NotImplementedError("Not needed for this test.")
    }

    override fun deleteAll(): Int {
        val affected = alarms.size
        alarms = emptyList()
        return affected
    }

    override fun deleteForAlarmId(alarmId: Int): Int {
        val affected = alarms.size
        alarms = emptyList()
        return affected
    }

    override fun deleteForSessionId(sessionId: String): Int {
        val affected = alarms.size
        alarms = emptyList()
        return affected
    }

    override fun delete(query: SQLiteDatabase.() -> Int): Int {
        throw NotImplementedError("Not needed for this test.")
    }

}

private class InMemoryHighlightsDatabaseRepository : HighlightsDatabaseRepository {

    private var highlights = listOf<Highlight>()

    override fun update(values: ContentValues, sessionId: String): Long {
        val highlight = Highlight(
            sessionId = sessionId.toInt(),
            isHighlight = true,
        )
        highlights = listOf(highlight)
        return 0L
    }

    override fun query(): List<Highlight> {
        return highlights
    }

    override fun queryBySessionId(sessionId: Int): Highlight? {
        return highlights.singleOrNull { it.sessionId == sessionId }
    }

    override fun delete(sessionId: String): Int {
        val affected = highlights.size
        highlights = emptyList()
        return affected
    }

    override fun deleteAll(): Int {
        val affected = highlights.size
        highlights = emptyList()
        return affected
    }
}

private object StandardOutputExceptionHandler : ExceptionHandling {
    override fun onExceptionHandling(context: CoroutineContext, throwable: Throwable) {
        println("Exception: $throwable")
    }
}
