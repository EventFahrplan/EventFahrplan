package nerd.tuxmobil.fahrplan.congress.repositories

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import info.metadude.android.eventfahrplan.database.repositories.SessionsDatabaseRepository
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionsDatabaseModel
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

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
                    engelsystemNetworkRepository = mock(),
                    sharedPreferencesRepository = mock(),
                    sessionsTransformer = mock()
            )
            return this
        }

    companion object {

        private val SESSION_1001 = Session(
            sessionId = "1001",
            changedIsCanceled = false,
            changedTitle = false,
            changedIsNew = false,
        )

        private val SESSION_1002 = Session(
            sessionId = "1002",
            changedIsCanceled = true,
            changedTitle = false,
            changedIsNew = false,
        )

        private val SESSION_1003 = Session(
            sessionId = "1003",
            changedIsCanceled = false,
            changedTitle = true,
            changedIsNew = false,
        )

        private val SESSION_1004 = Session(
            sessionId = "1004",
            changedIsCanceled = false,
            changedTitle = false,
            changedIsNew = true,
        )

        private val SESSION_1005 = Session(
            sessionId = "1005",
            changedIsCanceled = true,
            changedTitle = true,
            changedIsNew = true,
        )

        private val SESSION_2001 = Session(
            sessionId = "2001",
            highlight = false,
            changedIsCanceled = false,
        )

        private val SESSION_2002 = Session(
            sessionId = "2002",
            highlight = true,
            changedIsCanceled = false,
        )

        private val SESSION_2003 = Session(
            sessionId = "2003",
            highlight = true,
            changedIsCanceled = true,
        )

        private val SESSION_2004 = Session(
            sessionId = "2004",
            highlight = false,
            changedIsCanceled = true,
        )

        private val SESSION_3001 = Session(
            sessionId = "3001",
            changedIsCanceled = false,
        )

        private val SESSION_3002 = Session(
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
        whenever(sessionsDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn sessions.toSessionsDatabaseModel()
        val changedSessions = testableAppRepository.loadChangedSessions()
        assertThat(changedSessions).containsExactly(SESSION_1002, SESSION_1003, SESSION_1004, SESSION_1005)
        verifyInvokedOnce(sessionsDatabaseRepository).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadStarredSessions passes through an empty list`() = runTest {
        whenever(sessionsDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn emptyList()
        testableAppRepository.starredSessions.test {
            assertThat(awaitItem()).isEqualTo(emptyList<Session>())
        }
        verifyInvokedOnce(sessionsDatabaseRepository).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadStarredSessions filters out sessions which are not starred`() = runTest {
        val sessions = listOf(SESSION_2001, SESSION_2002, SESSION_2003, SESSION_2004)
        whenever(sessionsDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn sessions.toSessionsDatabaseModel()
        testableAppRepository.starredSessions.test {
            assertThat(awaitItem()).containsExactly(SESSION_2002)
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
        whenever(sessionsDatabaseRepository.querySessionsForDayIndexOrderedByDateUtc(any())) doReturn sessions.toSessionsDatabaseModel()
        val uncanceledSessions = testableAppRepository.loadUncanceledSessionsForDayIndex(0)
        assertThat(uncanceledSessions).containsExactly(SESSION_3001)
        verifyInvokedOnce(sessionsDatabaseRepository).querySessionsForDayIndexOrderedByDateUtc(any())
    }

}
