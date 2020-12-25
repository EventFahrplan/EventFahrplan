package nerd.tuxmobil.fahrplan.congress.dataconverters

import org.threeten.bp.ZoneId
import org.threeten.bp.zone.ZoneRulesException
import info.metadude.android.eventfahrplan.database.models.Meta as MetaDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Meta as MetaNetworkModel
import nerd.tuxmobil.fahrplan.congress.models.Meta as MetaAppModel


fun MetaAppModel.toMetaNetworkModel() = MetaNetworkModel(
        eTag = eTag,
        numDays = numDays,
        subtitle = subtitle,
        timeZoneName = timeZoneId?.id,
        title = title,
        version = version
)

fun MetaDatabaseModel.toMetaAppModel() = MetaAppModel(
        eTag = eTag,
        numDays = numDays,
        subtitle = subtitle,
        timeZoneId = timeZoneName?.let {
            try {
                ZoneId.of(it)
            } catch (e: ZoneRulesException) {
                // TODO Duplicate try/catch, also in MetaValidation.validate.
                // Crashes on re-installation otherwise. Further investigation needed.
                null
            }
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
