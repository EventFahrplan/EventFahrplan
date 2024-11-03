package nerd.tuxmobil.fahrplan.congress.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.testing.MainDispatcherTestExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MainDispatcherTestExtension::class)
class SearchHistoryManagerTest {

    @Test
    fun `searchHistory emits empty list after initialization`() = runTest {
        val manager = SearchHistoryManager(createRepository(this,
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
            val manager = SearchHistoryManager(createRepository(this,
                history = listOf("Foobar")
            ))
            manager.searchHistory.test {
                assertThat(awaitItem()).containsExactly("Foobar")
                expectNoEvents()
            }
        }

    @Test
    fun `searchHistory emits list with one item after appending duplicate item`() = runTest {
        val manager = SearchHistoryManager(createRepository(this,
            history = listOf("First")
        ))
        manager.searchHistory.test {
            assertThat(awaitItem()).isEqualTo(listOf("First"))
            manager.append(this@runTest, "First")
            assertThat(awaitItem()).isEqualTo(listOf("First"))
            expectNoEvents()
        }
    }

    @Test
    fun `searchHistory emits list without empty strings`() = runTest {
        val manager = SearchHistoryManager(createRepository(this,
            history = listOf("Head", "", "Tail")
        ))
        manager.searchHistory.test {
            assertThat(awaitItem()).isEqualTo(listOf("Tail", "Head"))
            expectNoEvents()
        }
    }

    @Test
    fun `searchHistory emits reversed list`() = runTest {
        val manager = SearchHistoryManager(createRepository(this,
            history = listOf("Head", "Tail")
        ))
        manager.searchHistory.test {
            assertThat(awaitItem()).isEqualTo(listOf("Tail", "Head"))
            expectNoEvents()
        }
    }

    @Test
    fun `searchHistory emits reordered list after appending duplicate head item`() = runTest {
        val manager = SearchHistoryManager(createRepository(this,
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
        val manager = SearchHistoryManager(createRepository(this,
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
        val manager = SearchHistoryManager(createRepository(this,
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

private fun createRepository(scope: CoroutineScope, history: List<String>) =
    Repository(scope).apply {
        this.history = history
    }

private class Repository(
    private val scope: CoroutineScope,
) : SearchRepository {

    private lateinit var job: Job
    private val refreshSignal = MutableSharedFlow<Unit>()
    var history = listOf<String>()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val searchHistory: Flow<List<String>> by lazy {
        refreshSignal
            .onStart { emit(Unit) }
            .mapLatest { history }
            .flowOn(StandardTestDispatcher())
    }

    private fun refresh() {
        job = scope.launch {
            refreshSignal.emit(Unit)
        }
    }

    override fun updateSearchHistory(history: List<String>) {
        this.history = history
        refresh()
    }
}


