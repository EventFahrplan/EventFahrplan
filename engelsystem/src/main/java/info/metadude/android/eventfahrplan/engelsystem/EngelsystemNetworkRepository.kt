package info.metadude.android.eventfahrplan.engelsystem

import info.metadude.android.eventfahrplan.engelsystem.models.ShiftsResult
import okhttp3.OkHttpClient

interface EngelsystemNetworkRepository {

    suspend fun load(okHttpClient: OkHttpClient, url: String): ShiftsResult

}
