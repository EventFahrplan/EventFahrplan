package nerd.tuxmobil.fahrplan.congress.dataconverters

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import info.metadude.android.eventfahrplan.database.models.Meta as DatabaseMeta
import info.metadude.android.eventfahrplan.network.models.Meta as NetworkMeta

class MetaExtensionsTest {

    @Test
    fun databaseMeta_toMetaAppModel_toMetaDatabaseModel() {
        val meta = DatabaseMeta(
                dayChangeHour = 13,
                dayChangeMinute = 7,
                eTag = "abc123",
                numDays = 23,
                subtitle = "My subtitle",
                title = "My title",
                version = "v.9.9.9"
        )
        assertThat(meta.toMetaAppModel().toMetaDatabaseModel()).isEqualTo(meta)
    }

    @Test
    fun networkMeta_toMetaAppModel_toMetaNetworkModel() {
        val meta = NetworkMeta(
                dayChangeHour = 13,
                dayChangeMinute = 7,
                eTag = "abc123",
                numDays = 23,
                subtitle = "My subtitle",
                title = "My title",
                version = "v.9.9.9"
        )
        assertThat(meta.toMetaAppModel().toMetaNetworkModel()).isEqualTo(meta)
    }

}
