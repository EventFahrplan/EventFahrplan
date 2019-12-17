package info.metadude.android.eventfahrplan.engelsystem.models

import info.metadude.kotlin.library.engelsystem.models.Shift

sealed class ShiftsResult {

    data class Success(val shifts: List<Shift>) : ShiftsResult()
    data class Error(val httpStatusCode: Int, val exceptionMessage: String) : ShiftsResult()
    data class Exception(val throwable: Throwable) : ShiftsResult()

}
