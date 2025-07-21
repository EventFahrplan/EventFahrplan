package info.metadude.android.eventfahrplan.network.models

data class Meta(

        var scheduleGenerator: ScheduleGenerator? = null, // null by default if network response lacks the corresponding property
        var httpHeader: HttpHeader = HttpHeader(),
        var numDays: Int = 0,
        var subtitle: String = "",
        var title: String = "",
        var timeZoneName: String? = null,
        var version: String = ""

)
