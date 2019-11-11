package nerd.tuxmobil.fahrplan.congress.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class EngelsystemUrlValidatorTest {

    @Test
    fun isValidWithHttps() {
        assertThat(EngelsystemUrlValidator("https").isValid()).isFalse()
    }

    @Test
    fun isValidWithHttpsColonSlashSlash() {
        assertThat(EngelsystemUrlValidator("https://").isValid()).isFalse()
    }

    @Test
    fun isValidWithoutTopLevelDomain() {
        assertThat(EngelsystemUrlValidator("https://example").isValid()).isFalse()
    }

    @Test
    fun isValidWithDomain() {
        assertThat(EngelsystemUrlValidator("https://example.com").isValid()).isFalse()
    }

    @Test
    fun isValidWithPath() {
        assertThat(EngelsystemUrlValidator("https://example.com/test").isValid()).isFalse()
    }

    @Test
    fun isValidWithWithoutQueryKey() {
        assertThat(EngelsystemUrlValidator("https://example.com/test/path?").isValid()).isFalse()
    }

    @Test
    fun isValidWithWithoutQueryEquals() {
        assertThat(EngelsystemUrlValidator("https://example.com/test/path?key").isValid()).isFalse()
    }

    @Test
    fun isValidWithWithoutQueryValue() {
        assertThat(EngelsystemUrlValidator("https://example.com/test/path?key=").isValid()).isFalse()
    }

    @Test
    fun isValidWithKeyAndValue() {
        assertThat(EngelsystemUrlValidator("https://example.com/test/path?key=value").isValid()).isTrue()
    }

}
