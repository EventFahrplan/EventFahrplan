package nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev2

import info.metadude.android.eventfahrplan.commons.extensions.sanitize
import info.metadude.android.eventfahrplan.network.models.HttpHeader
import info.metadude.android.eventfahrplan.network.models.Meta
import info.metadude.android.eventfahrplan.network.models.ScheduleGenerator
import info.metadude.kotlin.library.schedule.v2.models.ScheduleV2

internal fun ScheduleV2.toMetaNetworkModel(
    responseETag: String,
    responseLastModifiedAt: String,
) = Meta(
    scheduleGenerator = ScheduleGenerator(
        name = generator?.name?.sanitize(),
        version = generator?.version?.sanitize(),
    ),
    httpHeader = HttpHeader(
        eTag = responseETag,
        lastModified = responseLastModifiedAt,
    ),
    numDays = schedule.conference.days.size,
    title = schedule.conference.title.sanitize(),
    timeZoneName = schedule.conference.timeZone?.name?.id,
    version = schedule.version.sanitize(),
)
