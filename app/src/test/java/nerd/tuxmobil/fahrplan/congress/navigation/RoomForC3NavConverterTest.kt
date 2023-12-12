package nerd.tuxmobil.fahrplan.congress.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class RoomForC3NavConverterTest(

        private val roomName: String?,
        private val expectedText: String

) {

    companion object {

        private fun scenarioOf(roomName: String?, expectedText: String) =
                arrayOf(roomName, expectedText)

        @JvmStatic
        @Parameters(name = "{index}: room = \"{0}\" -> expectedText = \"{1}\"")
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

    @Test
    fun convert() {
        assertThat(RoomForC3NavConverter().convert(roomName)).isEqualTo(expectedText)
    }

}
