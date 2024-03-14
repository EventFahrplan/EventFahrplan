package nerd.tuxmobil.fahrplan.congress.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class RoomForC3NavConverterTest {

    companion object {

        private fun scenarioOf(roomName: String?, expectedText: String) =
                arrayOf(roomName, expectedText)

        @JvmStatic
        fun data() = listOf(
                scenarioOf(roomName = "Ada", expectedText = "hall-a"),
                scenarioOf(roomName = "Borg", expectedText = "hall-b"),
                scenarioOf(roomName = "Clarke", expectedText = "hall-c"),
                scenarioOf(roomName = "Dijkstra", expectedText = "hall-d"),
                scenarioOf(roomName = "Eliza", expectedText = "hall-e"),
                scenarioOf(roomName = "NonExisting", expectedText = ""),
                scenarioOf(roomName = "", expectedText = ""),
                scenarioOf(roomName = null, expectedText = ""),
        )
    }

    @ParameterizedTest(name = """{index}: room = "{0}" -> expectedText = "{1}"""")
    @MethodSource("data")
    fun convert(
        roomName: String?,
        expectedText: String,
    ) {
        assertThat(RoomForC3NavConverter().convert(roomName)).isEqualTo(expectedText)
    }

}
