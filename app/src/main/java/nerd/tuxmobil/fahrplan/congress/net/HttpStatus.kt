package nerd.tuxmobil.fahrplan.congress.net

enum class HttpStatus(

        private val value: Int

) {

    HTTP_OK(0),
    HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE(1),
    HTTP_DNS_FAILURE(2),
    HTTP_COULD_NOT_CONNECT(3),
    HTTP_SSL_SETUP_FAILURE(4),
    HTTP_CANNOT_PARSE_CONTENT(5),
    HTTP_WRONG_HTTP_CREDENTIALS(6),
    HTTP_CONNECT_TIMEOUT(7),
    HTTP_NOT_MODIFIED(8),
    HTTP_NOT_FOUND(9),
    HTTP_CLEARTEXT_NOT_PERMITTED(10);

    companion object {

        fun of(value: Int) = when (value) {
            0 -> HTTP_OK
            1 -> HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE
            2 -> HTTP_DNS_FAILURE
            3 -> HTTP_COULD_NOT_CONNECT
            4 -> HTTP_SSL_SETUP_FAILURE
            5 -> HTTP_CANNOT_PARSE_CONTENT
            6 -> HTTP_WRONG_HTTP_CREDENTIALS
            7 -> HTTP_CONNECT_TIMEOUT
            8 -> HTTP_NOT_MODIFIED
            9 -> HTTP_NOT_FOUND
            10 -> HTTP_CLEARTEXT_NOT_PERMITTED
            else -> throw IllegalStateException("Unknown HttpStatus value: '$value'.")
        }

    }

    override fun toString() = "$value"

}
