package nerd.tuxmobil.fahrplan.congress.utils

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import nerd.tuxmobil.fahrplan.congress.utils.Validation.ValidationResult
import nerd.tuxmobil.fahrplan.congress.utils.Validation.ValidationResult.Error
import nerd.tuxmobil.fahrplan.congress.utils.Validation.ValidationResult.Success
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource

class UrlValidatorTest {

    companion object {

        private val INVALID = errorOf(R.string.validation_error_invalid_url)
        private val NO_HOST = errorOf(R.string.validation_error_url_without_host)
        private val NO_HTTPS = errorOf(R.string.validation_error_url_without_https)

        private fun errorOf(id: Int) = Error(errorMessage = "$id")

        private fun scenarioOf(url: String, expected: ValidationResult) =
                of(url, expected)

        @JvmStatic
        fun data() = listOf(
                scenarioOf(url = "https", expected = INVALID),
                scenarioOf(url = "https://", expected = INVALID),
                scenarioOf(url = "https://example", expected = INVALID),
                scenarioOf(url = "example.com", expected = NO_HOST),
                scenarioOf(url = "example.com/schedule.xml", expected = NO_HOST),
                scenarioOf(url = "www.example.com/schedule.xml", expected = NO_HOST),
                scenarioOf(url = "http://example.com", expected = NO_HTTPS),
                scenarioOf(url = "HTTP://example.com", expected = NO_HTTPS),
                scenarioOf(url = "ftp://example.com/schedule.xml", expected = INVALID),
                scenarioOf(url = "rtsp://example.com/schedule.xml", expected = INVALID),
                scenarioOf(url = "https://example.com", expected = Success),
                scenarioOf(url = "https://example.com/test", expected = Success),
                scenarioOf(url = "https://example.com/test/path?key=value", expected = Success),
                scenarioOf(url = "HTTPS://example.com/schedule.xml", expected = Success),
                scenarioOf(url = "Https://EXAMPLE.COM/schedule.xml", expected = Success),
        )
    }

    private val validator = UrlValidator(
        resourceResolver = object : ResourceResolving {
            override fun getString(id: Int, vararg formatArgs: Any) = "$id"
            override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any) = "$id"
        },
        urlTypeName = "",
    )

    @ParameterizedTest(name = "{index}: url = {0} -> {1}")
    @MethodSource("data")
    fun validate(
        url: String,
        expected: ValidationResult,
    ) {
        assertThat(validator.validate(url)).isEqualTo(expected)
    }

}
