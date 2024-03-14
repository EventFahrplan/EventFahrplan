package info.metadude.android.eventfahrplan.network.fetching

import info.metadude.android.eventfahrplan.network.models.HttpHeader

data class FetchScheduleResult(

        val httpStatus: HttpStatus,
        val scheduleXml: String = "",
        val httpHeader: HttpHeader,
        val hostName: String,
        val exceptionMessage: String = ""

)
