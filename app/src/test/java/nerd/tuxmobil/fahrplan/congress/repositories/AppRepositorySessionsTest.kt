package nerd.tuxmobil.fahrplan.congress.repositories

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import info.metadude.android.eventfahrplan.database.repositories.SessionsDatabaseRepository
import nerd.tuxmobil.fahrplan.congress.dataconverters.toSessionsDatabaseModel
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt

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

        private val SESSION_1001 = Session("1001").apply {
            changedIsCanceled = false
            changedTitle = false
            changedIsNew = false
            url = "" // only initialized for toSessionsDatabaseModel()
        }

        private val SESSION_1002 = Session("1002").apply {
            changedIsCanceled = true
            changedTitle = false
            changedIsNew = false
            url = "" // only initialized for toSessionsDatabaseModel()
        }

        private val SESSION_1003 = Session("1003").apply {
            changedIsCanceled = false
            changedTitle = true
            changedIsNew = false
            url = "" // only initialized for toSessionsDatabaseModel()
        }

        private val SESSION_1004 = Session("1004").apply {
            changedIsCanceled = false
            changedTitle = false
            changedIsNew = true
            url = "" // only initialized for toSessionsDatabaseModel()
        }

        private val SESSION_1005 = Session("1005").apply {
            changedIsCanceled = true
            changedTitle = true
            changedIsNew = true
            url = "" // only initialized for toSessionsDatabaseModel()
        }

        private val SESSION_2001 = Session("2001").apply {
            highlight = false
            changedIsCanceled = false
            url = "" // only initialized for toSessionsDatabaseModel()
        }

        private val SESSION_2002 = Session("2002").apply {
            highlight = true
            changedIsCanceled = false
            url = "" // only initialized for toSessionsDatabaseModel()
        }

        private val SESSION_2003 = Session("2003").apply {
            highlight = true
            changedIsCanceled = true
            url = "" // only initialized for toSessionsDatabaseModel()
        }

        private val SESSION_2004 = Session("2004").apply {
            highlight = false
            changedIsCanceled = true
            url = "" // only initialized for toSessionsDatabaseModel()
        }

        private val SESSION_3001 = Session("3001").apply {
            changedIsCanceled = false
            url = "" // only initialized for toSessionsDatabaseModel()
        }

        private val SESSION_3002 = Session("3002").apply {
            changedIsCanceled = true
            url = "" // only initialized for toSessionsDatabaseModel()
        }

    }

    @Test
    fun `loadChangedSessions passes through an empty list`() {
        whenever(sessionsDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn emptyList()
        assertThat(testableAppRepository.loadChangedSessions()).isEmpty()
        verify(sessionsDatabaseRepository, once()).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadChangedSessions filters out sessions which are not changed`() {
        val sessions = listOf(SESSION_1001, SESSION_1002, SESSION_1003, SESSION_1004, SESSION_1005)
        whenever(sessionsDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn sessions.toSessionsDatabaseModel()
        val changedSessions = testableAppRepository.loadChangedSessions()
        assertThat(changedSessions).containsExactly(SESSION_1002, SESSION_1003, SESSION_1004, SESSION_1005)
        verify(sessionsDatabaseRepository, once()).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadStarredSessions passes through an empty list`() {
        whenever(sessionsDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn emptyList()
        assertThat(testableAppRepository.loadStarredSessions()).isEmpty()
        verify(sessionsDatabaseRepository, once()).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadStarredSessions filters out sessions which are not starred`() {
        val sessions = listOf(SESSION_2001, SESSION_2002, SESSION_2003, SESSION_2004)
        whenever(sessionsDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn sessions.toSessionsDatabaseModel()
        val starredSessions = testableAppRepository.loadStarredSessions()
        assertThat(starredSessions).containsExactly(SESSION_2002)
        verify(sessionsDatabaseRepository, once()).querySessionsOrderedByDateUtc()
    }

    @Test
    fun `loadUncanceledSessionsForDayIndex passes through an empty list`() {
        whenever(sessionsDatabaseRepository.querySessionsForDayIndexOrderedByDateUtc(anyInt())) doReturn emptyList()
        assertThat(testableAppRepository.loadUncanceledSessionsForDayIndex(0)).isEmpty()
        verify(sessionsDatabaseRepository, once()).querySessionsForDayIndexOrderedByDateUtc(anyInt())
    }

    @Test
    fun `loadUncanceledSessionsForDayIndex filters out sessions which are canceled`() {
        val sessions = listOf(SESSION_3001, SESSION_3002)
        whenever(sessionsDatabaseRepository.querySessionsForDayIndexOrderedByDateUtc(anyInt())) doReturn sessions.toSessionsDatabaseModel()
        val uncanceledSessions = testableAppRepository.loadUncanceledSessionsForDayIndex(0)
        assertThat(uncanceledSessions).containsExactly(SESSION_3001)
        verify(sessionsDatabaseRepository, once()).querySessionsForDayIndexOrderedByDateUtc(anyInt())
    }

    private fun once() = times(1)

}
