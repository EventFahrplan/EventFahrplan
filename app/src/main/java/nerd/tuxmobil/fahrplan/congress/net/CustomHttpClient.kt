package nerd.tuxmobil.fahrplan.congress.net

import android.content.Context
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import okhttp3.Cache
import okhttp3.CompressionInterceptor
import okhttp3.Gzip
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.zstd.Zstd

object CustomHttpClient {

    private const val CACHE_MAX_SIZE_BYTES = 10 * 1024 * 1024L // 10 MB

    fun createHttpClient(context: Context): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()

        val cache = Cache(context.cacheDir, CACHE_MAX_SIZE_BYTES)
        clientBuilder.cache(cache)

        val userAgentInterceptor = UserAgentInterceptor(
            userAgent = "${BuildConfig.APPLICATION_ID}, ${BuildConfig.VERSION_NAME}"
        )
        clientBuilder.addNetworkInterceptor(userAgentInterceptor)

        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.HEADERS
            }
            clientBuilder.addNetworkInterceptor(loggingInterceptor)
        }

        clientBuilder.addInterceptor(CompressionInterceptor(Zstd, Gzip))

        return clientBuilder.build()
    }

}
