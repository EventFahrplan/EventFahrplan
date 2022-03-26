package androidx.core.app;

import info.metadude.android.eventfahrplan.commons.logging.Logging;

/**
 * Mitigates the effect of a SecurityException which can occur in {@link JobIntentService}.
 * The issue is tracked here: https://issuetracker.google.com/issues/63622293
 */
public abstract class SafeJobIntentService extends JobIntentService {

    private static final String LOG_TAG = "SafeJobIntentService";

    @Override
    GenericWorkItem dequeueWork() {
        try {
            return super.dequeueWork();
        } catch (SecurityException e) {
            // There is not much we can do here.
            Logging.get().report(LOG_TAG, "" + e.getMessage());
        }
        return null;
    }

}
