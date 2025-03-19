package nerd.tuxmobil.fahrplan.congress.repositories

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.models.NextFetch
import nerd.tuxmobil.fahrplan.congress.preferences.SharedPreferencesRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Covers [AppRepository.scheduleNextFetch].
 */
@ExtendWith(MainDispatcherTestExtension::class)
class AppRepositoryScheduleNextFetchTest {

    private val sharedPreferencesRepository = mock<SharedPreferencesRepository>()

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
                sessionsDatabaseRepository = mock(),
                scheduleNetworkRepository = mock(),
                engelsystemNetworkRepository = mock(),
                sharedPreferencesRepository = sharedPreferencesRepository,
                sessionsTransformer = mock(),
            )
            return this
        }

    @Test
    fun `scheduleNextFetch emits zero schedule next fetch by default`() = runTest {
        whenever(sharedPreferencesRepository.getScheduleNextFetchAt()).thenReturn(0L)
        testableAppRepository.scheduleNextFetch.test {
            val actual = awaitItem()
            assertThat(actual).isEqualTo(NextFetch(Moment.ofEpochMilli(0), Duration.ofMilliseconds(0)))
        }
    }

    @Test
    fun `scheduleNextFetch emits the schedule next fetch`() = runTest {
        val nextFetchAt = Moment.ofEpochMilli(1633046400000)
        val interval = Duration.ofMilliseconds(3000)
        whenever(sharedPreferencesRepository.getScheduleNextFetchAt()).thenReturn(nextFetchAt.toMilliseconds())
        whenever(sharedPreferencesRepository.getScheduleNextFetchInterval()).thenReturn(interval.toPartialMilliseconds().toLong())
        testableAppRepository.updateScheduleNextFetch(NextFetch(nextFetchAt, interval))

        val expectedNextFetchAt = Moment.ofEpochMilli(1633046400000)
        val expectedInterval = Duration.ofMilliseconds(3000)
        val expected = NextFetch(expectedNextFetchAt, expectedInterval)
        testableAppRepository.scheduleNextFetch.test {
            val actual = awaitItem()
            assertThat(actual).isEqualTo(expected)
        }
    }

}
