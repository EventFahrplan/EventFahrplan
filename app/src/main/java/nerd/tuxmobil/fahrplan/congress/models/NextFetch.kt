package nerd.tuxmobil.fahrplan.congress.models

import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.preferences.RealSharedPreferencesRepository

data class NextFetch(
    val nextFetchAt: Moment,
    val interval: Duration,
) {
    /**
     * Returns true if the values of [nextFetchAt] and [interval] are reasonable to be processed.
     *
     * See [SCHEDULE_NEXT_FETCH_AT_DEFAULT_VALUE][RealSharedPreferencesRepository.SCHEDULE_NEXT_FETCH_AT_DEFAULT_VALUE]
     * and [SCHEDULE_NEXT_FETCH_INTERVAL_DEFAULT_VALUE][RealSharedPreferencesRepository.SCHEDULE_NEXT_FETCH_INTERVAL_DEFAULT_VALUE]
     */
    fun isValid() = !nextFetchAt.isEpoch() && interval.isPositive()
}
