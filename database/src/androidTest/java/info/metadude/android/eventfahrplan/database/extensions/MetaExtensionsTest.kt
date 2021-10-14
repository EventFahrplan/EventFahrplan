package info.metadude.android.eventfahrplan.database.extensions

import androidx.test.ext.junit.runners.AndroidJUnit4
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.ETAG
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.NUM_DAYS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SUBTITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.TIME_ZONE_NAME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.VERSION
import info.metadude.android.eventfahrplan.database.models.Meta
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MetaExtensionsTest {

    @Test
    fun toContentValues() {
        val meta = Meta(
                eTag = "abc123",
                numDays = 23,
                subtitle = "My subtitle",
                timeZoneName = "Europe/Berlin",
                title = "My title",
                version = "v.9.9.9"
        )
        val values = meta.toContentValues()
        assertThat(values.getAsString(ETAG)).isEqualTo("abc123")
        assertThat(values.getAsInteger(NUM_DAYS)).isEqualTo(23)
        assertThat(values.getAsString(SUBTITLE)).isEqualTo("My subtitle")
        assertThat(values.getAsString(TIME_ZONE_NAME)).isEqualTo("Europe/Berlin")
        assertThat(values.getAsString(TITLE)).isEqualTo("My title")
        assertThat(values.getAsString(VERSION)).isEqualTo("v.9.9.9")
    }

}
