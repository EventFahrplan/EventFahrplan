package info.metadude.android.eventfahrplan.engelsystem

import info.metadude.kotlin.library.engelsystem.ApiModule
import info.metadude.kotlin.library.engelsystem.EngelsystemService
import okhttp3.OkHttpClient

/**
 * Indirection provider to allow replacing the service in test scenarios.
 * The default implementation is used at runtime.
 */
interface EngelsystemServiceProvider {

    companion object {
        fun getNewInstance() = object : EngelsystemServiceProvider {}
    }

    fun getService(baseUrl: String, okHttpClient: OkHttpClient): EngelsystemService {
        return ApiModule.provideEngelsystemService(baseUrl, okHttpClient)
    }

}
