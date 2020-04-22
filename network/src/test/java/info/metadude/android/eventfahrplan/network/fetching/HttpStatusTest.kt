package info.metadude.android.eventfahrplan.network.fetching

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class HttpStatusTest {

    @Test
    fun `isSuccessful maps to HTTP_OK`() {
        assertThat(HttpStatus.HTTP_OK.isSuccessful).isTrue()
    }

    @Test
    fun `isNotModified maps to HTTP_NOT_MODIFIED`() {
        assertThat(HttpStatus.HTTP_NOT_MODIFIED.isNotModified).isTrue()
    }

}
