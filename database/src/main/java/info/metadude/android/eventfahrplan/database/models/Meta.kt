package info.metadude.android.eventfahrplan.database.models

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Defaults.*

data class Meta(

        val dayChangeHour: Int = DAY_CHANGE_HOUR_DEFAULT,
        val dayChangeMinute: Int = DAY_CHANGE_MINUTE_DEFAULT,
        val eTag: String = "",
        val numDays: Int = NUM_DAYS_DEFAULT,
        val subtitle: String = "",
        val title: String = "",
        val version: String = ""

)
