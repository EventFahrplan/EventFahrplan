package nerd.tuxmobil.fahrplan.congress.alarms

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsDestination.ConfirmDeleteAll
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsEffect.NavigateTo
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsEffect.NavigateToSession
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsState.Loading
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsState.Success
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsViewEvent.OnDeleteAllClick
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsViewEvent.OnDeleteAllWithConfirmationClick
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsViewEvent.OnDeleteItemClick
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsViewEvent.OnItemClick
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExtendWith(MainDispatcherTestExtension::class)
class AlarmsViewModelTest {

    private companion object {
        val SESSION_STARTS_AT = Moment.ofEpochMilli(1683981000000) // 2023-05-13T12:30:00+00:00
        const val ALARM_TIME_IN_MIN = 10
        val ALARM_STARTS_AT = SESSION_STARTS_AT.minusMinutes(ALARM_TIME_IN_MIN.toLong())
    }

    @Nested
    inner class AlarmsState {

        @Test
        fun `alarmsState emits Loading`() = runTest {
            val repository = createRepository(
                alarms = emptyList(),
                sessions = emptyFlow(),
            )
            val viewModel = createViewModel(repository)
            viewModel.alarmsState.test {
                assertThat(awaitItem()).isEqualTo(Loading)
            }
            verifyInvokedNever(repository).readUseDeviceTimeZoneEnabled()
        }

        @Test
        fun `alarmsState emits Success when alarm and session are associated`() = runTest {
            val repository = createRepository(
                alarms = listOf(
                    createAlarm(
                        sessionId = "s0",
                        alarmStartsAt = ALARM_STARTS_AT
                    )
                ),
                sessions = flowOf(
                    listOf(
                        Session(
                            sessionId = "s0",
                            title = "Title",
                            subtitle = "Subtitle",
                            dateUTC = SESSION_STARTS_AT.toMilliseconds(),
                        )
                    )
                ),
            )
            val viewModel = createViewModel(repository)
            viewModel.alarmsState.test {
                // Success values are covered in AlarmsStateFactoryTest
                assertThat(awaitItem()).isInstanceOf(Success::class.java)
            }
            verifyInvokedOnce(repository).readUseDeviceTimeZoneEnabled()
        }

    }

    @Nested
    inner class HasAlarms {

        @Test
        fun `hasAlarms never emits when no alarms nor sessions are present`() = runTest {
            val repository = createRepository(
                alarms = emptyList(),
                sessions = emptyFlow(),
            )
            val viewModel = createViewModel(repository)
            viewModel.hasAlarms.test {
                expectNoEvents()
            }
        }

        @Test
        fun `hasAlarms emits false when no alarms present`() = runTest {
            val repository = createRepository(
                alarms = emptyList(),
                sessions = flowOf(emptyList()),
            )
            val viewModel = createViewModel(
                repository = repository,
                alarmsStateFactory = createAlarmsStateFactory(emptyList())
            )
            viewModel.hasAlarms.test {
                assertThat(awaitItem()).isFalse()
            }
        }

        @Test
        fun `hasAlarms emits true when at least one alarm is present`() = runTest {
            val repository = createRepository(
                alarms = listOf(
                    createAlarm(
                        sessionId = "s0",
                        alarmStartsAt = ALARM_STARTS_AT
                    )
                ),
                sessions = flowOf(
                    listOf(
                        Session(
                            sessionId = "s0",
                            title = "Title",
                            subtitle = "Subtitle",
                            dateUTC = SESSION_STARTS_AT.toMilliseconds(),
                        )
                    )
                ),
            )
            val viewModel = createViewModel(repository)
            viewModel.hasAlarms.test {
                assertThat(awaitItem()).isTrue()
            }
        }

    }

    @Nested
    inner class OnViewEvent {

        @Test
        fun `OnItemClick emits NavigateToSession effect`() = runTest {
            val viewModel = createViewModel(createRepository())
            viewModel.onViewEvent(OnItemClick("s13"))
            viewModel.effects.test {
                assertThat(awaitItem()).isEqualTo(NavigateToSession("s13"))
            }
        }

        @Test
        fun `OnDeleteItemClick invokes corresponding repository and alarm services functions`() =
            runTest {
                val repository = createRepository()
                val alarmServices = mock<AlarmServices>()
                val viewModel = createViewModel(repository, alarmServices)
                val session = Session(
                    sessionId = "s0",
                    title = "Title",
                    subtitle = "Subtitle",
                    dateUTC = SESSION_STARTS_AT.toMilliseconds(),
                )
                viewModel.onViewEvent(
                    OnDeleteItemClick(
                        sessionId = session.sessionId,
                        dayIndex = session.dayIndex,
                        title = session.title,
                        firesAt = ALARM_STARTS_AT,
                    )
                )
                verifyInvokedOnce(repository).deleteAlarmForSessionId(any())
                verifyInvokedOnce(alarmServices).discardSessionAlarm(any())
            }

        @Test
        fun `OnDeleteAllClick never invokes repository nor alarm services functions when no alarm is present`() =
            runTest {
                val alarmServices = mock<AlarmServices>()
                val repository = createRepository(
                    alarms = emptyList(),
                )
                val viewModel = createViewModel(repository, alarmServices)
                viewModel.onViewEvent(OnDeleteAllClick)
                verifyInvokedOnce(repository).readAlarms()
                verifyInvokedNever(alarmServices).discardSessionAlarm(any())
                verifyInvokedNever(repository).deleteAllAlarms()
            }

        @Test
        fun `OnDeleteAllClick invokes corresponding repository and alarm services functions when any alarm is present`() =
            runTest {
                val alarmServices = mock<AlarmServices>()
                val repository = createRepository(
                    alarms = listOf(createAlarm("s0")),
                )
                val viewModel = createViewModel(repository, alarmServices)
                viewModel.onViewEvent(OnDeleteAllClick)
                verifyInvokedOnce(repository).readAlarms()
                verifyInvokedOnce(alarmServices).discardSessionAlarm(any())
                verifyInvokedOnce(repository).deleteAllAlarms()
            }

        @Test
        fun `OnDeleteAllWithConfirmationClick emits NavigateTo effect`() = runTest {
            val viewModel = createViewModel(createRepository())
            viewModel.onViewEvent(OnDeleteAllWithConfirmationClick)
            viewModel.effects.test {
                assertThat(awaitItem()).isEqualTo(NavigateTo(ConfirmDeleteAll))
            }
        }

    }

    private fun createRepository(
        sessions: Flow<List<Session>> = flowOf(emptyList()),
        alarms: List<Alarm> = emptyList(),
    ) = mock<AppRepository> {
        on { this@on.alarms } doReturn flowOf(alarms)
        on { this@on.sessions } doReturn sessions
        on { this@on.sessionsWithoutShifts } doReturn emptyFlow()
        on { this@on.readAlarms(any()) } doReturn alarms
        on { this@on.readUseDeviceTimeZoneEnabled() } doReturn true
    }

    private fun createViewModel(
        repository: AppRepository,
        alarmServices: AlarmServices = mock(),
        alarmsStateFactory: AlarmsStateFactory = createAlarmsStateFactory(createSessionAlarmParameters()),
    ) = AlarmsViewModel(
        repository = repository,
        executionContext = TestExecutionContext,
        alarmServices = alarmServices,
        alarmsStateFactory = alarmsStateFactory,
    )

    private fun createAlarmsStateFactory(
        parameters: List<SessionAlarmParameter>,
    ): AlarmsStateFactory {
        return mock<AlarmsStateFactory> {
            // See AlarmsStateFactoryTest for real return values
            on { createAlarmsState(any(), any(), any()) } doReturn parameters
        }
    }

    private fun createSessionAlarmParameters() = listOf(
        mock<SessionAlarmParameter> {
            on { sessionId } doReturn "not asserted in this test"
            on { title } doReturn "not asserted in this test"
            on { firesAt } doReturn Moment.ofEpochMilli(-1) // not asserted in this test
        }
    )

    private fun createAlarm(
        sessionId: String,
        alarmStartsAt: Moment = Moment.ofEpochMilli(1620909000000),
    ) = Alarm(
        dayIndex = 2,
        sessionId = sessionId,
        sessionTitle = "Unused",
        startTime = alarmStartsAt,
    )

}
