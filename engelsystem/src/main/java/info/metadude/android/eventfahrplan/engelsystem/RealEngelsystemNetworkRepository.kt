package info.metadude.android.eventfahrplan.engelsystem

import info.metadude.android.eventfahrplan.engelsystem.models.ShiftsResult
import info.metadude.android.eventfahrplan.engelsystem.utils.UriParser
import info.metadude.kotlin.library.engelsystem.ApiModule
import info.metadude.kotlin.library.engelsystem.EngelsystemApi
import okhttp3.OkHttpClient
import retrofit2.HttpException

class RealEngelsystemNetworkRepository(

    private val uriParser: UriParser = UriParser(),
    private val engelsystemApi: EngelsystemApi = ApiModule

) : EngelsystemNetworkRepository {

    override suspend fun load(okHttpClient: OkHttpClient, url: String) = try {
        val uri = uriParser.parseUri(url)
        val service = engelsystemApi.provideEngelsystemService(uri.baseUrl, okHttpClient)
        val shifts = service.getShifts(uri.pathPart, uri.apiKey)
        ShiftsResult.Success(shifts)
    } catch (e: HttpException) {
        ShiftsResult.Error(e.code(), e.message())
    } catch (e: Exception) {
        ShiftsResult.Exception(e)
    }

}
