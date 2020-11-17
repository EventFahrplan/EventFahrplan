package info.metadude.android.eventfahrplan.commons.extensions

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StandardExtensionsTest {

    @Test
    fun `onFailure invokes the passed lambda if the boolean receiver is false`() {
        var invoked = false
        val returnValue = false.onFailure {
            invoked = true
        }
        assertThat(returnValue).isFalse()
        assertThat(invoked).isTrue()
    }

    @Test
    fun `onFailure does not invoke the passed lambda if the boolean receiver is true`() {
        var invoked = false
        val returnValue = true.onFailure {
            invoked = true
        }
        assertThat(returnValue).isTrue()
        assertThat(invoked).isFalse()
    }

}
