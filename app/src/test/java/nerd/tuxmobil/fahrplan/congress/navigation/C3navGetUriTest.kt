package nerd.tuxmobil.fahrplan.congress.navigation

import android.net.Uri
import androidx.core.net.toUri
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
 * Covers [C3nav.getUri].
 */
@RunWith(Parameterized::class)
class C3navGetUriTest(
    private val baseUrl: String,
    private val room: Room,
    private val convertedName: String,
    private val uri: Uri,
) {

    companion object {

        private const val VALID_BASE_URL = "https://37c3.c3nav.de/l/"
        private const val EMPTY_BASE_URL = ""
        private const val VALID_IDENTIFIER = "88888888-4444-4444-4444-121212121212"

        private fun scenarioOf(baseUrl: String, room: Room, convertedName: String, uri: Uri) =
            arrayOf(baseUrl, room, convertedName, uri)

        @JvmStatic
        @Parameters(name = "{index}: baseUrl = \"{0}\", \"{1}\", convertedName = \"{2}\" -> isSupported = \"{3}\"")
        fun data() = listOf(
            scenarioOf(
                VALID_BASE_URL,
                Room(name = "Ada", identifier = VALID_IDENTIFIER),
                convertedName = "ada",
                uri = "https://37c3.c3nav.de/l/88888888-4444-4444-4444-121212121212".toUri()
            ),
            scenarioOf(
                VALID_BASE_URL,
                Room(name = "", identifier = VALID_IDENTIFIER),
                convertedName = "",
                uri = "https://37c3.c3nav.de/l/88888888-4444-4444-4444-121212121212".toUri()
            ),
            scenarioOf(
                VALID_BASE_URL,
                Room(name = "Ada", identifier = ""),
                convertedName = "ada",
                uri = "https://37c3.c3nav.de/l/ada".toUri()
            ),
            scenarioOf(
                VALID_BASE_URL,
                Room(name = "", identifier = ""),
                convertedName = "",
                uri = "https://37c3.c3nav.de/l/".toUri()
            ),
            scenarioOf(
                EMPTY_BASE_URL,
                Room(name = "Ada", identifier = VALID_IDENTIFIER),
                convertedName = "ada",
                uri = Uri.EMPTY
            ),
        )

    }

    @Test
    fun getUri() {
        val nameConverter = mock<RoomForC3NavConverter> {
            on { convert(anyOrNull()) } doReturn convertedName
        }
        val c3nav = C3nav(baseUrl, nameConverter)
        assertThat(c3nav.getUri(room)).isEqualTo(uri)
    }

}
