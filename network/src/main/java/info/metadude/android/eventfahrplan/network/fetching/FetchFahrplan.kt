package info.metadude.android.eventfahrplan.network.fetching

import android.net.Uri
import android.os.AsyncTask
import android.os.Build

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.UnknownServiceException

import javax.net.ssl.SSLException
import javax.net.ssl.SSLHandshakeException

import info.metadude.android.eventfahrplan.commons.logging.Logging

import okhttp3.OkHttpClient
import okhttp3.Request

internal class FetchFahrplan(private val logging: Logging) {

    private lateinit var task: FetchFahrplanTask
    private lateinit var onFetchScheduleResult: (fetchScheduleResult: FetchScheduleResult) -> Unit

    fun fetch(okHttpClient: OkHttpClient, url: String, eTag: String) {
        task = FetchFahrplanTask(okHttpClient, logging, onFetchScheduleResult)
        task.execute(url, eTag)
    }

    fun cancel() {
        if (::task.isInitialized) {
            task.cancel(true)
        }
    }

    fun setListener(listener: (fetchScheduleResult: FetchScheduleResult) -> Unit) {
        onFetchScheduleResult = listener
        if (::task.isInitialized) {
            task.setListener(listener)
        }
    }
}

internal class FetchFahrplanTask(

    private val okHttpClient: OkHttpClient,
    private val logging: Logging,
    private var onFetchScheduleResult: (fetchScheduleResult: FetchScheduleResult) -> Unit

) : AsyncTask<String, Void, HttpStatus>() {

    private companion object {
        const val EMPTY_RESPONSE_STRING = ""
        const val LOG_TAG = "FetchFahrplan"
        const val HTTP_HEADER_NAME_ETAG = "ETag"
        const val HTTP_HEADER_NAME_IF_NONE_MATCH = "If-None-Match"
    }

    private var responseStr = EMPTY_RESPONSE_STRING
    private var eTagStr = ""
    private var completed = false
    private lateinit var status: HttpStatus
    private var host = ""
    private var exceptionMessage = ""

    fun setListener(listener: (fetchScheduleResult: FetchScheduleResult) -> Unit) {
        onFetchScheduleResult = listener
        if (completed) {
            notifyActivity()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg args: String?): HttpStatus {
        val url = args[0]!!
        val eTag = args[1]!!
        host = Uri.parse(url).host ?: throw NullPointerException("Host is null for url = '$url'")
        return fetch(url, eTag)
    }

    @Deprecated("Deprecated in Java")
    override fun onCancelled() {
        logging.d(LOG_TAG, "Fetch cancelled")
    }

    @Deprecated("Deprecated in Java")
    override fun onPostExecute(status: HttpStatus) {
        completed = true
        this.status = status
        notifyActivity()
    }

    private fun notifyActivity() {
        if (status == HttpStatus.HTTP_OK) {
            logging.d(LOG_TAG, "Fetch done successfully")
            onFetchScheduleResult(
                FetchScheduleResult(status, responseStr, eTagStr, host, exceptionMessage)
            )
        } else {
            logging.d(LOG_TAG, "Fetch failed")
            onFetchScheduleResult(
                FetchScheduleResult(
                    status, EMPTY_RESPONSE_STRING, eTagStr, host, exceptionMessage
                )
            )
        }
        completed = false // notify only once
    }

    private fun fetch(url: String, eTag: String): HttpStatus {
        logging.d(LOG_TAG, url)
        logging.d(LOG_TAG, "$HTTP_HEADER_NAME_ETAG: '$eTag'")
        val requestBuilder = Request.Builder().apply {
            url(url)
            if (eTag.isNotEmpty()) {
                addHeader(HTTP_HEADER_NAME_IF_NONE_MATCH, eTag)
            }
        }

        val response = try {
            okHttpClient.newCall(requestBuilder.build()).execute()
        } catch (e: SSLException) {
            setExceptionMessage(e)
            customizeExceptionMessage(e)
            e.printStackTrace()
            return HttpStatus.HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE
        } catch (e: SocketTimeoutException) {
            return HttpStatus.HTTP_CONNECT_TIMEOUT
        } catch (e: UnknownHostException) {
            e.printStackTrace()
            return HttpStatus.HTTP_DNS_FAILURE
        } catch (e: UnknownServiceException) {
            e.printStackTrace()
            return HttpStatus.HTTP_CLEARTEXT_NOT_PERMITTED
        } catch (e: IOException) {
            e.printStackTrace()
            return HttpStatus.HTTP_COULD_NOT_CONNECT
        }
        val statusCode = response.code

        if (statusCode == 304) {
            return HttpStatus.HTTP_NOT_MODIFIED
        }

        if (statusCode != 200) {
            logging.e(LOG_TAG, "Error $statusCode while retrieving XML data")
            if (statusCode == 401) {
                return HttpStatus.HTTP_WRONG_HTTP_CREDENTIALS
            }
            return if (statusCode == 404) {
                HttpStatus.HTTP_NOT_FOUND
            } else {
                HttpStatus.HTTP_COULD_NOT_CONNECT
            }
        }

        eTagStr = response.header(HTTP_HEADER_NAME_ETAG).orEmpty()
        if (eTagStr.isEmpty()) {
            logging.d(LOG_TAG, "$HTTP_HEADER_NAME_ETAG is missing.")
        } else {
            logging.d(LOG_TAG, "$HTTP_HEADER_NAME_ETAG: '$eTagStr'")
        }

        responseStr = try {
            response.body!!.string()
        } catch (e: NullPointerException) {
            return HttpStatus.HTTP_CANNOT_PARSE_CONTENT
        } catch (e: IOException) {
            return HttpStatus.HTTP_CANNOT_PARSE_CONTENT
        } finally {
            response.body?.close()
        }
        return HttpStatus.HTTP_OK
    }

    private fun setExceptionMessage(exception: SSLException) {
        fun getExceptionMessage(cause: Throwable?, message: String?): String? =
            if (cause == null) message else getExceptionMessage(cause.cause, cause.message)

        exceptionMessage = getExceptionMessage(exception.cause, exception.message).orEmpty()
    }

    private fun customizeExceptionMessage(exception: SSLException) {
        if (exception is SSLHandshakeException && Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            // See https://github.com/EventFahrplan/EventFahrplan/issues/431
            exceptionMessage += "\n\nPlease note that server certificates using elliptic curves " +
                    "with a length > 256 bits are not supported on Android 7.0. This might cause " +
                    "this error."
        }
    }

}
