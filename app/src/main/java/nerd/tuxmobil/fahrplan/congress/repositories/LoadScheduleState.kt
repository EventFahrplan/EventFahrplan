package nerd.tuxmobil.fahrplan.congress.repositories

import nerd.tuxmobil.fahrplan.congress.net.HttpStatus
import nerd.tuxmobil.fahrplan.congress.net.ParseResult

sealed interface LoadScheduleState {

    object InitialFetching : LoadScheduleState

    object Fetching : LoadScheduleState

    object FetchSuccess : LoadScheduleState

    data class FetchFailure(
        val httpStatus: HttpStatus,
        val hostName: String,
        val exceptionMessage: String,
        val isUserRequest: Boolean
    ) : LoadScheduleState

    object InitialParsing : LoadScheduleState

    object Parsing : LoadScheduleState

    object ParseSuccess : LoadScheduleState

    // TODO Merge ParseResult innards into ParseFailure class
    data class ParseFailure(
        val parseResult: ParseResult
    ) : LoadScheduleState

}
