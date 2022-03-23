package info.metadude.android.eventfahrplan.engelsystem

import info.metadude.android.eventfahrplan.engelsystem.models.ShiftsResult
import info.metadude.android.eventfahrplan.engelsystem.utils.UriParser
import okhttp3.OkHttpClient
import retrofit2.HttpException

class EngelsystemNetworkRepository(

    private val uriParser: UriParser = UriParser(),
    private val serviceProvider: EngelsystemServiceProvider = EngelsystemServiceProvider.getNewInstance()

) {

    suspend fun load(okHttpClient: OkHttpClient, url: String) = try {
        val uri = uriParser.parseUri(url)
        val service = serviceProvider.getService(uri.baseUrl, okHttpClient)
        val shifts = service.getShifts(uri.pathPart, uri.apiKey)
        ShiftsResult.Success(shifts)
    } catch (e: HttpException) {
        ShiftsResult.Error(e.code(), e.message())
    } catch (e: Exception) {
        ShiftsResult.Exception(e)
    }

}
