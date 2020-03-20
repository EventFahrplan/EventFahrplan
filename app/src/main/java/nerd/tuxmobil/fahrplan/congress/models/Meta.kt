package nerd.tuxmobil.fahrplan.congress.models

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable

data class Meta(

        var eTag: String = "",
        var numDays: Int = MetasTable.Defaults.NUM_DAYS_DEFAULT,
        var subtitle: String = "",
        var title: String = "",
        var version: String = ""

)
