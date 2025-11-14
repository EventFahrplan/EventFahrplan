package nerd.tuxmobil.fahrplan.congress.repositories

import android.content.ContentValues
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.NUM_DAYS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_ETAG
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_GENERATOR_NAME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_GENERATOR_VERSION
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_LAST_MODIFIED
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SUBTITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.TIME_ZONE_NAME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.VERSION
import info.metadude.android.eventfahrplan.database.repositories.MetaDatabaseRepository
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.threeten.bp.ZoneId
import info.metadude.android.eventfahrplan.database.models.HttpHeader as HttpHeaderDatabaseModel
import info.metadude.android.eventfahrplan.database.models.Meta as MetaDatabaseModel
import info.metadude.android.eventfahrplan.database.models.ScheduleGenerator as ScheduleGeneratorDatabaseModel
import nerd.tuxmobil.fahrplan.congress.models.HttpHeader as HttpHeaderAppModel
import nerd.tuxmobil.fahrplan.congress.models.Meta as MetaAppModel
import nerd.tuxmobil.fahrplan.congress.models.ScheduleGenerator as ScheduleGeneratorAppModel

/**
 * Covers [AppRepository.meta] and [AppRepository.updateMeta].
 */
@ExtendWith(MainDispatcherTestExtension::class)
class AppRepositoryMetaTest {

    private val metaDatabaseRepository = InMemoryMetaDatabaseRepository()

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
                sessionsDatabaseRepository = mock(),
                metaDatabaseRepository = metaDatabaseRepository,
                scheduleNetworkRepository = mock(),
                engelsystemRepository = mock(),
                sharedPreferencesRepository = mock(),
                settingsRepository = mock(),
                sessionsTransformer = mock()
            )
            return this
        }

    @Test
    fun `meta emits default Meta model`() = runTest {
        val expected = MetaAppModel(version = "0.0.0")
        testableAppRepository.meta.test {
            val actual = awaitItem()
            assertThat(actual).isEqualTo(expected)
        }
    }

    @Test
    fun `meta emits empty Meta model`() = runTest {
        testableAppRepository.updateMeta(MetaDatabaseModel(
            scheduleGenerator = ScheduleGeneratorDatabaseModel(name = "", version = "")),
        )
        val expected = MetaAppModel(
            scheduleGenerator = ScheduleGeneratorAppModel(name = "", version = ""),
        )
        testableAppRepository.meta.test {
            val actual = awaitItem()
            assertThat(actual).isEqualTo(expected)
        }
    }

    @Test
    fun `meta emits custom Meta model`() = runTest {
        testableAppRepository.updateMeta(MetaDatabaseModel(
            numDays = 4,
            version = "1.2.3",
            timeZoneName = "Europe/Berlin",
            title = "37C3",
            subtitle = "Unlocked",
            httpHeader = HttpHeaderDatabaseModel(eTag = "abc", lastModified = "9000"),
            scheduleGenerator = ScheduleGeneratorDatabaseModel(name = "pretalx", version = "2024.1.0"),
        ))
        val expected = MetaAppModel(
            numDays = 4,
            version = "1.2.3",
            timeZoneId = ZoneId.of("Europe/Berlin"),
            title = "37C3",
            subtitle = "Unlocked",
            httpHeader = HttpHeaderAppModel(eTag = "abc", lastModified = "9000"),
            scheduleGenerator = ScheduleGeneratorAppModel(name = "pretalx", version = "2024.1.0"),
        )
        testableAppRepository.meta.test {
            val actual = awaitItem()
            assertThat(actual).isEqualTo(expected)
        }
    }

}

private class InMemoryMetaDatabaseRepository : MetaDatabaseRepository {

    private var meta = MetaDatabaseModel(version = "0.0.0")

    override fun insert(values: ContentValues): Long {
        meta = values.toMeta()
        return 0
    }

    override fun query(): MetaDatabaseModel {
        return meta
    }

}

private fun ContentValues.toMeta() = MetaDatabaseModel(
    numDays = get(NUM_DAYS) as Int,
    version = get(VERSION) as String,
    timeZoneName = get(TIME_ZONE_NAME) as String?,
    title = get(TITLE) as String,
    subtitle = get(SUBTITLE) as String,
    httpHeader = HttpHeaderDatabaseModel(
        eTag = get(SCHEDULE_ETAG) as String,
        lastModified = get(SCHEDULE_LAST_MODIFIED) as String,
    ),
    scheduleGenerator = ScheduleGeneratorDatabaseModel(
        name = get(SCHEDULE_GENERATOR_NAME) as String,
        version = get(SCHEDULE_GENERATOR_VERSION) as String,
    ),
)
