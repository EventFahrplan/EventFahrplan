package info.metadude.android.eventfahrplan.engelsystem

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import info.metadude.android.eventfahrplan.engelsystem.models.ShiftsResult
import info.metadude.kotlin.library.engelsystem.EngelsystemApi
import info.metadude.kotlin.library.engelsystem.EngelsystemService
import info.metadude.kotlin.library.engelsystem.adapters.ZonedDateTimeJsonAdapter
import info.metadude.kotlin.library.engelsystem.models.Shift
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.EOFException
import java.net.HttpURLConnection

class EngelsystemNetworkRepositoryTest {

    private companion object {
        val STARTS_AT: ZonedDateTime = ZonedDateTime.of(2019, 8, 21, 13, 0, 0, 0, ZoneOffset.ofHours(2))
        val ENDS_AT: ZonedDateTime = STARTS_AT.plusHours(2).plusMinutes(45)
        const val VALID_ONE_ITEM_SHIFTS_JSON = """
                [
                    {
                        "user_comment": "This is a very secret comment.",
                        "Name": "Kirmes",
                        "RID": 12,
                        "SID": 579,
                        "TID": 10,
                        "UID": 32,
                        "created_at_timestamp": 1565979486,
                        "created_by_user_id": 32,
                        "description": "Kirmes are fun.",
                        "edited_at_timestamp": 1567367095,
                        "edited_by_user_id": 32,
                        "end_date": "2019-08-21T15:45:00+02:00",
                        "event_timezone": "Europe/Berlin",
                        "freeloaded": 0,
                        "id": 37,
                        "shifttype_id": 6,
                        "shifttype_name": "Name of the shift type",
                        "shifttype_description": "# Description of the shift type as markdown\n",
                        "start_date": "2019-08-21T13:00:00+02:00",
                        "title": "Tag 1: Decorate fridge"
                    }
                ]
                """
        const val INVALID_ONE_ITEM_SHIFTS_JSON = """
                {
                    "unknown": "foobar"
                }
                """
        const val EMPTY_ARRAY_SHIFTS_JSON = "[]"
        const val EMPTY_STRING = ""
        val EXPECTED_ONE_ITEM_SHIFTS = listOf(Shift(
            userComment = "This is a very secret comment.",
            endsAtDate = ENDS_AT,
            locationDescription = "Kirmes are fun.",
            locationName = "Kirmes",
            sID = 579,
            startsAtDate = STARTS_AT,
            talkTitle = "Tag 1: Decorate fridge",
            timeZoneName = "Europe/Berlin",
            typeId = 6,
            typeName = "Name of the shift type",
            typeDescription = "# Description of the shift type as markdown\n",
        ))
        const val URL = "https://example.com/test/shifts-json-export/file.json?key=111111"

    }


    private val mockWebServer = MockWebServer()
    private val okHttpClient = mock<OkHttpClient>()
    private val repository = createRepository()

    @BeforeEach
    fun setUp() {
        mockWebServer.start()
    }

    @AfterEach
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `load returns exception when call responds with HTTP 200 and invalid JSON`() = runTest {
        val shiftsResult = repository.loadMockResponse(
            httpStatusCode = /* not relevant here */ HttpURLConnection.HTTP_OK,
            shiftsJson = INVALID_ONE_ITEM_SHIFTS_JSON
        )
        assertThat(shiftsResult).isInstanceOf(ShiftsResult.Exception::class.java)
        assertThat((shiftsResult as ShiftsResult.Exception).throwable).isInstanceOf(JsonDataException::class.java)
    }

    @Test
    fun `load returns exception when shifts JSON is empty`() = runTest {
        val shiftsResult = repository.loadMockResponse(
            httpStatusCode = /* not relevant here */ HttpURLConnection.HTTP_OK,
            shiftsJson = EMPTY_STRING
        )
        assertThat(shiftsResult).isInstanceOf(ShiftsResult.Exception::class.java)
        assertThat((shiftsResult as ShiftsResult.Exception).throwable).isInstanceOf(EOFException::class.java)
    }

    @Test
    fun `load returns success and empty list when call responds with HTTP 200 and empty array JSON`() =
        runTest {
            val shiftsResult = repository.loadMockResponse(
                httpStatusCode = HttpURLConnection.HTTP_OK,
                shiftsJson = EMPTY_ARRAY_SHIFTS_JSON
            )
            assertThat(shiftsResult).isEqualTo(ShiftsResult.Success(emptyList()))
        }

    @Test
    fun `load returns success when call responds with HTTP 200`() = runTest {
        val shiftsResult = repository.loadMockResponse(
            httpStatusCode = HttpURLConnection.HTTP_OK,
            shiftsJson = VALID_ONE_ITEM_SHIFTS_JSON
        )
        assertThat(shiftsResult).isEqualTo(ShiftsResult.Success(EXPECTED_ONE_ITEM_SHIFTS))
    }

    @Test
    fun `load returns error when call responds with HTTP 300`() = runTest {
        val shiftsResult = repository.loadMockResponse(
            httpStatusCode = HttpURLConnection.HTTP_MULT_CHOICE,
            shiftsJson = VALID_ONE_ITEM_SHIFTS_JSON
        )
        assertThat(shiftsResult).isEqualTo(ShiftsResult.Error(HttpURLConnection.HTTP_MULT_CHOICE, "Redirection"))
    }

    @Test
    fun `load returns error when call responds with HTTP 400`() = runTest {
        val shiftsResult = repository.loadMockResponse(
            httpStatusCode = HttpURLConnection.HTTP_BAD_REQUEST,
            shiftsJson = VALID_ONE_ITEM_SHIFTS_JSON
        )
        assertThat(shiftsResult).isEqualTo(ShiftsResult.Error(HttpURLConnection.HTTP_BAD_REQUEST, "Client Error"))
    }

    @Test
    fun `load returns error when call responds with HTTP 500`() = runTest {
        val shiftsResult = repository.loadMockResponse(
            httpStatusCode = HttpURLConnection.HTTP_INTERNAL_ERROR,
            shiftsJson = VALID_ONE_ITEM_SHIFTS_JSON
        )
        assertThat(shiftsResult).isEqualTo(ShiftsResult.Error(HttpURLConnection.HTTP_INTERNAL_ERROR, "Server Error"))
    }

    /**
     * Performs a HTTP request against a [mockWebServer] using the given [httpStatusCode] and [shiftsJson] for the response.
     */
    private suspend fun RealEngelsystemNetworkRepository.loadMockResponse(
        httpStatusCode: Int,
        shiftsJson: String
    ): ShiftsResult {
        val shiftsResponse = MockResponse()
            .addHeader("Content-Type", "application/json")
            .setResponseCode(httpStatusCode)
            .setBody(shiftsJson)
        mockWebServer.enqueue(shiftsResponse)
        return load(okHttpClient, URL)
    }

    private fun createRepository() = RealEngelsystemNetworkRepository(
        engelsystemApi = InterceptedServiceProvider(mockWebServer)
    )

    /**
     * Test-specific [EngelsystemApi] which injects the given [mockWebServer]
     * to return predefined responses.
     */
    private class InterceptedServiceProvider(private val mockWebServer: MockWebServer) : EngelsystemApi {

        override fun provideEngelsystemService(baseUrl: String, okHttpClient: OkHttpClient): EngelsystemService {
            val moshi = Moshi.Builder()
                .add(ZonedDateTime::class.java, ZonedDateTimeJsonAdapter())
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            return retrofit.create(EngelsystemService::class.java)
        }

    }


}
