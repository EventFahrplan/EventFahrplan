package nerd.tuxmobil.fahrplan.congress.repositories

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestRule
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedNever
import info.metadude.android.eventfahrplan.commons.testing.verifyInvokedOnce
import info.metadude.android.eventfahrplan.database.repositories.AlarmsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.HighlightsDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.MetaDatabaseRepository
import info.metadude.android.eventfahrplan.database.repositories.SessionsDatabaseRepository
import info.metadude.android.eventfahrplan.network.models.HttpHeader
import info.metadude.android.eventfahrplan.network.repositories.ScheduleNetworkRepository
import kotlinx.coroutines.test.runTest
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.dataconverters.toAppFetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData
import nerd.tuxmobil.fahrplan.congress.net.FetchScheduleResult
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus
import nerd.tuxmobil.fahrplan.congress.net.ParseResult
import nerd.tuxmobil.fahrplan.congress.net.ParseScheduleResult
import nerd.tuxmobil.fahrplan.congress.preferences.SharedPreferencesRepository
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.FetchFailure
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.Fetching
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.InitialFetching
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.InitialParsing
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.ParseFailure
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.ParseSuccess
import nerd.tuxmobil.fahrplan.congress.repositories.LoadScheduleState.Parsing
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import info.metadude.android.eventfahrplan.database.models.Highlight as DatabaseHighlight
import info.metadude.android.eventfahrplan.database.models.Meta as DatabaseMeta
import info.metadude.android.eventfahrplan.database.models.Session as DatabaseSession
import info.metadude.android.eventfahrplan.network.fetching.FetchScheduleResult as NetworkFetchScheduleResult
import info.metadude.android.eventfahrplan.network.fetching.HttpStatus as NetworkHttpStatus
import info.metadude.android.eventfahrplan.network.models.Meta as NetworkMeta
import info.metadude.android.eventfahrplan.network.models.Session as NetworkSession
import nerd.tuxmobil.fahrplan.congress.models.Session as AppSession

private typealias OnFetchingDone = (fetchScheduleResult: FetchScheduleResult) -> Unit
private typealias OnParsingDone = (parseScheduleResult: ParseResult) -> Unit
private typealias OnFetchScheduleFinished = (fetchScheduleResult: NetworkFetchScheduleResult) -> Unit

/**
 * Covers [AppRepository.loadSchedule] and the private `parseSchedule` function.
 *
 * The following public properties are checked:
 * - [AppRepository.loadScheduleState]
 * - [AppRepository.starredSessions]
 * - [AppRepository.changedSessions]
 * - [AppRepository.selectedSession]
 * - [AppRepository.uncanceledSessionsForDayIndex]
 */
class AppRepositoryLoadAndParseScheduleTest {

    private companion object {
        const val HOST_NAME = "https://example.com"
        const val SCHEDULE_URL = "https://example.com/schedule.xml"
        const val EMPTY_ENGELSYSTEM_URL = ""
    }

    @get:Rule
    val mainDispatcherTestRule = MainDispatcherTestRule()

    private val alarmsDatabaseRepository = mock<AlarmsDatabaseRepository>()
    private val highlightsDatabaseRepository = mock<HighlightsDatabaseRepository>()
    private val sessionsDatabaseRepository = mock<SessionsDatabaseRepository>()
    private val metaDatabaseRepository = mock<MetaDatabaseRepository>()
    private val scheduleNetworkRepository = TestScheduleNetworkRepository()
    private val sharedPreferencesRepository = mock<SharedPreferencesRepository>()
    private val sessionsTransformer = mock<SessionsTransformer>()

    private val testableAppRepository: AppRepository
        get() = with(AppRepository) {
            initialize(
                context = mock(),
                logging = mock(),
                executionContext = TestExecutionContext,
                databaseScope = mock(),
                networkScope = mock(),
                okHttpClient = mock(),
                alarmsDatabaseRepository = alarmsDatabaseRepository,
                highlightsDatabaseRepository = highlightsDatabaseRepository,
                sessionsDatabaseRepository = sessionsDatabaseRepository,
                metaDatabaseRepository = metaDatabaseRepository,
                scheduleNetworkRepository = scheduleNetworkRepository,
                engelsystemNetworkRepository = mock(),
                sharedPreferencesRepository = sharedPreferencesRepository,
                sessionsTransformer = sessionsTransformer
            )
            return this
        }

    @Test
    fun `loadScheduleState emits InitialFetching when invoking loadSchedule for the first time`() =
        runTest {
            whenever(metaDatabaseRepository.query()) doReturn DatabaseMeta(numDays = 0)
            testableAppRepository.loadSchedule(isUserRequest = false)
            testableAppRepository.loadScheduleState.test {
                val actualState = awaitItem()
                assertThat(actualState).isEqualTo(InitialFetching)
            }
        }

    @Test
    fun `loadScheduleState emits Fetching when invoking loadSchedule once a schedule has been stored`() =
        runTest {
            whenever(metaDatabaseRepository.query()) doReturn DatabaseMeta(numDays = 1)
            testableAppRepository.loadSchedule(isUserRequest = false)
            testableAppRepository.loadScheduleState.test {
                val actualState = awaitItem()
                assertThat(actualState).isEqualTo(Fetching)
            }
        }

    @Test
    fun `loadScheduleState emits InitialParsing HTTP 200 when schedule has been loaded for the first time`() =
        runTest {
            whenever(metaDatabaseRepository.query()) doReturn DatabaseMeta(numDays = 0)
            val success = createFetchScheduleResult(NetworkHttpStatus.HTTP_OK)
            val onFetchingDone: OnFetchingDone = { result ->
                assertThat(result).isEqualTo(success.toAppFetchScheduleResult())
            }
            testableAppRepository.loadSchedule(isUserRequest = false, onFetchingDone)
            scheduleNetworkRepository.onFetchScheduleFinished(success)
            testableAppRepository.loadScheduleState.test {
                assertThat(awaitItem()).isEqualTo(InitialParsing)
            }
            verifyInvokedOnce(sharedPreferencesRepository).setScheduleLastFetchedAt(any())
            verifyInvokedOnce(metaDatabaseRepository).insert(any())
        }

    @Test
    fun `loadScheduleState emits InitialParsing HTTP 200 when schedule has been loaded and stored`() =
        runTest {
            whenever(metaDatabaseRepository.query()) doReturn DatabaseMeta(numDays = 1)
            val success = createFetchScheduleResult(NetworkHttpStatus.HTTP_OK)
            val onFetchingDone: OnFetchingDone = { result ->
                assertThat(result).isEqualTo(success.toAppFetchScheduleResult())
            }
            testableAppRepository.loadSchedule(isUserRequest = false, onFetchingDone)
            scheduleNetworkRepository.onFetchScheduleFinished(success)
            testableAppRepository.loadScheduleState.test {
                assertThat(awaitItem()).isEqualTo(Parsing)
            }
            verifyInvokedOnce(sharedPreferencesRepository).setScheduleLastFetchedAt(any())
            verifyInvokedOnce(metaDatabaseRepository).insert(any())
        }

    @Test
    fun `loadScheduleState emits FetchFailure HTTP 304 when schedule has not been modified`() =
        runTest {
            whenever(metaDatabaseRepository.query()) doReturn DatabaseMeta(numDays = 1)
            whenever(sharedPreferencesRepository.getEngelsystemShiftsUrl()) doReturn EMPTY_ENGELSYSTEM_URL // early exit to bypass here
            val notModified = createFetchScheduleResult(NetworkHttpStatus.HTTP_NOT_MODIFIED)
            val onFetchingDone: OnFetchingDone = { result ->
                assertThat(result).isEqualTo(notModified.toAppFetchScheduleResult())
            }
            testableAppRepository.loadSchedule(isUserRequest = false, onFetchingDone)
            scheduleNetworkRepository.onFetchScheduleFinished(notModified)
            testableAppRepository.loadScheduleState.test {
                val expected =
                    createFetchFailure(HttpStatus.HTTP_NOT_MODIFIED, isUserRequest = false)
                assertThat(awaitItem()).isEqualTo(expected)
            }
            verifyInvokedOnce(sharedPreferencesRepository).setScheduleLastFetchedAt(any())
            verifyInvokedNever(metaDatabaseRepository).insert(any())
        }

    @Test
    fun `loadScheduleState emits FetchFailure HTTP 404 when schedule cannot be loaded`() = runTest {
        whenever(metaDatabaseRepository.query()) doReturn DatabaseMeta(numDays = 1)
        val notFound = createFetchScheduleResult(NetworkHttpStatus.HTTP_NOT_FOUND)
        val onFetchingDone: OnFetchingDone = { result ->
            assertThat(result).isEqualTo(notFound.toAppFetchScheduleResult())
        }
        testableAppRepository.loadSchedule(isUserRequest = true, onFetchingDone)
        scheduleNetworkRepository.onFetchScheduleFinished(notFound)
        testableAppRepository.loadScheduleState.test {
            val expectedResult = createFetchFailure(HttpStatus.HTTP_NOT_FOUND, isUserRequest = true)
            assertThat(awaitItem()).isEqualTo(expectedResult)
        }
        verifyInvokedNever(sharedPreferencesRepository).setScheduleLastFetchedAt(any())
        verifyInvokedNever(metaDatabaseRepository).insert(any())
    }

    /**
     * Quickly passing through the loading part and only checking parsing part here.
     */
    @Test
    fun `loadScheduleState emits ParseSuccess when parsing finished successfully`() =
        runTest {
            whenever(metaDatabaseRepository.query()) doReturn DatabaseMeta(numDays = 1)
            val success = createFetchScheduleResult(NetworkHttpStatus.HTTP_OK)
            val onParsingDone: OnParsingDone = { result ->
                assertThat(result).isEqualTo(ParseScheduleResult(isSuccess = true, "1.0.0"))
            }
            testableAppRepository.loadSchedule(isUserRequest = false, onParsingDone = onParsingDone)
            scheduleNetworkRepository.onFetchScheduleFinished(success)

            // onUpdateSessions
            whenever(sessionsDatabaseRepository.querySessionsOrderedByDateUtc()) doReturn listOf(
                DatabaseSession(sessionId = "55", isHighlight = true, changedLanguage = true)
            )
            whenever(highlightsDatabaseRepository.query()) doReturn emptyList()
            whenever(alarmsDatabaseRepository.query()) doReturn emptyList()

            scheduleNetworkRepository.onUpdateSessions(emptyList())

            verifyInvokedOnce(sharedPreferencesRepository).setChangesSeen(any())
            verifyInvokedOnce(sessionsDatabaseRepository).updateSessions(any(), any())

            assertStarredSessionsProperty()
            assertChangedSessionsProperty()
            assertSelectedSessionProperty()
            assertUncanceledSessionsForDayIndexProperty()

            // onUpdateMeta
            scheduleNetworkRepository.onUpdateMeta(NetworkMeta())
            verify(metaDatabaseRepository, times(2)).insert(any())

            // onParsingDone
            whenever(sharedPreferencesRepository.getEngelsystemShiftsUrl()) doReturn EMPTY_ENGELSYSTEM_URL // early exit to bypass here
            scheduleNetworkRepository.onParsingDone(true, "1.0.0")
            testableAppRepository.loadScheduleState.test {
                assertThat(awaitItem()).isEqualTo(ParseSuccess)
            }
        }

    private suspend fun assertStarredSessionsProperty() {
        testableAppRepository.starredSessions.test {
            val session = AppSession("55").apply { highlight = true }
            assertThat(awaitItem()).isEqualTo(listOf(session))
        }
    }

    private suspend fun assertChangedSessionsProperty() {
        testableAppRepository.changedSessions.test {
            val session = AppSession("55").apply {
                highlight = true
                changedLanguage = true
            }
            assertThat(awaitItem()).isEqualTo(listOf(session))
        }
    }

    private suspend fun assertSelectedSessionProperty() {
        whenever(sessionsDatabaseRepository.querySessionBySessionId(any())) doReturn DatabaseSession(
            "23"
        )
        whenever(highlightsDatabaseRepository.queryBySessionId(any())) doReturn DatabaseHighlight(
            sessionId = 23,
            isHighlight = false
        )
        whenever(alarmsDatabaseRepository.query(anyString())) doReturn emptyList()
        whenever(sharedPreferencesRepository.getSelectedSessionId()) doReturn "23"
        testableAppRepository.selectedSession.test {
            assertThat(awaitItem()).isEqualTo(AppSession("23"))
        }
    }

    private suspend fun assertUncanceledSessionsForDayIndexProperty() {
        whenever(sessionsTransformer.transformSessions(any(), any())) doReturn ScheduleData(
            dayIndex = 0,
            roomDataList = emptyList()
        )
        testableAppRepository.uncanceledSessionsForDayIndex.test {
            assertThat(awaitItem()).isEqualTo(
                ScheduleData(
                    dayIndex = 0,
                    roomDataList = emptyList()
                )
            )
        }
    }

    @Test
    fun `loadScheduleState emits ParseFailure when parsing finished with an error`() =
        runTest {
            whenever(metaDatabaseRepository.query()) doReturn DatabaseMeta(numDays = 1)
            val success = createFetchScheduleResult(NetworkHttpStatus.HTTP_OK)
            val onParsingDone: OnParsingDone = { result ->
                assertThat(result).isEqualTo(ParseScheduleResult(isSuccess = false, "1.0.0"))
            }
            testableAppRepository.loadSchedule(isUserRequest = false, onParsingDone = onParsingDone)
            scheduleNetworkRepository.onFetchScheduleFinished(success)

            // onParsingDone
            whenever(sharedPreferencesRepository.getEngelsystemShiftsUrl()) doReturn EMPTY_ENGELSYSTEM_URL // early exit to bypass here
            scheduleNetworkRepository.onParsingDone(false, "1.0.0")
            testableAppRepository.loadScheduleState.test {
                assertThat(awaitItem()).isEqualTo(ParseFailure(ParseScheduleResult(false, "1.0.0")))
            }
        }

    private fun createFetchScheduleResult(httpStatus: NetworkHttpStatus) =
        NetworkFetchScheduleResult(
            httpStatus = httpStatus,
            scheduleXml = "some fahrplan xml",
            httpHeader = HttpHeader("a1b2bc3"),
            hostName = HOST_NAME
        )

    private fun createFetchFailure(httpStatus: HttpStatus, isUserRequest: Boolean) =
        FetchFailure(
            httpStatus,
            hostName = HOST_NAME,
            exceptionMessage = "",
            isUserRequest = isUserRequest
        )

    private fun AppRepository.loadSchedule(
        isUserRequest: Boolean,
        onFetchingDone: OnFetchingDone = {},
        onParsingDone: OnParsingDone = {},
    ) =
        loadSchedule(SCHEDULE_URL, isUserRequest, onFetchingDone, onParsingDone, mock())

    private class TestScheduleNetworkRepository : ScheduleNetworkRepository {

        lateinit var onFetchScheduleFinished: OnFetchScheduleFinished
            private set

        lateinit var onUpdateSessions: (sessions: List<NetworkSession>) -> Unit
            private set

        lateinit var onUpdateMeta: (meta: NetworkMeta) -> Unit
            private set

        lateinit var onParsingDone: (result: Boolean, version: String) -> Unit
            private set

        override fun fetchSchedule(
            okHttpClient: OkHttpClient,
            url: String,
            httpHeader: HttpHeader,
            onFetchScheduleFinished: OnFetchScheduleFinished
        ) {
            this.onFetchScheduleFinished = onFetchScheduleFinished
        }

        override fun parseSchedule(
            scheduleXml: String,
            httpHeader: HttpHeader,
            onUpdateSessions: (sessions: List<NetworkSession>) -> Unit,
            onUpdateMeta: (meta: NetworkMeta) -> Unit,
            onParsingDone: (result: Boolean, version: String) -> Unit
        ) {
            this.onUpdateSessions = onUpdateSessions
            this.onUpdateMeta = onUpdateMeta
            this.onParsingDone = onParsingDone
        }

    }

}
