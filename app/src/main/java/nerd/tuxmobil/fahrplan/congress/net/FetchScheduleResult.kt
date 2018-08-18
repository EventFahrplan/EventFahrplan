package nerd.tuxmobil.fahrplan.congress.net

import nerd.tuxmobil.fahrplan.congress.net.CustomHttpClient.HTTP_STATUS

data class FetchScheduleResult(

        val httpStatus: HTTP_STATUS,
        val scheduleXml: String = "",
        val eTag: String = "",
        val hostName: String

)
