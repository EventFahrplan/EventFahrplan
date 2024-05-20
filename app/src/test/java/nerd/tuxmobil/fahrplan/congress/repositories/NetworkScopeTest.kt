package nerd.tuxmobil.fahrplan.congress.repositories

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class NetworkScopeTest {

    @Test
    fun `name can be retrieved within exception handler`() {
        val networkScope = NetworkScope.of(TestExecutionContext) { context, _ ->
            assertThat("Alpha").isEqualTo(context[CoroutineName.Key]?.name)
        }
        networkScope.launchNamed("Alpha") {
            throw Exception()
        }
    }

    @Test
    fun `exception is handled`() {
        var isExceptionHandled = false

        val networkScope = NetworkScope.of(TestExecutionContext) { _, _ ->
            isExceptionHandled = true
        }
        networkScope.launchNamed("Test") {
            throw Exception()
        }
        assertThat(isExceptionHandled).isTrue()
    }

}
