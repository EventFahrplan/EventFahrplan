package info.metadude.android.eventfahrplan.network.fetching

data class FetchScheduleResult(

        val httpStatus: HttpStatus,
        val scheduleXml: String = "",
        val eTag: String = "",
        val hostName: String,
        val exceptionMessage: String = ""

)
