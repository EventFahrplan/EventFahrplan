// SPDX-FileCopyrightText: 2023 johnjohndoe <https://github.com/johnjohndoe>
//
// SPDX-License-Identifier: Apache-2.0

package info.metadude.android.eventfahrplan.commons.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Sets the given [dispatcher] as an underlying dispatcher of [Dispatchers.Main].
 * All consecutive usages of [Dispatchers.Main] will use the given [dispatcher] under the hood.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherTestRule(

    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

) : TestWatcher() {

    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }

}
