package info.metadude.android.eventfahrplan.engelsystem

import info.metadude.android.eventfahrplan.engelsystem.models.EngelsystemUri
import info.metadude.android.eventfahrplan.engelsystem.models.ShiftsResult
import info.metadude.android.eventfahrplan.engelsystem.utils.UriParser
import info.metadude.kotlin.library.engelsystem.ApiModule
import okhttp3.OkHttpClient
import ru.gildor.coroutines.retrofit.Result
import ru.gildor.coroutines.retrofit.awaitResult
import ru.gildor.coroutines.retrofit.getOrDefault
import java.io.IOException
import java.net.URISyntaxException

class EngelsystemNetworkRepository(

        private val uriParser: UriParser = UriParser()

) {

    suspend fun load(okHttpClient: OkHttpClient, url: String): ShiftsResult {
        val uri: EngelsystemUri
        try {
            uri = uriParser.parseUri(url)
        } catch (e: URISyntaxException) {
            return ShiftsResult.Exception(e)
        }
        val service = ApiModule.provideEngelsystemService(uri.baseUrl, okHttpClient)
        val call = service.getShifts(uri.pathPart, uri.apiKey)
        return when (val result = safeApiCall(
                call = { call.awaitResult() },
                errorMessage = "Error loading shifts from Engelsystem."
        )) {
            is Result.Ok -> ShiftsResult.Success(result.getOrDefault(emptyList()))
            is Result.Error -> ShiftsResult.Error(result.response.code(), result.exception.message())
            is Result.Exception -> ShiftsResult.Exception(result.exception)
        }
    }

    /**
     * Wrap a suspending API [call] in try/catch. In case an exception is thrown,
     * a [Result.Exception] is created based on the [errorMessage].
     */
    private suspend fun <T : Any> safeApiCall(call: suspend () -> Result<T>, errorMessage: String): Result<T> {
        return try {
            call()
        } catch (e: Exception) {
            // An exception was thrown when calling the API so we're converting this to an IOException
            Result.Exception(IOException(errorMessage, e))
        }
    }

}
