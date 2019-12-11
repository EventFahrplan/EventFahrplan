package nerd.tuxmobil.fahrplan.congress.net

import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.Response
import java.io.IOException

internal class UserAgentInterceptor(

        private val userAgent: String

) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Chain): Response {
        val originalRequest = chain.request()
        val requestWithUserAgent = originalRequest.newBuilder()
                .header("User-Agent", userAgent)
                .build()
        return chain.proceed(requestWithUserAgent)
    }

}
