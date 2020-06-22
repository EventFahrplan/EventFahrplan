package info.metadude.android.eventfahrplan.engelsystem

import info.metadude.android.eventfahrplan.engelsystem.loading.ShiftsLoading.awaitShiftsResult
import info.metadude.android.eventfahrplan.engelsystem.models.EngelsystemUri
import info.metadude.android.eventfahrplan.engelsystem.models.ShiftsResult
import info.metadude.android.eventfahrplan.engelsystem.utils.UriParser
import info.metadude.kotlin.library.engelsystem.ApiModule
import okhttp3.OkHttpClient
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
        return call.awaitShiftsResult()
    }

}
