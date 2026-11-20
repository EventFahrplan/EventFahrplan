package nerd.tuxmobil.fahrplan.congress.repositories

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.database.models.Favorite
import info.metadude.android.eventfahrplan.database.repositories.FavoritesDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.SessionsDatabaseRepository
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.preferences.SettingsRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import info.metadude.android.eventfahrplan.database.models.Session as SessionDatabaseModel

/**
 * Covers [AppRepository.loadFilteredChangedSessions].
 */
class AppRepositoryFilteredChangedSessionsTest {

    private val databaseSessions = listOf(
        SessionDatabaseModel(sessionId = "unchanged-favorite"),
        SessionDatabaseModel(sessionId = "changed-favorite", changedTitle = true),
        SessionDatabaseModel(sessionId = "new", changedIsNew = true),
        SessionDatabaseModel(sessionId = "canceled", changedIsCanceled = true),
    )

    private val databaseFavorites = listOf(
        Favorite(sessionId = "unchanged-favorite", isFavorite = true),
        Favorite(sessionId = "changed-favorite", isFavorite = true),
    )

    private val settingsRepository = mock<SettingsRepository>()

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
                sessionsDatabaseRepository = mock<SessionsDatabaseRepository> {
                    on { querySessionsOrderedByDateUtc() } doReturn databaseSessions
                },
                favoritesDatabaseRepository = mock<FavoritesDatabaseRepository> {
                    on { query() } doReturn databaseFavorites
                },
                metaDatabaseRepository = mock(),
                scheduleNetworkRepository = mock(),
                sharedPreferencesRepository = mock(),
                settingsRepository = settingsRepository,
                sessionsTransformer = mock(),
            )
            return this
        }

    @Test
    fun `loadFilteredChangedSessions returns all changed sessions if favorites filter is disabled`() {
        whenever(settingsRepository.isScheduleChangesFavoritesFilterEnabled()).thenReturn(false)
        val sessionIds = testableAppRepository.loadFilteredChangedSessions().map { it.sessionId }
        assertThat(sessionIds).containsExactly("changed-favorite", "new", "canceled").inOrder()
    }

    @Test
    fun `loadFilteredChangedSessions returns changed favorite sessions only if favorites filter is enabled`() {
        whenever(settingsRepository.isScheduleChangesFavoritesFilterEnabled()).thenReturn(true)
        val sessionIds = testableAppRepository.loadFilteredChangedSessions().map { it.sessionId }
        assertThat(sessionIds).containsExactly("changed-favorite")
    }

}
