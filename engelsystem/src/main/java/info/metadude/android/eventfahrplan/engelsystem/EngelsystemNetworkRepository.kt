package info.metadude.android.eventfahrplan.engelsystem

import info.metadude.android.eventfahrplan.engelsystem.loading.ShiftsLoading.awaitShiftsResult
import info.metadude.android.eventfahrplan.engelsystem.models.ShiftsResult
import info.metadude.android.eventfahrplan.engelsystem.utils.UriParser
import info.metadude.kotlin.library.engelsystem.ApiModule
import okhttp3.OkHttpClient
import java.net.URISyntaxException

class EngelsystemNetworkRepository(

        private val uriParser: UriParser = UriParser()

) {

    suspend fun load(okHttpClient: OkHttpClient, url: String) = try {
        val uri = uriParser.parseUri(url)
        val service = ApiModule.provideEngelsystemService(uri.baseUrl, okHttpClient)
        val call = service.getShifts(uri.pathPart, uri.apiKey)
        call.awaitShiftsResult()
    } catch (e: URISyntaxException) {
        ShiftsResult.Exception(e)
    } catch (e: IllegalArgumentException) {
        ShiftsResult.Exception(e)
    }

}
