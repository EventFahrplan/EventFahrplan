package nerd.tuxmobil.fahrplan.congress.repositories

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestRule
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import info.metadude.android.eventfahrplan.database.repositories.SessionsDatabaseRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionsDatabaseModel
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import kotlin.time.ExperimentalTime

/**
 * Test class to deal with sessions which interact with the [SessionsDatabaseRepository].
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class AppRepositorySessionsTest {

    @get:Rule
    val mainDispatcherTestRule = MainDispatcherTestRule()

    private val sessionsDatabaseRepository = mock<SessionsDatabaseRepository>()

    private val testableAppRepository: AppRepository
        get() = with(AppRepository) {
            initialize(
                    context = mock(),
                    logging = mock(),
                    executionContext = TestExecutionContext,
                    databaseScope = mock(),
                    networkScope = mock(),
                    alarmsDatabaseRepository = mock(),
                    highlightsDatabaseRepository = mock(),
                    sessionsDatabaseRepository = sessionsDatabaseRepository,
                    metaDatabaseRepository = mock(),
                    scheduleNetworkRepository = mock(),
                    engelsystemNetworkRepository = mock(),
                    sharedPreferencesRepository = mock()
            )
            return this
        }

    companion object {

        private val SESSION_1001 = createSession("1001").apply {
            changedIsCanceled = false
            changedTitle = false
            changedIsNew = false
        }

        private val SESSION_1002 = createSession("1002").apply {
            changedIsCanceled = true
            changedTitle = false
            changedIsNew = false
        }

        private val SESSION_1003 = createSession("1003").apply {
            changedIsCanceled = false
            changedTitle = true
            changedIsNew = false
        }

        private val SESSION_1004 = createSession("1004").apply {
            changedIsCanceled = false
            changedTitle = false
            changedIsNew = true
        }

        private val SESSION_1005 = createSession("1005").apply {
            changedIsCanceled = true
            changedTitle = true
            changedIsNew = true
        }

        private val SESSION_2001 = createSession("2001").apply {
            highlight = false
            changedIsCanceled = false
        }

        private val SESSION_2002 = createSession("2002").apply {
            highlight = true
            changedIsCanceled = false
        }

        private val SESSION_2003 = createSession("2003").apply {
            highlight = true
            changedIsCanceled = true
        }

        private val SESSION_2004 = createSession("2004").apply {
            highlight = false
            changedIsCanceled = true
        }

        private val SESSION_3001 = createSession("3001").apply {
            changedIsCanceled = false
        }

        private val SESSION_3002 = createSession("3002").apply {
            changedIsCanceled = true
        }

        private fun createSession(sessionId: String) = Session(sessionId).apply {
            url = "" // only initialized for toSessionsDatabaseModel()
        }

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
    fun `loadStarredSessions passes through an empty list`() = mainDispatcherTestRule.runBlockingTest {
        whenever(sessionsDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn emptyList()
        testableAppRepository.starredSessions.test {
            assertThat(awaitItem()).isEqualTo(emptyList<Session>())
        }
        verifyInvokedOnce(sessionsDatabaseRepository).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadStarredSessions filters out sessions which are not starred`() = mainDispatcherTestRule.runBlockingTest {
        val sessions = listOf(SESSION_2001, SESSION_2002, SESSION_2003, SESSION_2004)
        whenever(sessionsDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn sessions.toSessionsDatabaseModel()
        testableAppRepository.starredSessions.test {
            assertThat(awaitItem()).containsExactly(SESSION_2002)
        }
        verifyInvokedOnce(sessionsDatabaseRepository).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadUncanceledSessionsForDayIndex passes through an empty list`() {
        whenever(sessionsDatabaseRepository.querySessionsForDayIndexOrderedByDateUtc(anyInt())) doReturn emptyList()
        assertThat(testableAppRepository.loadUncanceledSessionsForDayIndex(0)).isEmpty()
        verifyInvokedOnce(sessionsDatabaseRepository).querySessionsForDayIndexOrderedByDateUtc(anyInt())
    }

    @Test
    fun `loadUncanceledSessionsForDayIndex filters out sessions which are canceled`() {
        val sessions = listOf(SESSION_3001, SESSION_3002)
        whenever(sessionsDatabaseRepository.querySessionsForDayIndexOrderedByDateUtc(anyInt())) doReturn sessions.toSessionsDatabaseModel()
        val uncanceledSessions = testableAppRepository.loadUncanceledSessionsForDayIndex(0)
        assertThat(uncanceledSessions).containsExactly(SESSION_3001)
        verifyInvokedOnce(sessionsDatabaseRepository).querySessionsForDayIndexOrderedByDateUtc(anyInt())
    }

    @Test
    fun `loadEarliestSession fails when no session is present`() {
        whenever(sessionsDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn emptyList()
        try {
            testableAppRepository.loadEarliestSession()
            fail()
        } catch (e: NoSuchElementException) {
            assertThat(e.message).isEqualTo("List is empty.")
        }
        verifyInvokedOnce(sessionsDatabaseRepository).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadEarliestSession returns the first session of the first day`() {
        val sessions = listOf(SESSION_1005, SESSION_1001)
        whenever(sessionsDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn sessions.toSessionsDatabaseModel()
        assertThat(testableAppRepository.loadEarliestSession()).isEqualTo(SESSION_1005)
        verifyInvokedOnce(sessionsDatabaseRepository).querySessionsOrderedByDateUtc()
    }

}
