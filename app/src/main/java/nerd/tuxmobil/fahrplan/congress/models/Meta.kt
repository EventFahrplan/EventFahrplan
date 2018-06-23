package nerd.tuxmobil.fahrplan.congress.models

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable

data class Meta(

        var dayChangeHour: Int = MetasTable.Defaults.DAY_CHANGE_HOUR_DEFAULT,
        var dayChangeMinute: Int = MetasTable.Defaults.DAY_CHANGE_MINUTE_DEFAULT,
        var eTag: String = "",
        var numDays: Int = MetasTable.Defaults.NUM_DAYS_DEFAULT,
        var subtitle: String = "",
        var title: String = "",
        var version: String = ""

)
