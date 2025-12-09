package nerd.tuxmobil.fahrplan.congress.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MainDispatcherTestExtension::class)
class SearchHistoryManagerTest {

    @Test
    fun `searchHistory emits empty list after initialization`() = runTest {
        val manager = SearchHistoryManager(createRepository(
            history = emptyList()
        ))
        manager.searchHistory.test {
            assertThat(awaitItem()).isEqualTo(emptyList<String>())
            expectNoEvents()
        }
    }

    @Test
    fun `searchHistory emits list with one item after initializing it list with one item`() =
        runTest {
            val manager = SearchHistoryManager(createRepository(
                history = listOf("Foobar")
            ))
            manager.searchHistory.test {
                assertThat(awaitItem()).containsExactly("Foobar")
                expectNoEvents()
            }
        }

    @Test
    fun `searchHistory doesn't emit after appending duplicate item`() = runTest {
        val manager = SearchHistoryManager(createRepository(
            history = listOf("First")
        ))
        manager.searchHistory.test {
            assertThat(awaitItem()).isEqualTo(listOf("First"))
            manager.append(this@runTest, "First")
            expectNoEvents()
        }
    }

    @Test
    fun `searchHistory emits list without empty strings`() = runTest {
        val manager = SearchHistoryManager(createRepository(
            history = listOf("Head", "", "Tail")
        ))
        manager.searchHistory.test {
            assertThat(awaitItem()).isEqualTo(listOf("Tail", "Head"))
            expectNoEvents()
        }
    }

    @Test
    fun `searchHistory emits reversed list`() = runTest {
        val manager = SearchHistoryManager(createRepository(
            history = listOf("Head", "Tail")
        ))
        manager.searchHistory.test {
            assertThat(awaitItem()).isEqualTo(listOf("Tail", "Head"))
            expectNoEvents()
        }
    }

    @Test
    fun `searchHistory emits reordered list after appending duplicate head item`() = runTest {
        val manager = SearchHistoryManager(createRepository(
            history = listOf("Head", "Tail")
        ))
        manager.searchHistory.test {
            assertThat(awaitItem()).isEqualTo(listOf("Tail", "Head"))
            manager.append(this@runTest, "Head")
            assertThat(awaitItem()).isEqualTo(listOf("Head", "Tail"))
            expectNoEvents()
        }
    }

    @Test
    fun `searchHistory emits list with maximum 20 items`() = runTest {
        val manager = SearchHistoryManager(createRepository(
            history = (1..20).map { it.toString() }
        ))
        manager.searchHistory.test {
            assertThat(awaitItem()).isEqualTo((20 downTo 1).map { it.toString() })
            manager.append(this@runTest, "21")
            assertThat(awaitItem()).isEqualTo((21 downTo 2).map { it.toString() })
            expectNoEvents()
        }
    }

    @Test
    fun `searchHistory emits empty list when clearing it`() = runTest {
        val manager = SearchHistoryManager(createRepository(
            history = listOf("Foobar")
        ))
        manager.searchHistory.test {
            assertThat(awaitItem()).isEqualTo(listOf("Foobar"))
            manager.clear(this@runTest)
            assertThat(awaitItem()).isEqualTo(emptyList<String>())
            expectNoEvents()
        }
    }

}

private fun createRepository(history: List<String>): SearchRepository {
    return InMemorySearchRepository(history)
}
