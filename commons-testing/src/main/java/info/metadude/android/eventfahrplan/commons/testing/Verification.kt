@file:JvmName("Verification")

package info.metadude.android.eventfahrplan.commons.testing

import org.mockito.Mockito
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

/**
 *  Verifies certain behavior happened never.
 *
 *  Parameterized alias for [Mockito.verify].
 */
fun <T> verifyInvokedNever(mock: T): T {
    return verify(mock, never())
}

/**
 *  Verifies certain behavior happened exactly once.
 *
 *  Parameterized alias for [Mockito.verify].
 */
fun <T> verifyInvokedOnce(mock: T): T {
    return verify(mock, times(1))
}
