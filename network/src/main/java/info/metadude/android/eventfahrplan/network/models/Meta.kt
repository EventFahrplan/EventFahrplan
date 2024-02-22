package info.metadude.android.eventfahrplan.network.models

data class Meta(

        var httpHeader: HttpHeader = HttpHeader(),
        var numDays: Int = 0,
        var subtitle: String = "",
        var title: String = "",
        var timeZoneName: String? = null,
        var version: String = ""

)
