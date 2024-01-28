package nerd.tuxmobil.fahrplan.congress.repositories

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import nerd.tuxmobil.fahrplan.congress.TestExecutionContext
import nerd.tuxmobil.fahrplan.congress.exceptions.ExceptionHandling
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class NetworkScopeTest {

    @Test
    fun `name can be retrieved within exception handler`() {
        val networkScope = NetworkScope.of(TestExecutionContext, object : ExceptionHandling {
            override fun onExceptionHandling(context: CoroutineContext, throwable: Throwable) {
                assertThat("Alpha").isEqualTo(context[CoroutineName.Key]?.name)
            }
        })
        networkScope.launchNamed("Alpha") {
            throw Exception()
        }
    }

    @Test
    fun `exception is handled`() {
        var isExceptionHandled = false

        val networkScope = NetworkScope.of(TestExecutionContext, object : ExceptionHandling {
            override fun onExceptionHandling(context: CoroutineContext, throwable: Throwable) {
                isExceptionHandled = true
            }
        })
        networkScope.launchNamed("Test") {
            throw Exception()
        }
        assertThat(isExceptionHandled).isTrue
    }

}
