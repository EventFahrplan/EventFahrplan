package nerd.tuxmobil.fahrplan.congress.utils

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class UrlValidatorTest {

    companion object {

        private fun scenarioOf(url: String, isValid: Boolean) =
                arrayOf(url, isValid)

        @JvmStatic
        fun data() = listOf(
                scenarioOf(url = "https", isValid = false),
                scenarioOf(url = "https://", isValid = false),
                scenarioOf(url = "https://example", isValid = false),
                scenarioOf(url = "https://example.com", isValid = true),
                scenarioOf(url = "https://example.com/test", isValid = true),
                scenarioOf(url = "https://example.com/test/path?key=value", isValid = true),
        )
    }

    @ParameterizedTest(name = "{index}: url = {0} -> isValid = {1}")
    @MethodSource("data")
    fun isValid(
        url: String,
        isValid: Boolean
    ) {
        assertThat(UrlValidator(url).isValid()).isEqualTo(isValid)
    }

}
