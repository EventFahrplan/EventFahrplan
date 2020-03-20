package info.metadude.android.eventfahrplan.database.models

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Defaults.NUM_DAYS_DEFAULT

data class Meta(

        val eTag: String = "",
        val numDays: Int = NUM_DAYS_DEFAULT,
        val subtitle: String = "",
        val title: String = "",
        val version: String = ""

)
