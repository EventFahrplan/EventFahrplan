package nerd.tuxmobil.fahrplan.congress.repositories

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.database.repositories.SessionsDatabaseRepository
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import info.metadude.android.eventfahrplan.database.models.Session as SessionDatabaseModel

/**
 * Covers [AppRepository.sessionsWithoutShifts].
 */
class AppRepositorySessionsWithoutShiftsTest {

    private val testableAppRepository: AppRepository
        get() = with(AppRepository) {
            val executionContext = TestExecutionContext
            initialize(
                context = mock(),
                logging = mock(),
                executionContext = executionContext,
                databaseScope = DatabaseScope.of(executionContext, mock()),
                networkScope = mock(),
                okHttpClient = mock(),
                alarmsDatabaseRepository = mock(),
                sessionsDatabaseRepository = createSessionsDatabaseRepository(),
                highlightsDatabaseRepository = mock(),
                metaDatabaseRepository = mock(),
                scheduleNetworkRepository = mock(),
                sharedPreferencesRepository = mock(),
                sessionsTransformer = mock(),
            )
            return this
        }

    @Test
    fun `sessionsWithoutShifts emits once at initialization`() = runTest {
        testableAppRepository.sessionsWithoutShifts.test {
            val sessions1 = awaitItem()
            assertThat(sessions1).isNotEmpty()
            expectNoEvents()
        }
    }

    @Test
    fun `sessionsWithoutShifts emits once more when updateDisplayDayIndex is invoked`() = runTest {
        testableAppRepository.sessionsWithoutShifts.test {
            val sessions1 = awaitItem()
            assertThat(sessions1).isNotEmpty()
            testableAppRepository.updateDisplayDayIndex(1)
            val sessions2 = awaitItem()
            assertThat(sessions2).isNotEmpty()
            expectNoEvents()
        }
    }

    private fun createSessionsDatabaseRepository(): SessionsDatabaseRepository {
        val repository = mock<SessionsDatabaseRepository>()
        val session = SessionDatabaseModel("123")
        whenever(repository.querySessionsWithoutRoom(any()))
            .thenReturn(listOf(session))
        return repository
    }

}
