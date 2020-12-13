package nerd.tuxmobil.fahrplan.congress.dataconverters

import org.threeten.bp.ZoneId
import info.metadude.android.eventfahrplan.database.models.Meta as MetaDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Meta as MetaNetworkModel
import nerd.tuxmobil.fahrplan.congress.models.Meta as MetaAppModel


fun MetaDatabaseModel.toMetaNetworkModel() = MetaNetworkModel(
        eTag = eTag,
        numDays = numDays,
        subtitle = subtitle,
        timeZoneName = timeZoneName,
        title = title,
        version = version
)

fun MetaDatabaseModel.toMetaAppModel() = MetaAppModel(
        eTag = eTag,
        numDays = numDays,
        subtitle = subtitle,
        timeZoneId = timeZoneName?.let {
            // Try/catch omitted here because this happened in MetaValidation.validate before.
            ZoneId.of(it)
        },
        title = title,
        version = version
)

fun MetaNetworkModel.toMetaDatabaseModel() = MetaDatabaseModel(
        eTag = eTag,
        numDays = numDays,
        subtitle = subtitle,
        timeZoneName = timeZoneName,
        title = title,
        version = version
)
