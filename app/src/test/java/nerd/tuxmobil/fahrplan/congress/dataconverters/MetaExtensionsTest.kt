package nerd.tuxmobil.fahrplan.congress.dataconverters

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.threeten.bp.ZoneId
import info.metadude.android.eventfahrplan.database.models.HttpHeader as HttpHeaderDatabaseModel
import info.metadude.android.eventfahrplan.database.models.Meta as MetaDatabaseModel
import info.metadude.android.eventfahrplan.network.models.HttpHeader as HttpHeaderNetworkModel
import info.metadude.android.eventfahrplan.network.models.Meta as MetaNetworkModel
import nerd.tuxmobil.fahrplan.congress.models.HttpHeader as HttpHeaderAppModel
import nerd.tuxmobil.fahrplan.congress.models.Meta as MetaAppModel

class MetaExtensionsTest {

    private val metaAppModel = MetaAppModel(
            httpHeader = HttpHeaderAppModel("abc123"),
            numDays = 23,
            subtitle = "My subtitle",
            timeZoneId = ZoneId.of("Europe/Berlin"),
            title = "My title",
            version = "v.9.9.9"
    )

    private val metaDatabaseModel = MetaDatabaseModel(
            httpHeader = HttpHeaderDatabaseModel("abc123"),
            numDays = 23,
            subtitle = "My subtitle",
            timeZoneName = "Europe/Berlin",
            title = "My title",
            version = "v.9.9.9"
    )

    private val metaNetworkModel = MetaNetworkModel(
            httpHeader = HttpHeaderNetworkModel("abc123"),
            numDays = 23,
            subtitle = "My subtitle",
            timeZoneName = "Europe/Berlin",
            title = "My title",
            version = "v.9.9.9"
    )

    @Test
    fun `toMetaNetworkModel converts an app into a network model`() {
        assertThat(metaAppModel.toMetaNetworkModel()).isEqualTo(metaNetworkModel)
    }

    @Test
    fun `toMetaAppModel converts a database into an app model`() {
        assertThat(metaDatabaseModel.toMetaAppModel()).isEqualTo(metaAppModel)
    }

    @Test
    fun `toMetaDatabaseModel converts a network into a database model`() {
        assertThat(metaNetworkModel.toMetaDatabaseModel()).isEqualTo(metaDatabaseModel)
    }

}
