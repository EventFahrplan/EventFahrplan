package nerd.tuxmobil.fahrplan.congress.schedule.observables

import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.schedule.MainActivity
import nerd.tuxmobil.fahrplan.congress.schedule.MainViewModel

/**
 * Payload of the observable [loadScheduleUiState][MainViewModel.loadScheduleUiState] property
 * in the [MainViewModel] which is observed by the [MainActivity].
 */
sealed interface LoadScheduleUiState {

    sealed interface Initializing : LoadScheduleUiState {
        val progressInfo: Int

        data object InitialFetching : Initializing {
            override val progressInfo = R.string.progress_loading_data
        }

        data object InitialParsing : Initializing {
            override val progressInfo = R.string.progress_processing_data
        }

    }

    sealed interface Active : LoadScheduleUiState {

        data object Fetching : Active
        data object Parsing : Active

    }

    sealed interface Success : LoadScheduleUiState {

        data object FetchSuccess : Success
        data object ParseSuccess : Success

    }

    sealed interface Failure : LoadScheduleUiState {

        data object SilentFetchFailure : Failure
        data object UserTriggeredFetchFailure : Failure
        data object ParseFailure : Failure

    }

}
