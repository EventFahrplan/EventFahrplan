package nerd.tuxmobil.fahrplan.congress.repositories

import android.content.ContentValues
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.database.models.ColumnStatistic
import info.metadude.android.eventfahrplan.database.models.Session
import info.metadude.android.eventfahrplan.database.repositories.SessionsDatabaseRepository
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock

/**
 * Covers [AppRepository.scheduleStatistic].
 */
@ExtendWith(MainDispatcherTestExtension::class)
class AppRepositoryScheduleStatisticTest {

    private val sessionsDatabaseRepository = InMemorySessionDatabaseRepository()

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

    @Test
    fun `scheduleStatistic emits empty list by default`() = runTest {
        sessionsDatabaseRepository.clear()
        val expected = emptyList<ColumnStatistic>()
        testableAppRepository.scheduleStatistic.test {
            val actual = awaitItem()
            assertThat(actual).isEqualTo(expected)
        }
    }

    @Test
    fun `scheduleStatistic emits list of ColumnStatistic items`() = runTest {
        testableAppRepository.updateSessions(emptyList(), emptyList())
        val expected = listOf(
            ColumnStatistic("title", countNone = 100, countPresent = 0),
            ColumnStatistic("subtitle", countNone = 0, countPresent = 100),
        )
        testableAppRepository.scheduleStatistic.test {
            val actual = awaitItem()
            assertThat(actual).isEqualTo(expected)
        }
    }

}

private class InMemorySessionDatabaseRepository : SessionsDatabaseRepository {

    private var scheduleStatistic = listOf(
        ColumnStatistic("title", countNone = 100, countPresent = 0),
        ColumnStatistic("subtitle", countNone = 0, countPresent = 100),
    )

    fun clear() {
        scheduleStatistic = emptyList()
    }

    override fun queryScheduleStatistic() = scheduleStatistic

    override fun upsertSessions(
        contentValuesByGuid: List<Pair<String, ContentValues>>,
        toBeDeletedGuids: List<String>) {
        // hardcoded in class property
    }

    override fun insertGuid(guidContentValues: ContentValues) =
        throw NotImplementedError()

    override fun deleteGuidByNotificationId(notificationId: Int) =
        throw NotImplementedError()

    override fun querySessionByGuid(guid: String) =
        throw NotImplementedError()

    override fun querySessionsForDayIndexOrderedByDateUtc(dayIndex: Int) =
        throw NotImplementedError()

    override fun querySessionsOrderedByDateUtc(): List<Session> =
        throw NotImplementedError()

    override fun querySessionsWithoutRoom(roomName: String) =
        throw NotImplementedError()

    override fun querySessionsWithinRoom(roomName: String) =
        throw NotImplementedError()

}
