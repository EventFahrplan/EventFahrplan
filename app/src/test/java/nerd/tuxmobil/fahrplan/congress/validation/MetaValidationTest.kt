package nerd.tuxmobil.fahrplan.congress.validation

import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.network.models.Meta
import nerd.tuxmobil.fahrplan.congress.validation.MetaValidation.validate
import org.junit.jupiter.api.Test

class MetaValidationTest {

    @Test
    fun `validate returns instance with default values`() {
        assertThat(Meta().validate()).isEqualTo(Meta())
    }

    @Test
    fun `validate returns timeZoneName = null if timeZoneName = null`() {
        assertThat(Meta(timeZoneName = null).validate()).isEqualTo(Meta(timeZoneName = null))
    }

    @Test
    fun `validate returns timeZoneName = null if timeZoneName is empty`() {
        assertThat(Meta(timeZoneName = "").validate()).isEqualTo(Meta(timeZoneName = null))
    }

    @Test
    fun `validate returns timeZoneName = null if timeZoneName is invalid`() {
        assertThat(Meta(timeZoneName = "Berlin").validate()).isEqualTo(Meta(timeZoneName = null))
    }

    @Test
    fun `validate returns timeZoneName value if timeZoneName is valid`() {
        assertThat(Meta(timeZoneName = "Europe/Berlin").validate())
                .isEqualTo(Meta(timeZoneName = "Europe/Berlin"))
    }

}
