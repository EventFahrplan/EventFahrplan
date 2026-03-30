package nerd.tuxmobil.fahrplan.congress.dataconverters

import nerd.tuxmobil.fahrplan.congress.net.HttpStatus
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.UnknownServiceException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import javax.net.ssl.SSLException

fun Throwable.toHttpStatus() = when (this) {
    is KeyManagementException -> HttpStatus.HTTP_COULD_NOT_CONNECT
    is NoSuchAlgorithmException -> HttpStatus.HTTP_COULD_NOT_CONNECT
    is SSLException -> HttpStatus.HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE
    is SocketTimeoutException -> HttpStatus.HTTP_CONNECT_TIMEOUT
    is UnknownHostException -> HttpStatus.HTTP_DNS_FAILURE
    is UnknownServiceException -> HttpStatus.HTTP_CLEARTEXT_NOT_PERMITTED
    is IOException -> HttpStatus.HTTP_COULD_NOT_CONNECT
    else -> HttpStatus.HTTP_COULD_NOT_CONNECT
}
