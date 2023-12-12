package nerd.tuxmobil.fahrplan.congress.navigation

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Room
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

/**
 * Covers [C3nav.isSupported].
 */
@RunWith(Parameterized::class)
class C3navIsSupportedTest(
    private val baseUrl: String,
    private val room: Room,
    private val convertedName: String,
    private val isSupported: Boolean,
) {

    companion object {

        private const val VALID_BASE_URL = "https://37c3.c3nav.de/l/"
        private const val EMPTY_BASE_URL = ""
        private const val VALID_IDENTIFIER = "88888888-4444-4444-4444-121212121212"

        private fun scenarioOf(
            baseUrl: String,
            room: Room,
            convertedName: String,
            isSupported: Boolean
        ) =
            arrayOf(baseUrl, room, convertedName, isSupported)

        @JvmStatic
        @Parameters(name = "{index}: baseUrl = \"{0}\", \"{1}\", convertedName = \"{2}\" -> isSupported = \"{3}\"")
        fun data() = listOf(
            scenarioOf(
                VALID_BASE_URL,
                Room(name = "Ada", identifier = VALID_IDENTIFIER),
                convertedName = "ada",
                isSupported = true
            ),
            scenarioOf(
                VALID_BASE_URL,
                Room(name = "", identifier = VALID_IDENTIFIER),
                convertedName = "",
                isSupported = true
            ),
            scenarioOf(
                VALID_BASE_URL,
                Room(name = "Ada", identifier = ""),
                convertedName = "ada",
                isSupported = true
            ),
            scenarioOf(
                VALID_BASE_URL,
                Room(name = "", identifier = ""),
                convertedName = "",
                isSupported = false
            ),
            scenarioOf(
                EMPTY_BASE_URL,
                Room(name = "Ada", identifier = VALID_IDENTIFIER),
                convertedName = "ada",
                isSupported = false
            ),
        )

    }

    @Test
    fun isSupported() {
        val nameConverter = mock<RoomForC3NavConverter> {
            on { convert(anyOrNull()) } doReturn convertedName
        }
        val c3nav = C3nav(baseUrl, nameConverter)
        assertThat(c3nav.isSupported(room)).isEqualTo(isSupported)
    }

}
