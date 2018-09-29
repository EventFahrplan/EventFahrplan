package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.network.fetching.HttpStatus as NetworkHttpStatus
import nerd.tuxmobil.fahrplan.congress.net.HttpStatus as AppHttpStatus

fun NetworkHttpStatus.toAppHttpStatus() = when (this) {
    NetworkHttpStatus.HTTP_OK -> AppHttpStatus.HTTP_OK
    NetworkHttpStatus.HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE -> AppHttpStatus.HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE
    NetworkHttpStatus.HTTP_DNS_FAILURE -> AppHttpStatus.HTTP_DNS_FAILURE
    NetworkHttpStatus.HTTP_COULD_NOT_CONNECT -> AppHttpStatus.HTTP_COULD_NOT_CONNECT
    NetworkHttpStatus.HTTP_SSL_SETUP_FAILURE -> AppHttpStatus.HTTP_SSL_SETUP_FAILURE
    NetworkHttpStatus.HTTP_CANNOT_PARSE_CONTENT -> AppHttpStatus.HTTP_CANNOT_PARSE_CONTENT
    NetworkHttpStatus.HTTP_WRONG_HTTP_CREDENTIALS -> AppHttpStatus.HTTP_WRONG_HTTP_CREDENTIALS
    NetworkHttpStatus.HTTP_CONNECT_TIMEOUT -> AppHttpStatus.HTTP_CONNECT_TIMEOUT
    NetworkHttpStatus.HTTP_NOT_MODIFIED -> AppHttpStatus.HTTP_NOT_MODIFIED
    NetworkHttpStatus.HTTP_NOT_FOUND -> AppHttpStatus.HTTP_NOT_FOUND
}

fun AppHttpStatus.toNetworkHttpStatus() = when (this) {
    AppHttpStatus.HTTP_OK -> NetworkHttpStatus.HTTP_OK
    AppHttpStatus.HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE -> NetworkHttpStatus.HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE
    AppHttpStatus.HTTP_DNS_FAILURE -> NetworkHttpStatus.HTTP_DNS_FAILURE
    AppHttpStatus.HTTP_COULD_NOT_CONNECT -> NetworkHttpStatus.HTTP_COULD_NOT_CONNECT
    AppHttpStatus.HTTP_SSL_SETUP_FAILURE -> NetworkHttpStatus.HTTP_SSL_SETUP_FAILURE
    AppHttpStatus.HTTP_CANNOT_PARSE_CONTENT -> NetworkHttpStatus.HTTP_CANNOT_PARSE_CONTENT
    AppHttpStatus.HTTP_WRONG_HTTP_CREDENTIALS -> NetworkHttpStatus.HTTP_WRONG_HTTP_CREDENTIALS
    AppHttpStatus.HTTP_CONNECT_TIMEOUT -> NetworkHttpStatus.HTTP_CONNECT_TIMEOUT
    AppHttpStatus.HTTP_NOT_MODIFIED -> NetworkHttpStatus.HTTP_NOT_MODIFIED
    AppHttpStatus.HTTP_NOT_FOUND -> NetworkHttpStatus.HTTP_NOT_FOUND
}
