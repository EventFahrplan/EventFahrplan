package info.metadude.android.eventfahrplan.commons.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Sets the given [dispatcher] as an underlying dispatcher of [Dispatchers.Main].
 * All consecutive usages of [Dispatchers.Main] will use given [dispatcher] under the hood.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherTestRule(

    private val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

) : TestWatcher(), TestCoroutineScope by TestCoroutineScope(dispatcher) {

    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
        cleanupTestCoroutines()
        Dispatchers.resetMain()
    }

}
