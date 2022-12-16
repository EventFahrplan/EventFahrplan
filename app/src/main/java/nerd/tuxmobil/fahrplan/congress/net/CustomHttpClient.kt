package nerd.tuxmobil.fahrplan.congress.net

import nerd.tuxmobil.fahrplan.congress.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object CustomHttpClient {

    fun createHttpClient(): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()

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

        return clientBuilder.build()
    }

}
