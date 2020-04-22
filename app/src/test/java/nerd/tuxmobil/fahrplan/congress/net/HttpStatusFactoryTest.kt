package nerd.tuxmobil.fahrplan.congress.net

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class HttpStatusFactoryTest(

        private val ordinal: Int,
        private val httpStatus: HttpStatus

) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "#{index}: {0} == {1}")
        fun data() = listOf(
                arrayOf(0, HttpStatus.HTTP_OK),
                arrayOf(1, HttpStatus.HTTP_LOGIN_FAIL_UNTRUSTED_CERTIFICATE),
                arrayOf(2, HttpStatus.HTTP_DNS_FAILURE),
                arrayOf(3, HttpStatus.HTTP_COULD_NOT_CONNECT),
                arrayOf(4, HttpStatus.HTTP_SSL_SETUP_FAILURE),
                arrayOf(5, HttpStatus.HTTP_CANNOT_PARSE_CONTENT),
                arrayOf(6, HttpStatus.HTTP_WRONG_HTTP_CREDENTIALS),
                arrayOf(7, HttpStatus.HTTP_CONNECT_TIMEOUT),
                arrayOf(8, HttpStatus.HTTP_NOT_MODIFIED),
                arrayOf(9, HttpStatus.HTTP_NOT_FOUND),
                arrayOf(10, HttpStatus.HTTP_CLEARTEXT_NOT_PERMITTED)
        )
    }

    @Test
    fun `Creates an HttpStatus from its ordinal`() {
        assertThat(HttpStatus.of(ordinal)).isEqualTo(httpStatus)
    }

}
