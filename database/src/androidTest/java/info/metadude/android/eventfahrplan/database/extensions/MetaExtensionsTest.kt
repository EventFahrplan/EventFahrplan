package info.metadude.android.eventfahrplan.database.extensions

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.NUM_DAYS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_ETAG
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_LAST_MODIFIED
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SUBTITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.TIME_ZONE_NAME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.VERSION
import info.metadude.android.eventfahrplan.database.models.HttpHeader
import info.metadude.android.eventfahrplan.database.models.Meta
import org.junit.jupiter.api.Test

class MetaExtensionsTest {

    @Test
    fun toContentValues() {
        val meta = Meta(
                httpHeader = HttpHeader(eTag = "abc123", lastModified = "2023-12-31T23:59:59+01:00"),
                numDays = 23,
                subtitle = "My subtitle",
                timeZoneName = "Europe/Berlin",
                title = "My title",
                version = "v.9.9.9",
        )
        val values = meta.toContentValues()
        assertThat(values.getAsString(SCHEDULE_ETAG)).isEqualTo("abc123")
        assertThat(values.getAsString(SCHEDULE_LAST_MODIFIED)).isEqualTo("2023-12-31T23:59:59+01:00")
        assertThat(values.getAsInteger(NUM_DAYS)).isEqualTo(23)
        assertThat(values.getAsString(SUBTITLE)).isEqualTo("My subtitle")
        assertThat(values.getAsString(TIME_ZONE_NAME)).isEqualTo("Europe/Berlin")
        assertThat(values.getAsString(TITLE)).isEqualTo("My title")
        assertThat(values.getAsString(VERSION)).isEqualTo("v.9.9.9")
    }

}
