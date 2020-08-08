package nerd.tuxmobil.fahrplan.congress.models

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable
import org.threeten.bp.ZoneId

data class Meta(

        @Deprecated("To be removed. Access from AppRepository only. Left here only for data transfer.")
        var eTag: String = "",
        var numDays: Int = MetasTable.Defaults.NUM_DAYS_DEFAULT,
        var subtitle: String = "",
        var timeZoneId: ZoneId? = null,
        var title: String = "",
        var version: String = ""

)
