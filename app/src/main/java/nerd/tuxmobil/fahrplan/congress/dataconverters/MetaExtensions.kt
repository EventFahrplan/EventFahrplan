package nerd.tuxmobil.fahrplan.congress.dataconverters

import org.threeten.bp.ZoneId
import org.threeten.bp.zone.ZoneRulesException
import info.metadude.android.eventfahrplan.database.models.Meta as MetaDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Meta as MetaNetworkModel
import nerd.tuxmobil.fahrplan.congress.models.Meta as MetaAppModel


fun MetaAppModel.toMetaNetworkModel() = MetaNetworkModel(
        httpHeader = httpHeader.toHttpHeaderNetworkModel(),
        numDays = numDays,
        scheduleGenerator = scheduleGenerator?.toScheduleGeneratorNetworkModel(),
        subtitle = subtitle,
        timeZoneName = timeZoneId?.id,
        title = title,
        version = version
)

fun MetaDatabaseModel.toMetaAppModel() = MetaAppModel(
        httpHeader = httpHeader.toHttpHeaderAppModel(),
        numDays = numDays,
        scheduleGenerator = scheduleGenerator?.toScheduleGeneratorAppModel(),
        subtitle = subtitle,
        timeZoneId = timeZoneName?.let {
            try {
                ZoneId.of(it)
            } catch (_: ZoneRulesException) {
                // TODO Duplicate try/catch, also in MetaValidation.validate.
                // Crashes on re-installation otherwise. Further investigation needed.
                null
            }
        },
        title = title,
        version = version
)

fun MetaNetworkModel.toMetaDatabaseModel() = MetaDatabaseModel(
        httpHeader = httpHeader.toHttpHeaderDatabaseModel(),
        numDays = numDays,
        scheduleGenerator = scheduleGenerator?.toScheduleGeneratorDatabaseModel(),
        subtitle = subtitle,
        timeZoneName = timeZoneName,
        title = title,
        version = version
)
