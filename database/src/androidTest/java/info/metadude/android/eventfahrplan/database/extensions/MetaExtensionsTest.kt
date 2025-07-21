package info.metadude.android.eventfahrplan.database.extensions

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.NUM_DAYS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_ETAG
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_GENERATOR_NAME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_GENERATOR_VERSION
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_LAST_MODIFIED
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SUBTITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.TIME_ZONE_NAME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.VERSION
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Defaults.NUM_DAYS_DEFAULT
import info.metadude.android.eventfahrplan.database.models.HttpHeader
import info.metadude.android.eventfahrplan.database.models.Meta
import info.metadude.android.eventfahrplan.database.models.ScheduleGenerator
import org.junit.jupiter.api.Test

class MetaExtensionsTest {

    @Test
    fun toContentValuesWithPropertiesWithNonNullValues() {
        val meta = Meta(
                httpHeader = HttpHeader(eTag = "abc123", lastModified = "2023-12-31T23:59:59+01:00"),
                numDays = 23,
                scheduleGenerator = ScheduleGenerator(name = "pretalx", version = "2024.1.0"),
                subtitle = "My subtitle",
                timeZoneName = "Europe/Berlin",
                title = "My title",
                version = "v.9.9.9",
        )
        val values = meta.toContentValues()
        assertThat(values.getAsString(SCHEDULE_ETAG)).isEqualTo("abc123")
        assertThat(values.getAsString(SCHEDULE_LAST_MODIFIED)).isEqualTo("2023-12-31T23:59:59+01:00")
        assertThat(values.getAsString(SCHEDULE_GENERATOR_NAME)).isEqualTo("pretalx")
        assertThat(values.getAsString(SCHEDULE_GENERATOR_VERSION)).isEqualTo("2024.1.0")
        assertThat(values.getAsInteger(NUM_DAYS)).isEqualTo(23)
        assertThat(values.getAsString(SUBTITLE)).isEqualTo("My subtitle")
        assertThat(values.getAsString(TIME_ZONE_NAME)).isEqualTo("Europe/Berlin")
        assertThat(values.getAsString(TITLE)).isEqualTo("My title")
        assertThat(values.getAsString(VERSION)).isEqualTo("v.9.9.9")
    }

    @Test
    fun toContentValuesWithPropertiesWithDefaultValues() {
        val meta = Meta(
                httpHeader = HttpHeader(),
                numDays = NUM_DAYS_DEFAULT,
                scheduleGenerator = null,
                subtitle = "",
                timeZoneName = null,
                title = "",
                version = "",
        )
        val values = meta.toContentValues()
        assertThat(values.getAsString(SCHEDULE_ETAG)).isEmpty()
        assertThat(values.getAsString(SCHEDULE_LAST_MODIFIED)).isEmpty()
        assertThat(values.getAsString(SCHEDULE_GENERATOR_NAME)).isNull()
        assertThat(values.getAsString(SCHEDULE_GENERATOR_VERSION)).isNull()
        assertThat(values.getAsInteger(NUM_DAYS)).isEqualTo(NUM_DAYS_DEFAULT)
        assertThat(values.getAsString(SUBTITLE)).isEmpty()
        assertThat(values.getAsString(TIME_ZONE_NAME)).isNull()
        assertThat(values.getAsString(TITLE)).isEmpty()
        assertThat(values.getAsString(VERSION)).isEmpty()
    }

}
