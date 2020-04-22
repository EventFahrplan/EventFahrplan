package nerd.tuxmobil.fahrplan.congress.errormessaging

import nerd.tuxmobil.fahrplan.congress.net.HttpStatus

data class ErrorMessage(

        val httpStatus: HttpStatus,
        val exceptionMessage: String,
        val hostName: String

)
