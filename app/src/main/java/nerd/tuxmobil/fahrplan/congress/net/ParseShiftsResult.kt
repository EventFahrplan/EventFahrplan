package nerd.tuxmobil.fahrplan.congress.net

sealed class ParseShiftsResult(

        override val isSuccess: Boolean

) : ParseResult {

    data object Success : ParseShiftsResult(true)

    data class Error(

            val httpStatusCode: Int,
            val exceptionMessage: String

    ) : ParseShiftsResult(false) {

        fun isForbidden() = 403 == httpStatusCode && "Forbidden" == exceptionMessage

        fun isNotFound() = 404 == httpStatusCode && "Not Found" == exceptionMessage

    }

    data class Exception(

            val throwable: Throwable

    ) : ParseShiftsResult(false)

    companion object {

        @JvmStatic
        fun of(result: LoadShiftsResult) = when (result) {
            is LoadShiftsResult.Success -> Success
            is LoadShiftsResult.Error -> Error(result.httpStatusCode, result.exceptionMessage)
            is LoadShiftsResult.Exception -> Exception(result.throwable)
        }

    }

}
