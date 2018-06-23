package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.database.models.Meta
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MetaExtensionsTest {

    @Test
    fun toMetaAppModel_toMetaDatabaseModel() {
        val meta = Meta(
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

}
