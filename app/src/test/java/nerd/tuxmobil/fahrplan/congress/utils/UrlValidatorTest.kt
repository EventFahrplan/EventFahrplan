package nerd.tuxmobil.fahrplan.congress.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class UrlValidatorTest {

    @Test
    fun isValidWithHttps() {
        assertThat(UrlValidator("https").isValid()).isFalse()
    }

    @Test
    fun isValidWithHttpsColonSlashSlash() {
        assertThat(UrlValidator("https://").isValid()).isFalse()
    }

    @Test
    fun isValidWithoutTopLevelDomain() {
        assertThat(UrlValidator("https://example").isValid()).isFalse()
    }

    @Test
    fun isValidWithDomain() {
        assertThat(UrlValidator("https://example.com").isValid()).isTrue()
    }

    @Test
    fun isValidWithPath() {
        assertThat(UrlValidator("https://example.com/test").isValid()).isTrue()
    }

    @Test
    fun isValidWithKeyAndValue() {
        assertThat(UrlValidator("https://example.com/test/path?key=value").isValid()).isTrue()
    }

}
