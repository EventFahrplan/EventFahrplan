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

        object InitialFetching : Initializing {
            override val progressInfo = R.string.progress_loading_data
        }

        object InitialParsing : Initializing {
            override val progressInfo = R.string.progress_processing_data
        }

    }

    sealed interface Active : LoadScheduleUiState {

        object Fetching : Active
        object Parsing : Active

    }

    sealed interface Success : LoadScheduleUiState {

        object FetchSuccess : Success
        object ParseSuccess : Success

    }

    sealed interface Failure : LoadScheduleUiState {

        object SilentFetchFailure : Failure
        object UserTriggeredFetchFailure : Failure
        object ParseFailure : Failure

    }

}
