package nerd.tuxmobil.fahrplan.congress.repositories

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import info.metadude.android.eventfahrplan.database.repositories.AlarmsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.HighlightsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.SessionsDatabaseRepository
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionAppModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionsAppModel
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import info.metadude.android.eventfahrplan.database.models.Session as SessionDatabaseModel

/**
 * Test class to deal with sessions which interact with the [SessionsDatabaseRepository].
 */
class AppRepositorySessionsTest {

    private val sessionsDatabaseRepository = mock<SessionsDatabaseRepository>()

    private val testableAppRepository: AppRepository
        get() = with(AppRepository) {
            initialize(
                    context = mock(),
                    logging = mock(),
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

        private val SESSION_1001 = SessionDatabaseModel(
                sessionId = "1001",
                guid = "1001",
                changedIsCanceled = false,
                changedTitle = false,
                changedIsNew = false
        )

        private val SESSION_1002 = SessionDatabaseModel(
                sessionId = "1002",
                guid = "1002",
                changedIsCanceled = true,
                changedTitle = false,
                changedIsNew = false
        )

        private val SESSION_1003 = SessionDatabaseModel(
                sessionId = "1003",
                guid = "1003",
                changedIsCanceled = false,
                changedTitle = true,
                changedIsNew = false
        )

        private val SESSION_1004 = SessionDatabaseModel(
                sessionId = "1004",
                guid = "1004",
                changedIsCanceled = false,
                changedTitle = false,
                changedIsNew = true
        )

        private val SESSION_1005 = SessionDatabaseModel(
                sessionId = "1005",
                guid = "1005",
                changedIsCanceled = true,
                changedTitle = true,
                changedIsNew = true,
        )

        private val SESSION_2001 = SessionDatabaseModel(
                sessionId = "2001",
                guid = "2001",
                isHighlight = false,
                changedIsCanceled = false,
        )

        private val SESSION_2002 = SessionDatabaseModel(
                sessionId = "2002",
                guid = "2002",
                isHighlight = true,
                changedIsCanceled = false,
        )

        private val SESSION_2003 = SessionDatabaseModel(
                sessionId = "2003",
                guid = "2003",
                isHighlight = true,
                changedIsCanceled = true,
        )

        private val SESSION_2004 = SessionDatabaseModel(
                sessionId = "2004",
                guid = "2004",
                isHighlight = false,
                changedIsCanceled = true,
        )

        private val SESSION_3001 = SessionDatabaseModel(
                sessionId = "3001",
                guid = "3001",
                changedIsCanceled = false,
        )

        private val SESSION_3002 = SessionDatabaseModel(
                sessionId = "3002",
                guid = "3002",
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
        val expectedSessions = listOf(SESSION_1002, SESSION_1003, SESSION_1004, SESSION_1005).toSessionsAppModel()
        assertThat(changedSessions).containsExactlyElementsIn(expectedSessions)
        verifyInvokedOnce(sessionsDatabaseRepository).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadStarredSessions passes through an empty list`() {
        whenever(sessionsDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn emptyList()
        assertThat(testableAppRepository.loadStarredSessions()).isEmpty()
        verifyInvokedOnce(sessionsDatabaseRepository).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadStarredSessions filters out sessions which are not starred`() {
        val sessions = listOf(SESSION_2001, SESSION_2002, SESSION_2003, SESSION_2004)
        whenever(sessionsDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn sessions
        val starredSessions = testableAppRepository.loadStarredSessions()
        assertThat(starredSessions).containsExactly(SESSION_2002.toSessionAppModel())
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
        whenever(sessionsDatabaseRepository.querySessionsForDayIndexOrderedByDateUtc(anyInt())) doReturn sessions
        val uncanceledSessions = testableAppRepository.loadUncanceledSessionsForDayIndex(0)
        assertThat(uncanceledSessions).containsExactly(SESSION_3001.toSessionAppModel())
        verifyInvokedOnce(sessionsDatabaseRepository).querySessionsForDayIndexOrderedByDateUtc(anyInt())
    }

}
