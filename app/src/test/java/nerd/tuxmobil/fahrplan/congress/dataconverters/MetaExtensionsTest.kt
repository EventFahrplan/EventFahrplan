package nerd.tuxmobil.fahrplan.congress.dataconverters

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import info.metadude.android.eventfahrplan.database.models.Meta as MetaDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Meta as MetaNetworkModel
import nerd.tuxmobil.fahrplan.congress.models.Meta as MetaAppModel

class MetaExtensionsTest {

    private val metaAppModel = MetaAppModel(
            eTag = "abc123",
            numDays = 23,
            subtitle = "My subtitle",
            title = "My title",
            version = "v.9.9.9"
    )

    private val metaDatabaseModel = MetaDatabaseModel(
            eTag = "abc123",
            numDays = 23,
            subtitle = "My subtitle",
            title = "My title",
            version = "v.9.9.9"
    )

    private val metaNetworkModel = MetaNetworkModel(
            eTag = "abc123",
            numDays = 23,
            subtitle = "My subtitle",
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
