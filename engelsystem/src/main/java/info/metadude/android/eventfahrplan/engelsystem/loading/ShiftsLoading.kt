package info.metadude.android.eventfahrplan.engelsystem.loading

import info.metadude.android.eventfahrplan.engelsystem.models.ShiftsResult
import info.metadude.kotlin.library.engelsystem.models.Shift
import retrofit2.Call
import retrofit2.Response
import retrofit2.awaitResponse

internal object ShiftsLoading {

    /**
     * Suspend extension that allows to suspend a shifts [Call] inside a coroutine.
     * Returns a sealed [ShiftsResult] object. See [toShiftsResult].
     */
    // Visible for testing
    suspend fun Call<List<Shift>>.awaitShiftsResult() = try {
        awaitResponse().toShiftsResult()
    } catch (e: Exception) {
        ShiftsResult.Exception(e)
    }

    /**
     * Converts the receiver into a [ShiftsResult] inspecting the [Response.body]
     * and [Response.message].
     */
    // Visible for testing
    fun Response<List<Shift>>.toShiftsResult() = if (isSuccessful) {
        val shifts = body()
        if (shifts == null) {
            ShiftsResult.Exception.MissingResponseSuccessBody
        } else {
            ShiftsResult.Success(shifts)
        }
    } else {
        val httpStatusMessage: String? = message()
        if (httpStatusMessage.isNullOrEmpty()) {
            ShiftsResult.Exception.MissingResponseHttpStatusMessage
        } else {
            ShiftsResult.Error(code(), httpStatusMessage)
        }
    }

}
