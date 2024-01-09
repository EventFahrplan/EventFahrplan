package nerd.tuxmobil.fahrplan.congress

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import org.junit.Test

class MyAppTest {

    @Test
    fun `getMilliseconds returns moment representing midnight of first day, zone offset +1`() {
        val moment = MyApp.getMoment("Europe/Paris", 2024, 12, 27)
        assertThat(moment).isEqualTo(Moment.ofEpochMilli(1735254000000))
    }

    @Test
    fun `getMilliseconds returns moment representing midnight of last day, zone offset +1`() {
        val moment = MyApp.getMoment("Europe/Paris", 2024, 12, 31)
        assertThat(moment).isEqualTo(Moment.ofEpochMilli(1735599600000))
    }

}
