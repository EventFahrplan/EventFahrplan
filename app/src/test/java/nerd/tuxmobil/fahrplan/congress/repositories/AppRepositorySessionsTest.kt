package nerd.tuxmobil.fahrplan.congress.repositories

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import info.metadude.android.eventfahrplan.database.repositories.SessionsDatabaseRepository
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionAppModel
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import info.metadude.android.eventfahrplan.database.models.Session as SessionDatabaseModel
import nerd.tuxmobil.fahrplan.congress.models.Session as SessionAppModel

/**
 * Test class to deal with sessions which interact with the [SessionsDatabaseRepository].
 */
@ExtendWith(MainDispatcherTestExtension::class)
class AppRepositorySessionsTest {

    private val sessionsDatabaseRepository = mock<SessionsDatabaseRepository>()

    private val testableAppRepository: AppRepository
        get() = with(AppRepository) {
            initialize(
                    context = mock(),
                    logging = mock(),
                    executionContext = TestExecutionContext,
                    databaseScope = mock(),
                    networkScope = mock(),
                    okHttpClient = mock(),
                    alarmsDatabaseRepository = mock(),
                    highlightsDatabaseRepository = mock(),
                    sessionsDatabaseRepository = sessionsDatabaseRepository,
                    metaDatabaseRepository = mock(),
                    scheduleNetworkRepository = mock(),
                    engelsystemRepository = mock(),
                    sharedPreferencesRepository = mock(),
                    settingsRepository = mock(),
                    sessionsTransformer = mock()
            )
            return this
        }

    companion object {

        private val SESSION_1001 = SessionDatabaseModel(
            sessionId = "1001",
            changedIsCanceled = false,
            changedTitle = false,
            changedIsNew = false,
        )

        private val SESSION_1002 = SessionDatabaseModel(
            sessionId = "1002",
            changedIsCanceled = true,
            changedTitle = false,
            changedIsNew = false,
        )

        private val SESSION_1003 = SessionDatabaseModel(
            sessionId = "1003",
            changedIsCanceled = false,
            changedTitle = true,
            changedIsNew = false,
        )

        private val SESSION_1004 = SessionDatabaseModel(
            sessionId = "1004",
            changedIsCanceled = false,
            changedTitle = false,
            changedIsNew = true,
        )

        private val SESSION_1005 = SessionDatabaseModel(
            sessionId = "1005",
            changedIsCanceled = true,
            changedTitle = true,
            changedIsNew = true,
        )

        private val SESSION_2001 = SessionDatabaseModel(
            sessionId = "2001",
            isHighlight = false,
            changedIsCanceled = false,
        )

        private val SESSION_2002 = SessionDatabaseModel(
            sessionId = "2002",
            isHighlight = true,
            changedIsCanceled = false,
        )

        private val SESSION_2003 = SessionDatabaseModel(
            sessionId = "2003",
            isHighlight = true,
            changedIsCanceled = true,
        )

        private val SESSION_2004 = SessionDatabaseModel(
            sessionId = "2004",
            isHighlight = false,
            changedIsCanceled = true,
        )

        private val SESSION_3001 = SessionDatabaseModel(
            sessionId = "3001",
            changedIsCanceled = false,
        )

        private val SESSION_3002 = SessionDatabaseModel(
            sessionId = "3002",
            changedIsCanceled = true,
        )

    }

    @Test
    fun `loadChangedSessions passes through an empty list`() {
        whenever(sessionsDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn emptyList()
        assertThat(testableAppRepository.loadChangedSessions()).isEmpty()
        verifyInvokedOnce(sessionsDatabaseRepository).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadChangedSessions filters out sessions which are not changed`() {
        val sessions = listOf(SESSION_1001, SESSION_1002, SESSION_1003, SESSION_1004, SESSION_1005)
        whenever(sessionsDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn sessions
        val changedSessions = testableAppRepository.loadChangedSessions()
        assertThat(changedSessions).containsExactly(SESSION_1002, SESSION_1003, SESSION_1004, SESSION_1005)
        verifyInvokedOnce(sessionsDatabaseRepository).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadStarredSessions passes through an empty list`() = runTest {
        whenever(sessionsDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn emptyList()
        testableAppRepository.starredSessions.test {
            assertThat(awaitItem()).isEqualTo(emptyList<SessionAppModel>())
        }
        verifyInvokedOnce(sessionsDatabaseRepository).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadStarredSessions filters out sessions which are not starred`() = runTest {
        val sessions = listOf(SESSION_2001, SESSION_2002, SESSION_2003, SESSION_2004)
        whenever(sessionsDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn sessions
        testableAppRepository.starredSessions.test {
            assertThat(awaitItem()).containsExactly(SESSION_2002.toSessionAppModel())
        }
        verifyInvokedOnce(sessionsDatabaseRepository).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadUncanceledSessionsForDayIndex passes through an empty list`() {
        whenever(sessionsDatabaseRepository.querySessionsForDayIndexOrderedByDateUtc(any())) doReturn emptyList()
        assertThat(testableAppRepository.loadUncanceledSessionsForDayIndex(0)).isEmpty()
        verifyInvokedOnce(sessionsDatabaseRepository).querySessionsForDayIndexOrderedByDateUtc(any())
    }

    @Test
    fun `loadUncanceledSessionsForDayIndex filters out sessions which are canceled`() {
        val sessions = listOf(SESSION_3001, SESSION_3002)
        whenever(sessionsDatabaseRepository.querySessionsForDayIndexOrderedByDateUtc(any())) doReturn sessions
        val uncanceledSessions = testableAppRepository.loadUncanceledSessionsForDayIndex(0)
        assertThat(uncanceledSessions).containsExactly(SESSION_3001)
        verifyInvokedOnce(sessionsDatabaseRepository).querySessionsForDayIndexOrderedByDateUtc(any())
    }

}
