package androidx.core.app

import info.metadude.android.eventfahrplan.commons.logging.Logging

/**
 * Mitigates the effect of a SecurityException which can occur in [JobIntentService].
 * The issue is tracked here: https://issuetracker.google.com/issues/63622293
 */
abstract class SafeJobIntentService : JobIntentService() {

    private companion object {
        const val LOG_TAG = "SafeJobIntentService"
    }

    private val logging = Logging.get()

    internal override fun dequeueWork() =
        try {
            super.dequeueWork()
        } catch (e: SecurityException) {
            // There is not much we can do here.
            logging.report(LOG_TAG, "$e")
            null
        }

}
