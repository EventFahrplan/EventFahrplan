package nerd.tuxmobil.fahrplan.congress.navigation

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class RoomForC3NavConverterTest(

        private val room: String?,
        private val expectedText: String

) {

    companion object {

        private fun scenarioOf(room: String?, expectedText: String) =
                arrayOf(room, expectedText)

        @JvmStatic
        @Parameters(name = "{index}: room = \"{0}\" -> expectedText = \"{1}\"")
        fun data() = listOf(
                scenarioOf(room = "Ada", expectedText = "hall-a"),
                scenarioOf(room = "Borg", expectedText = "hall-b"),
                scenarioOf(room = "Clarke", expectedText = "hall-c"),
                scenarioOf(room = "Dijkstra", expectedText = "hall-d"),
                scenarioOf(room = "Eliza", expectedText = "hall-e"),
                scenarioOf(room = "NonExisting", expectedText = ""),
                scenarioOf(room = "", expectedText = ""),
                scenarioOf(room = null, expectedText = ""),
        )
    }

    @Test
    fun convert() {
        assertThat(RoomForC3NavConverter().convert(room)).isEqualTo(expectedText)
    }

}
