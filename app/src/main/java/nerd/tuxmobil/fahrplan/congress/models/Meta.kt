package nerd.tuxmobil.fahrplan.congress.models

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable

data class Meta(

        @Deprecated("To be removed. Access from AppRepository only. Left here only for data transfer.")
        var eTag: String = "",
        var numDays: Int = MetasTable.Defaults.NUM_DAYS_DEFAULT,
        var subtitle: String = "",
        var title: String = "",
        var version: String = ""

)
