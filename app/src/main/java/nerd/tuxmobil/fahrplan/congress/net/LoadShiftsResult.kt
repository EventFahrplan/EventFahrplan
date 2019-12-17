package nerd.tuxmobil.fahrplan.congress.net

sealed class LoadShiftsResult {

    object Success : LoadShiftsResult()
    data class Error(val httpStatusCode: Int, val exceptionMessage: String) : LoadShiftsResult()
    data class Exception(val throwable: Throwable) : LoadShiftsResult()

}
