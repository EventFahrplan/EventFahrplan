package nerd.tuxmobil.fahrplan.congress.net

data class FetchScheduleResult(

        val httpStatus: HttpStatus,
        val eTag: String = "",
        val hostName: String,
        val exceptionMessage: String = ""

) {

    val isSuccessful
        get() = HttpStatus.HTTP_OK == httpStatus

    companion object {

        @JvmStatic
        fun createError(httpStatus: HttpStatus, hostName: String) =
                FetchScheduleResult(httpStatus = httpStatus, hostName = hostName)

    }

}
