package nerd.tuxmobil.fahrplan.congress.datasources

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import info.metadude.android.eventfahrplan.network.fetching.HttpStatus.HTTP_NOT_FOUND
import info.metadude.android.eventfahrplan.network.fetching.HttpStatus.HTTP_OK
import info.metadude.android.eventfahrplan.network.repositories.ScheduleNetworkRepository
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.dataconverters.toAppFetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.dataconverters.toMetaDatabaseModel
import nerd.tuxmobil.fahrplan.congress.dataconverters.toMetaNetworkModel
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus.HTTP_NOT_MODIFIED
import nerd.tuxmobil.fahrplan.congress.net.ParseResult
import nerd.tuxmobil.fahrplan.congress.net.ParseScheduleResult
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.FetchFailure
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.Fetching
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.InitialFetching
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.InitialParsing
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.ParseFailure
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.ParseSuccess
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.Parsing
import nerd.tuxmobil.fahrplan.congress.validation.MetaValidation.validate
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import info.metadude.android.eventfahrplan.database.models.Meta as MetaDatabaseModel
import info.metadude.android.eventfahrplan.network.fetching.FetchScheduleResult as NetworkFetchScheduleResult
import info.metadude.android.eventfahrplan.network.fetching.HttpStatus as NetworkHttpStatus
import info.metadude.android.eventfahrplan.network.models.HttpHeader as HttpHeaderNetworkModel
import info.metadude.android.eventfahrplan.network.models.Meta as MetaNetworkModel
import info.metadude.android.eventfahrplan.network.models.Session as SessionNetworkModel
import nerd.tuxmobil.fahrplan.congress.models.HttpHeader as HttpHeaderAppModel
import nerd.tuxmobil.fahrplan.congress.models.Meta as MetaAppModel

private typealias OnFetchingDone = (fetchScheduleResult: FetchScheduleResult) -> Unit
private typealias OnParsingDone = (parseScheduleResult: ParseResult) -> Unit
private typealias OnFetchScheduleFinished = (fetchScheduleResult: NetworkFetchScheduleResult) -> Unit

@ExtendWith(MainDispatcherTestExtension::class)
class ScheduleV1XmlSourceTest {

    private companion object {
        const val HOST_NAME = "https://example.com"
        const val SCHEDULE_URL = "$HOST_NAME/schedule.xml"
    }

    private val okHttpClient = mock<OkHttpClient>()
    private val scheduleNetworkRepository = TestScheduleNetworkRepository()
    private val scheduleSourceRepository = mock<ScheduleSourceRepository> {
        on { readScheduleUrl() } doReturn SCHEDULE_URL
    }

    @Test
    fun `loadScheduleState emits InitialFetching and forwards url and request header when invoking loadSchedule for the first time`() =
        runTest {
            val source = createTestableSource()
            val meta = MetaAppModel(
                httpHeader = HttpHeaderAppModel(eTag = "old-etag", lastModified = "old-last-modified"),
                numDays = 0,
            )
            val onFetchingDone: OnFetchingDone = {}
            val onParsingDone: OnParsingDone = {}

            source.loadSchedule(
                meta = meta,
                isUserRequest = false,
                onFetchingDone = onFetchingDone,
                onParsingDone = onParsingDone,
                onLoadShifts = {},
            )

            source.loadScheduleState.test {
                assertThat(awaitItem()).isEqualTo(InitialFetching)
            }
            verify(scheduleSourceRepository).readScheduleUrl()
            assertThat(scheduleNetworkRepository.url).isEqualTo(SCHEDULE_URL)
            assertThat(scheduleNetworkRepository.httpHeader).isEqualTo(meta.toMetaNetworkModel().httpHeader)
            assertThat(scheduleNetworkRepository.okHttpClient).isSameInstanceAs(okHttpClient)
        }

    @Test
    fun `loadScheduleState emits Fetching when invoking loadSchedule once a schedule has been stored`() =
        runTest {
            val source = createTestableSource()
            val onFetchingDone: OnFetchingDone = {}
            val onParsingDone: OnParsingDone = {}

            source.loadSchedule(
                meta = MetaAppModel(numDays = 1),
                isUserRequest = false,
                onFetchingDone = onFetchingDone,
                onParsingDone = onParsingDone,
                onLoadShifts = {},
            )

            source.loadScheduleState.test {
                assertThat(awaitItem()).isEqualTo(Fetching)
            }
        }

    @Test
    fun `loadScheduleState emits InitialParsing when schedule has been loaded for the first time`() =
        runTest {
            val source = createTestableSource()
            val success = createFetchScheduleResult(HTTP_OK)
            val onFetchingDone: OnFetchingDone = { result ->
                assertThat(result).isEqualTo(success.toAppFetchScheduleResult())
            }
            val onParsingDone: OnParsingDone = {}

            source.loadSchedule(
                meta = MetaAppModel(numDays = 0),
                isUserRequest = false,
                onFetchingDone = onFetchingDone,
                onParsingDone = onParsingDone,
                onLoadShifts = {},
            )
            scheduleNetworkRepository.onFetchScheduleFinished(success)

            source.loadScheduleState.test {
                assertThat(awaitItem()).isEqualTo(InitialParsing)
            }
            verifyInvokedOnce(scheduleSourceRepository).updateScheduleLastFetchedAt()
            verifyInvokedOnce(scheduleSourceRepository).updateMeta(any())
        }

    @Test
    fun `loadScheduleState emits Parsing when schedule has been loaded and stored`() = runTest {
        val source = createTestableSource()
        val success = createFetchScheduleResult(HTTP_OK)
        val onFetchingDone: OnFetchingDone = { result ->
            assertThat(result).isEqualTo(success.toAppFetchScheduleResult())
        }
        val onParsingDone: OnParsingDone = {}

        source.loadSchedule(
            meta = MetaAppModel(numDays = 1),
            isUserRequest = false,
            onFetchingDone = onFetchingDone,
            onParsingDone = onParsingDone,
            onLoadShifts = {},
        )
        scheduleNetworkRepository.onFetchScheduleFinished(success)

        source.loadScheduleState.test {
            assertThat(awaitItem()).isEqualTo(Parsing)
        }
        verifyInvokedOnce(scheduleSourceRepository).updateScheduleLastFetchedAt()
        verifyInvokedOnce(scheduleSourceRepository).updateMeta(any())
    }

    @Test
    fun `loadScheduleState emits FetchFailure HTTP 304 when schedule has not been modified`() =
        runTest {
            val source = createTestableSource()
            val notModified = createFetchScheduleResult(NetworkHttpStatus.HTTP_NOT_MODIFIED)
            val onFetchingDone: OnFetchingDone = { result ->
                assertThat(result).isEqualTo(notModified.toAppFetchScheduleResult())
            }
            val onParsingDone: OnParsingDone = {}
            var loadShiftsInvocations = 0

            source.loadSchedule(
                meta = MetaAppModel(numDays = 1),
                isUserRequest = false,
                onFetchingDone = onFetchingDone,
                onParsingDone = onParsingDone,
                onLoadShifts = { loadShiftsInvocations++ },
            )
            scheduleNetworkRepository.onFetchScheduleFinished(notModified)

            source.loadScheduleState.test {
                assertThat(awaitItem()).isEqualTo(
                    createFetchFailure(HTTP_NOT_MODIFIED, isUserRequest = false)
                )
            }
            assertThat(loadShiftsInvocations).isEqualTo(1)
            verifyInvokedOnce(scheduleSourceRepository).updateScheduleLastFetchedAt()
            verifyInvokedNever(scheduleSourceRepository).updateMeta(any())
        }

    @Test
    fun `loadScheduleState emits FetchFailure HTTP 404 when schedule cannot be loaded`() = runTest {
        val source = createTestableSource()
        val notFound = createFetchScheduleResult(HTTP_NOT_FOUND)
        val onFetchingDone: OnFetchingDone = { result ->
            assertThat(result).isEqualTo(notFound.toAppFetchScheduleResult())
        }
        val onParsingDone: OnParsingDone = {}
        var loadShiftsInvocations = 0

        source.loadSchedule(
            meta = MetaAppModel(numDays = 1),
            isUserRequest = true,
            onFetchingDone = onFetchingDone,
            onParsingDone = onParsingDone,
            onLoadShifts = { loadShiftsInvocations++ },
        )
        scheduleNetworkRepository.onFetchScheduleFinished(notFound)

        source.loadScheduleState.test {
            assertThat(awaitItem()).isEqualTo(
                createFetchFailure(HttpStatus.HTTP_NOT_FOUND, isUserRequest = true)
            )
        }
        assertThat(loadShiftsInvocations).isEqualTo(0)
        verifyInvokedNever(scheduleSourceRepository).updateScheduleLastFetchedAt()
        verifyInvokedNever(scheduleSourceRepository).updateMeta(any())
    }

    @Test
    fun `loadScheduleState emits ParseSuccess when parsing finished successfully`() = runTest {
        val source = createTestableSource()
        val meta = MetaAppModel(numDays = 1)
        val success = createFetchScheduleResult(HTTP_OK)
        val onFetchingDone: OnFetchingDone = {}
        val onParsingDone: OnParsingDone = { result ->
            assertThat(result).isEqualTo(ParseScheduleResult(isSuccess = true, version = "1.0.0"))
        }
        var loadShiftsInvocations = 0

        source.loadSchedule(
            meta = meta,
            isUserRequest = false,
            onFetchingDone = onFetchingDone,
            onParsingDone = onParsingDone,
            onLoadShifts = { loadShiftsInvocations++ },
        )
        scheduleNetworkRepository.onFetchScheduleFinished(success)
        scheduleNetworkRepository.onUpdateSessions(emptyList())
        scheduleNetworkRepository.onUpdateMeta(MetaNetworkModel())
        scheduleNetworkRepository.onParsingDone(true, "1.0.0")

        source.loadScheduleState.test {
            assertThat(awaitItem()).isEqualTo(ParseSuccess)
        }
        verify(scheduleSourceRepository).updateSessions(emptyList())
        assertThat(loadShiftsInvocations).isEqualTo(1)

        val updateMetaCaptor = argumentCaptor<MetaDatabaseModel>()
        verify(scheduleSourceRepository, times(2)).updateMeta(updateMetaCaptor.capture())
        assertThat(updateMetaCaptor.firstValue).isEqualTo(
            meta.toMetaNetworkModel()
                .copy(httpHeader = success.httpHeader)
                .validate()
                .toMetaDatabaseModel()
        )
        assertThat(updateMetaCaptor.secondValue).isEqualTo(MetaNetworkModel().validate().toMetaDatabaseModel())
    }

    @Test
    fun `loadScheduleState emits ParseFailure and resets HTTP header when parsing finished with an error`() =
        runTest {
            val source = createTestableSource()
            val meta = MetaAppModel(
                httpHeader = HttpHeaderAppModel(eTag = "old-etag", lastModified = "old-last-modified"),
                numDays = 1,
            )
            val success = createFetchScheduleResult(HTTP_OK)
            val onFetchingDone: OnFetchingDone = {}
            val onParsingDone: OnParsingDone = { result ->
                assertThat(result).isEqualTo(ParseScheduleResult(isSuccess = false, version = "1.0.0"))
            }
            var loadShiftsInvocations = 0

            source.loadSchedule(
                meta = meta,
                isUserRequest = false,
                onFetchingDone = onFetchingDone,
                onParsingDone = onParsingDone,
                onLoadShifts = { loadShiftsInvocations++ },
            )
            scheduleNetworkRepository.onFetchScheduleFinished(success)
            scheduleNetworkRepository.onParsingDone(false, "1.0.0")

            source.loadScheduleState.test {
                assertThat(awaitItem()).isEqualTo(ParseFailure(ParseScheduleResult(false, "1.0.0")))
            }
            assertThat(loadShiftsInvocations).isEqualTo(1)

            val updateMetaCaptor = argumentCaptor<MetaDatabaseModel>()
            verify(scheduleSourceRepository, times(2)).updateMeta(updateMetaCaptor.capture())
            assertThat(updateMetaCaptor.firstValue).isEqualTo(
                meta.toMetaNetworkModel()
                    .copy(httpHeader = success.httpHeader)
                    .validate()
                    .toMetaDatabaseModel()
            )
            assertThat(updateMetaCaptor.secondValue).isEqualTo(
                meta.toMetaNetworkModel()
                    .copy(httpHeader = HttpHeaderNetworkModel(eTag = "", lastModified = ""))
                    .toMetaDatabaseModel()
            )
        }

    @Test
    fun `loadScheduleState emits ParseFailure when initial parsing finished with an error`() =
        runTest {
            val source = createTestableSource()
            val success = createFetchScheduleResult(HTTP_OK)
            val onFetchingDone: OnFetchingDone = {}
            val onParsingDone: OnParsingDone = { result ->
                assertThat(result).isEqualTo(ParseScheduleResult(isSuccess = false, version = "1.0.0"))
            }

            source.loadSchedule(
                meta = MetaAppModel(numDays = 0),
                isUserRequest = false,
                onFetchingDone = onFetchingDone,
                onParsingDone = onParsingDone,
                onLoadShifts = {},
            )
            scheduleNetworkRepository.onFetchScheduleFinished(success)
            scheduleNetworkRepository.onParsingDone(false, "1.0.0")

            source.loadScheduleState.test {
                assertThat(awaitItem()).isEqualTo(ParseFailure(ParseScheduleResult(false, "1.0.0")))
            }
            verify(scheduleSourceRepository, times(2)).updateMeta(any())
        }

    private fun createTestableSource() = ScheduleV1XmlSource(
        okHttpClient = okHttpClient,
        scheduleNetworkRepository = scheduleNetworkRepository,
        scheduleSourceRepository = scheduleSourceRepository,
    )

    private fun createFetchScheduleResult(httpStatus: NetworkHttpStatus) =
        NetworkFetchScheduleResult(
            httpStatus = httpStatus,
            scheduleXml = "some fahrplan xml",
            httpHeader = HttpHeaderNetworkModel(eTag = "a1b2c3", lastModified = "2023-12-31T23:59:59+01:00"),
            hostName = HOST_NAME,
        )

    private fun createFetchFailure(httpStatus: HttpStatus, isUserRequest: Boolean) = FetchFailure(
        httpStatus = httpStatus,
        hostName = HOST_NAME,
        exceptionMessage = "",
        isUserRequest = isUserRequest,
    )

    private class TestScheduleNetworkRepository : ScheduleNetworkRepository {

        lateinit var okHttpClient: OkHttpClient
            private set

        lateinit var url: String
            private set

        lateinit var httpHeader: HttpHeaderNetworkModel
            private set

        lateinit var onFetchScheduleFinished: OnFetchScheduleFinished
            private set

        lateinit var onUpdateSessions: (sessions: List<SessionNetworkModel>) -> Unit
            private set

        lateinit var onUpdateMeta: (meta: MetaNetworkModel) -> Unit
            private set

        lateinit var onParsingDone: (result: Boolean, version: String) -> Unit
            private set

        override fun fetchSchedule(
            okHttpClient: OkHttpClient,
            url: String,
            httpHeader: HttpHeaderNetworkModel,
            onFetchScheduleFinished: OnFetchScheduleFinished,
        ) {
            this.okHttpClient = okHttpClient
            this.url = url
            this.httpHeader = httpHeader
            this.onFetchScheduleFinished = onFetchScheduleFinished
        }

        override fun parseSchedule(
            scheduleXml: String,
            httpHeader: HttpHeaderNetworkModel,
            onUpdateSessions: (sessions: List<SessionNetworkModel>) -> Unit,
            onUpdateMeta: (meta: MetaNetworkModel) -> Unit,
            onParsingDone: (isSuccess: Boolean, version: String) -> Unit,
        ) {
            this.onUpdateSessions = onUpdateSessions
            this.onUpdateMeta = onUpdateMeta
            this.onParsingDone = onParsingDone
        }
    }
}

