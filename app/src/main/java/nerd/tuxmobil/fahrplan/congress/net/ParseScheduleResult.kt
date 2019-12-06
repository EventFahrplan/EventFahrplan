package nerd.tuxmobil.fahrplan.congress.net

data class ParseScheduleResult(

        override val isSuccess: Boolean,
        val version: String

) : ParseResult
