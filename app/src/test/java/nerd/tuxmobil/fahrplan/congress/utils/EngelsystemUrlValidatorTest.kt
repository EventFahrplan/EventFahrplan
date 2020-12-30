package nerd.tuxmobil.fahrplan.congress.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class EngelsystemUrlValidatorTest(

        private val url: String,
        private val isValid: Boolean

) {

    companion object {

        private fun scenarioOf(url: String, isValid: Boolean) =
                arrayOf(url, isValid)

        @JvmStatic
        @Parameters(name = "{index}: url = {0} -> isValid = {1}")
        fun data() = listOf(
                scenarioOf(url = "https", isValid = false),
                scenarioOf(url = "https://", isValid = false),
                scenarioOf(url = "https://example", isValid = false),
                scenarioOf(url = "https://example.com", isValid = false),
                scenarioOf(url = "https://example.com/test", isValid = false),
                scenarioOf(url = "https://example.com/test/path?", isValid = false),
                scenarioOf(url = "https://example.com/test/path?key", isValid = false),
                scenarioOf(url = "https://example.com/test/path?key=", isValid = false),
                scenarioOf(url = "https://example.com/test/path?key=value", isValid = true),
        )
    }

    @Test
    fun isValid() {
        assertThat(EngelsystemUrlValidator(url).isValid()).isEqualTo(isValid)
    }

}
