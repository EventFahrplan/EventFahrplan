package info.metadude.android.eventfahrplan.database.extensions

import android.support.test.runner.AndroidJUnit4
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.*
import info.metadude.android.eventfahrplan.database.models.Meta
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MetaExtensionsTest {

    @Test
    fun toContentValues() {
        val meta = Meta(
                dayChangeHour = 13,
                dayChangeMinute = 7,
                eTag = "abc123",
                numDays = 23,
                subtitle = "My subtitle",
                title = "My title",
                version = "v.9.9.9"
        )
        val values = meta.toContentValues()
        assertThat(values.getAsInteger(DAY_CHANGE_HOUR)).isEqualTo(13)
        assertThat(values.getAsInteger(DAY_CHANGE_MINUTE)).isEqualTo(7)
        assertThat(values.getAsString(ETAG)).isEqualTo("abc123")
        assertThat(values.getAsInteger(NUM_DAYS)).isEqualTo(23)
        assertThat(values.getAsString(SUBTITLE)).isEqualTo("My subtitle")
        assertThat(values.getAsString(TITLE)).isEqualTo("My title")
        assertThat(values.getAsString(VERSION)).isEqualTo("v.9.9.9")
    }

}
