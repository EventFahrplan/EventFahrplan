package info.metadude.android.eventfahrplan.engelsystem.models

import info.metadude.kotlin.library.engelsystem.models.Shift

sealed class ShiftsResult {

    data class Success(val shifts: List<Shift>) : ShiftsResult()
    data class Error(val httpStatusCode: Int, val exceptionMessage: String) : ShiftsResult()

    open class Exception(val throwable: Throwable) : ShiftsResult() {

        object MissingResponseSuccessBody : Exception(
                NullPointerException("Response success body is null."))

        object MissingResponseHttpStatusMessage : Exception(
                NullPointerException("Response HTTP status message is null or empty."))

        override fun toString() = "Exception(throwable=$throwable)"

    }

}
