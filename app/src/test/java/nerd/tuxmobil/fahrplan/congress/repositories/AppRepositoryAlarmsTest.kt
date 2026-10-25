package nerd.tuxmobil.fahrplan.congress.repositories

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import info.metadude.android.eventfahrplan.database.repositories.AlarmsDatabaseRepository
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

/**
 * Covers [AppRepository.onAlarmFired] and [AppRepository.alarmFiringEvents].
 */
class AppRepositoryAlarmsTest {

    private val alarmsDatabaseRepository = mock<AlarmsDatabaseRepository> {
        on { deleteForSessionId("session-01") } doReturn 1
    }

    private val testableAppRepository: AppRepository
        get() = with(AppRepository) {
            initialize(
                context = mock(),
                logging = mock(),
                executionContext = TestExecutionContext,
                databaseScope = mock(),
                networkScope = mock(),
                okHttpClient = mock(),
                alarmsDatabaseRepository = alarmsDatabaseRepository,
                highlightsDatabaseRepository = mock(),
                sessionsDatabaseRepository = mock(),
                metaDatabaseRepository = mock(),
                scheduleNetworkRepository = mock(),
                engelsystemRepository = mock(),
                sharedPreferencesRepository = mock(),
                settingsRepository = mock(),
                sessionsTransformer = mock(),
            )
            return this
        }

    @Test
    fun `onAlarmFired emits the session id on alarmFiringEvents`() = runTest {
        val repository = testableAppRepository

        repository.alarmFiringEvents.test {
            repository.onAlarmFired("session-01")
            assertThat(awaitItem()).isEqualTo("session-01")
        }
    }

    @Test
    fun `onAlarmFired does not emit on alarmFiringEvents if no alarm was deleted`() = runTest {
        val repository = testableAppRepository

        repository.alarmFiringEvents.test {
            repository.onAlarmFired("session-without-alarm")
            expectNoEvents()
        }
    }

    @Test
    fun `onAlarmFired deletes the alarm of the session`() {
        testableAppRepository.onAlarmFired("session-01")

        verifyInvokedOnce(alarmsDatabaseRepository).deleteForSessionId("session-01")
    }

    @Test
    fun `deleteAlarmForSessionId does not emit on alarmFiringEvents`() = runTest {
        val repository = testableAppRepository

        repository.alarmFiringEvents.test {
            repository.deleteAlarmForSessionId("session-01")
            expectNoEvents()
        }
    }

}
