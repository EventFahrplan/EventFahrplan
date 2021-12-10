package info.metadude.android.eventfahrplan.engelsystem.loading

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import info.metadude.android.eventfahrplan.engelsystem.loading.ShiftsLoading.awaitShiftsResult
import info.metadude.android.eventfahrplan.engelsystem.loading.ShiftsLoading.toShiftsResult
import info.metadude.android.eventfahrplan.engelsystem.models.ShiftsResult
import info.metadude.kotlin.library.engelsystem.EngelsystemService
import info.metadude.kotlin.library.engelsystem.adapters.ZonedDateTimeJsonAdapter
import info.metadude.kotlin.library.engelsystem.models.Shift
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.EOFException
import java.net.HttpURLConnection

class ShiftsLoadingTest {

    private companion object {
        val STARTS_AT: ZonedDateTime = ZonedDateTime.of(2019,8, 21, 13, 0, 0, 0, ZoneOffset.ofHours(2))
        val ENDS_AT: ZonedDateTime = STARTS_AT.plusHours(2).plusMinutes(45)
        const val VALID_ONE_ITEM_SHIFTS_JSON = """
                [
                    {
                        "Comment": "This is a very secret comment.",
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
                        "name": "Collect stickers",
                        "shifttype_id": 6,
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
        val ACTUAL_ONE_ITEM_SHIFTS = listOf(Shift(
                userComment = "This is a very secret comment.",
                endsAtDate = ENDS_AT,
                locationDescription = "Kirmes are fun.",
                locationName = "Kirmes",
                name = "Collect stickers",
                sID = 579,
                startsAtDate = STARTS_AT,
                talkTitle = "Tag 1: Decorate fridge",
                timeZoneName = "Europe/Berlin",
                typeId = 6
        ))
        val EXPECTED_ONE_ITEM_SHIFTS = listOf(Shift(
                userComment = "This is a very secret comment.",
                endsAtDate = ENDS_AT,
                locationDescription = "Kirmes are fun.",
                locationName = "Kirmes",
                name = "Collect stickers",
                sID = 579,
                startsAtDate = STARTS_AT,
                talkTitle = "Tag 1: Decorate fridge",
                timeZoneName = "Europe/Berlin",
                typeId = 6
        ))
    }

    private val mockWebServer = MockWebServer()

    @Before
    fun setUp() {
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `Converts a success response with null body into an ShiftsResult exception`() {
        val nullResponse: Response<List<Shift>> = Response.success(null)
        val shiftsResult = nullResponse.toShiftsResult()
        assertThat(shiftsResult).isEqualTo(ShiftsResult.Exception.MissingResponseSuccessBody)
    }

    @Test
    fun `Converts a success response with empty body into an ShiftsResult success with an empty list`() {
        val emptySuccessResponse = Response.success(emptyList<Shift>())
        val shiftsResult = emptySuccessResponse.toShiftsResult()
        assertThat(shiftsResult).isEqualTo(ShiftsResult.Success(emptyList()))
    }

    @Test
    fun `Converts a success response with one item shifts into an ShiftsResult success with an one item shifts list`() {
        val oneItemSuccessResponse = Response.success(ACTUAL_ONE_ITEM_SHIFTS)
        val shiftsResult = oneItemSuccessResponse.toShiftsResult()
        assertThat(shiftsResult).isEqualTo(ShiftsResult.Success(EXPECTED_ONE_ITEM_SHIFTS))
    }

    @Test
    fun `awaitShiftsResult returns exception when call responds with HTTP 200 and invalid JSON`() = runBlocking {
        val call = performHttpRequest(httpStatusCode = /* not relevant here */ HttpURLConnection.HTTP_OK, shiftsJson = INVALID_ONE_ITEM_SHIFTS_JSON)
        val shiftsResult = call.awaitShiftsResult()
        assertThat(shiftsResult).isInstanceOf(ShiftsResult.Exception::class.java)
        assertThat((shiftsResult as ShiftsResult.Exception).throwable).isInstanceOf(JsonDataException::class.java)
    }

    @Test
    fun `awaitShiftsResult returns exception when shifts JSON is empty`() = runBlocking {
        val call = performHttpRequest(httpStatusCode = /* not relevant here */ HttpURLConnection.HTTP_OK, shiftsJson = EMPTY_STRING)
        val shiftsResult = call.awaitShiftsResult()
        assertThat(shiftsResult).isInstanceOf(ShiftsResult.Exception::class.java)
        assertThat((shiftsResult as ShiftsResult.Exception).throwable).isInstanceOf(EOFException::class.java)
    }

    @Test
    fun `awaitShiftsResult returns success and empty list when call responds with HTTP 200 and empty array JSON`() = runBlocking {
        val call = performHttpRequest(HttpURLConnection.HTTP_OK, EMPTY_ARRAY_SHIFTS_JSON)
        val shiftsResult = call.awaitShiftsResult()
        assertThat(shiftsResult).isEqualTo(ShiftsResult.Success(emptyList()))
    }

    @Test
    fun `awaitShiftsResult returns success when call responds with HTTP 200`() = runBlocking {
        val call = performHttpRequest(HttpURLConnection.HTTP_OK, VALID_ONE_ITEM_SHIFTS_JSON)
        val shiftsResult = call.awaitShiftsResult()
        assertThat(shiftsResult).isEqualTo(ShiftsResult.Success(EXPECTED_ONE_ITEM_SHIFTS))
    }

    @Test
    fun `awaitShiftsResult returns error when call responds with HTTP 300`() = runBlocking {
        val call = performHttpRequest(HttpURLConnection.HTTP_MULT_CHOICE)
        val shiftsResult = call.awaitShiftsResult()
        assertThat(shiftsResult).isEqualTo(ShiftsResult.Error(HttpURLConnection.HTTP_MULT_CHOICE, "Redirection"))
    }

    @Test
    fun `awaitShiftsResult returns error when call responds with HTTP 400`() = runBlocking {
        val call = performHttpRequest(HttpURLConnection.HTTP_BAD_REQUEST)
        val shiftsResult = call.awaitShiftsResult()
        assertThat(shiftsResult).isEqualTo(ShiftsResult.Error(HttpURLConnection.HTTP_BAD_REQUEST, "Client Error"))
    }

    @Test
    fun `awaitShiftsResult returns error when call responds with HTTP 500`() = runBlocking {
        val call = performHttpRequest(HttpURLConnection.HTTP_INTERNAL_ERROR)
        val shiftsResult = call.awaitShiftsResult()
        assertThat(shiftsResult).isEqualTo(ShiftsResult.Error(HttpURLConnection.HTTP_INTERNAL_ERROR, "Server Error"))
    }

    /**
     * Performs a HTTP request against a [mockWebServer] using the given [httpStatusCode] and [shiftsJson] for the response.
     */
    private fun performHttpRequest(
            httpStatusCode: Int,
            shiftsJson: String = VALID_ONE_ITEM_SHIFTS_JSON
    ): Call<List<Shift>> {
        val shiftsResponse = MockResponse()
                .addHeader("Content-Type", "application/json")
                .setResponseCode(httpStatusCode)
                .setBody(shiftsJson)
        mockWebServer.enqueue(shiftsResponse)
        val service = createEngelsystemService()
        return service.getShifts(path = "/test/shifts-json-export", apiKey = "secret")
    }

    private fun createEngelsystemService(): EngelsystemService {
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
