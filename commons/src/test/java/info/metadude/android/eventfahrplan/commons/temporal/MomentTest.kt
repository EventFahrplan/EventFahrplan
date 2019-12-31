package info.metadude.android.eventfahrplan.commons.temporal

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MomentTest {

    @Test
    fun isEqualToWithEqualObjects() {
        val Dec30_2019 = 1577746077615
        val moment = Moment(Dec30_2019)
        assertThat(moment.year).isEqualTo(2019)
    }
}
