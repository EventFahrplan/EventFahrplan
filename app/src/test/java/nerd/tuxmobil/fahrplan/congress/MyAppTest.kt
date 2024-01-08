package nerd.tuxmobil.fahrplan.congress

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MyAppTest {

    @Test
    fun `getMilliseconds returns milliseconds representing midnight of first day, zone offset +1`() {
        val milliseconds = MyApp.getMilliseconds("Europe/Paris", 2024, 12, 27)
        assertThat(milliseconds).isEqualTo(1735254000000)
    }

    @Test
    fun `getMilliseconds returns milliseconds representing midnight of last day, zone offset +1`() {
        val milliseconds = MyApp.getMilliseconds("Europe/Paris", 2024, 12, 31)
        assertThat(milliseconds).isEqualTo(1735599600000)
    }

}
