package info.metadude.android.eventfahrplan.database.models

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Defaults.NUM_DAYS_DEFAULT

data class Meta(

        val httpHeader: HttpHeader = HttpHeader(),
        val numDays: Int = NUM_DAYS_DEFAULT,
        val subtitle: String = "",
        val timeZoneName: String? = null,
        val title: String = "",
        val version: String = ""

)
