package nerd.tuxmobil.fahrplan.congress.alarms

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsState.Loading
import nerd.tuxmobil.fahrplan.congress.alarms.AlarmsState.Success
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.commons.ScreenNavigation
import nerd.tuxmobil.fahrplan.congress.models.Alarm
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@ExtendWith(MainDispatcherTestExtension::class)
class AlarmsViewModelTest {

    private companion object {
        const val CONTENT_DESCRIPTION = "Some content description"
        const val FIRES_AT_TEXT = "***"
        val SESSION_STARTS_AT = Moment.ofEpochMilli(1683981000000) // 2023-05-13T12:30:00+00:00
        const val ALARM_TIME_IN_MIN = 10
        val ALARM_STARTS_AT = SESSION_STARTS_AT.minusMinutes(ALARM_TIME_IN_MIN.toLong())

    }

    @Test
    fun `alarmsState emits Loading`() = runTest {
        val repository = createRepository(
            alarmsList = emptyList(),
            sessionsFlow = emptyFlow()
        )
        val viewModel = createViewModel(repository)
        viewModel.alarmsState.test {
            assertThat(awaitItem()).isEqualTo(Loading)
        }
        verifyInvokedNever(repository).readUseDeviceTimeZoneEnabled()
    }

    @Test
    fun `alarmsState emits empty values Success when alarms emits empty list`() = runTest {
        val repository = createRepository(
            alarmsList = emptyList(),
            sessionsFlow = flowOf(listOf(Session("s0")))
        )
        val viewModel = createViewModel(repository)
        viewModel.alarmsState.test {
            assertThat(awaitSuccess().sessionAlarmParameters).isEqualTo(emptyList<SessionAlarmParameter>())
        }
    }

    @Test
    fun `alarmsState emits empty values Success when sessions emits empty list`() = runTest {
        val repository = createRepository(
            alarmsList = listOf(createAlarm("s0")),
            sessionsFlow = flowOf(emptyList())
        )
        val viewModel = createViewModel(repository)
        viewModel.alarmsState.test {
            assertThat(awaitSuccess().sessionAlarmParameters).isEqualTo(emptyList<SessionAlarmParameter>())
        }
    }

    @Test
    fun `alarmsState emits empty values Success when alarm and session are not associated`() =
        runTest {
            val repository = createRepository(
                alarmsList = listOf(createAlarm("s1")),
                sessionsFlow = flowOf(listOf(Session("s0")))
            )
            val viewModel = createViewModel(repository)
            viewModel.alarmsState.test {
                assertThat(awaitSuccess().sessionAlarmParameters).isEqualTo(emptyList<SessionAlarmParameter>())
            }
        }

    @Test
    fun `alarmsState emits values list Success when alarm and session are associated`() = runTest {
        val repository = createRepository(
            alarmsList = listOf(
                createAlarm(
                    sessionId = "s0",
                    alarmTimeInMin = ALARM_TIME_IN_MIN,
                    alarmStartsAt = ALARM_STARTS_AT.toMilliseconds()
                )
            ),
            sessionsFlow = flowOf(
                listOf(
                    Session(
                        sessionId = "s0",
                        title = "Title",
                        subtitle = "Subtitle",
                        dateUTC = SESSION_STARTS_AT.toMilliseconds(),
                    )
                )
            )
        )
        val viewModel = createViewModel(repository)
        viewModel.alarmsState.test {
            val parameter = SessionAlarmParameter(
                sessionId = "s0",
                title = "Title",
                titleContentDescription = CONTENT_DESCRIPTION,
                subtitle = "Subtitle",
                subtitleContentDescription = CONTENT_DESCRIPTION,
                alarmOffsetContentDescription = CONTENT_DESCRIPTION,
                alarmOffsetInMin = ALARM_TIME_IN_MIN,
                firesAt = ALARM_STARTS_AT.toMilliseconds(),
                firesAtText = FIRES_AT_TEXT,
                firesAtContentDescription = CONTENT_DESCRIPTION,
                dayIndex = 0,
                selected = false
            )
            assertThat(awaitSuccess().sessionAlarmParameters).isEqualTo(listOf(parameter))
        }
        verifyInvokedOnce(repository).readUseDeviceTimeZoneEnabled()
    }

    @Test
    fun `onItemClick triggers navigation to session details`() = runTest {
        val repository = createRepository(
            alarmsList = listOf(
                createAlarm(
                    sessionId = "s0",
                    alarmTimeInMin = ALARM_TIME_IN_MIN,
                    alarmStartsAt = ALARM_STARTS_AT.toMilliseconds()
                )
            ),
            sessionsFlow = flowOf(
                listOf(
                    Session(
                        sessionId = "s0",
                        title = "Title",
                        subtitle = "Subtitle",
                        dateUTC = SESSION_STARTS_AT.toMilliseconds(),
                    )
                )
            )
        )
        val screenNavigation = mock<ScreenNavigation>()
        doNothing().`when`(screenNavigation).navigateToSessionDetails(any())
        val viewModel = createViewModel(repository, screenNavigation = screenNavigation)
        viewModel.alarmsState.take(1).collect { state ->
            assertThat(state).isInstanceOf(Success::class.java)
            val success = state as Success
            val sessionAlarmParameter = state.sessionAlarmParameters.first()
            success.onItemClick(sessionAlarmParameter)
            verifyInvokedOnce(screenNavigation).navigateToSessionDetails(any())
        }
    }

    @Test
    fun `onDeleteItemClick invokes corresponding repository and alarm services functions`() =
        runTest {
            val repository = createRepository(
                alarmsList = listOf(
                    createAlarm(
                        sessionId = "s0",
                        alarmTimeInMin = ALARM_TIME_IN_MIN,
                        alarmStartsAt = ALARM_STARTS_AT.toMilliseconds()
                    )
                ),
                sessionsFlow = flowOf(
                    listOf(
                        Session(
                            sessionId = "s0",
                            title = "Title",
                            subtitle = "Subtitle",
                            dateUTC = SESSION_STARTS_AT.toMilliseconds(),
                        )
                    )
                )
            )
            val alarmServices = mock<AlarmServices>()
            val viewModel = createViewModel(repository, alarmServices)
            viewModel.alarmsState.take(1).collect { state ->
                assertThat(state).isInstanceOf(Success::class.java)
                val success = state as Success
                val sessionAlarmParameter = state.sessionAlarmParameters.first()
                success.onDeleteItemClick(sessionAlarmParameter)
                verifyInvokedOnce(repository).deleteAlarmForSessionId(any())
                verifyInvokedOnce(alarmServices).discardSessionAlarm(any())
            }
        }

    @Test
    fun `onDeleteAllClick never invokes repository nor alarm services functions when no alarm is present`() =
        runTest {
            val alarmServices = mock<AlarmServices>()
            val repository = createRepository(
                alarmsList = emptyList()
            )
            val viewModel = createViewModel(repository, alarmServices)
            viewModel.onDeleteAllClick()
            verifyInvokedOnce(repository).readAlarms()
            verifyInvokedNever(alarmServices).discardSessionAlarm(any())
            verifyInvokedNever(repository).deleteAllAlarms()
        }

    @Test
    fun `onDeleteAllClick invokes corresponding repository and alarm services functions when any alarm is present`() =
        runTest {
            val alarmServices = mock<AlarmServices>()
            val repository = createRepository(
                alarmsList = listOf(createAlarm("s0"))
            )
            val viewModel = createViewModel(repository, alarmServices)
            viewModel.onDeleteAllClick()
            verifyInvokedOnce(repository).readAlarms()
            verifyInvokedOnce(alarmServices).discardSessionAlarm(any())
            verifyInvokedOnce(repository).deleteAllAlarms()
        }

    private fun createRepository(
        sessionsFlow: Flow<List<Session>> = flowOf(emptyList()),
        alarmsList: List<Alarm> = emptyList()
    ) = mock<AppRepository> {
        on { alarms } doReturn flowOf(alarmsList)
        on { sessions } doReturn sessionsFlow
        on { sessionsWithoutShifts } doReturn emptyFlow()
        on { readAlarms(any()) } doReturn alarmsList
        on { readUseDeviceTimeZoneEnabled() } doReturn true
        on { deleteAlarmForSessionId(any()) } doReturn 0
        on { deleteAllAlarms() } doReturn 0
    }

    private fun createViewModel(
        repository: AppRepository,
        alarmServices: AlarmServices = mock(),
        screenNavigation: ScreenNavigation = mock(),
    ) = AlarmsViewModel(
        repository = repository,
        executionContext = TestExecutionContext,
        alarmServices = alarmServices,
        resourceResolving = TestResourceResolver,
        screenNavigation = screenNavigation,
        formattingDelegate = { _, _, _ -> FIRES_AT_TEXT }
    )

    private object TestResourceResolver : ResourceResolving {
        override fun getString(id: Int, vararg formatArgs: Any) = CONTENT_DESCRIPTION
    }

    private fun createAlarm(
        sessionId: String,
        alarmTimeInMin: Int = 10,
        alarmStartsAt: Long = 1620909000000
    ) = Alarm(
        alarmTimeInMin = alarmTimeInMin,
        day = 2,
        displayTime = -1,
        sessionId = sessionId,
        sessionTitle = "Unused",
        startTime = alarmStartsAt,
        timeText = "Unused"
    )

    private suspend fun <T> ReceiveTurbine<T>.awaitSuccess() = awaitItem() as Success

}
