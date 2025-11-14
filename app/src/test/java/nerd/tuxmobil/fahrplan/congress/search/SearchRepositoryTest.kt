package nerd.tuxmobil.fahrplan.congress.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.preferences.SharedPreferencesRepository
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Covers [AppRepository.searchHistory].
 */
@ExtendWith(MainDispatcherTestExtension::class)
class SearchRepositoryTest {

    private val sharedPreferencesRepository = mock<SharedPreferencesRepository>()

    private val testableSearchRepository: SearchRepository
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
                sessionsDatabaseRepository = mock(),
                metaDatabaseRepository = mock(),
                scheduleNetworkRepository = mock(),
                engelsystemRepository = mock(),
                sharedPreferencesRepository = sharedPreferencesRepository,
                settingsRepository = mock(),
                sessionsTransformer = mock()
            )
            return this
        }

    @Test
    fun `searchHistory emits empty list by default`() = runTest {
        testableSearchRepository.searchHistory.test {
            assertThat(awaitItem()).isEqualTo(emptyList<List<String>>())
        }
    }

    @Test
    fun `searchHistory emits list of history items`() = runTest {
        val actual = listOf("a", "b", "c")
        whenever(sharedPreferencesRepository.getSearchHistory()) doReturn actual
        testableSearchRepository.updateSearchHistory(actual)
        testableSearchRepository.searchHistory.test {
            assertThat(awaitItem()).isEqualTo(listOf("a", "b", "c"))
        }
    }

}

