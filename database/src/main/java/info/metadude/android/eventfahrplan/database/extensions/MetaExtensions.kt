package info.metadude.android.eventfahrplan.database.extensions

import androidx.core.content.contentValuesOf
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.NUM_DAYS
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_ETAG
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SCHEDULE_LAST_MODIFIED
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.SUBTITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.TIME_ZONE_NAME
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.TITLE
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.MetasTable.Columns.VERSION
import info.metadude.android.eventfahrplan.database.models.Meta

fun Meta.toContentValues() = contentValuesOf(
        SCHEDULE_ETAG to httpHeader.eTag,
        SCHEDULE_LAST_MODIFIED to httpHeader.lastModified,
        NUM_DAYS to numDays,
        SUBTITLE to subtitle,
        TIME_ZONE_NAME to timeZoneName,
        TITLE to title,
        VERSION to version
)
