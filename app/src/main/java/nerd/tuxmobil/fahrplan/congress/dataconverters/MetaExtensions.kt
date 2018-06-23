package nerd.tuxmobil.fahrplan.congress.dataconverters

import nerd.tuxmobil.fahrplan.congress.models.Meta
import info.metadude.android.eventfahrplan.database.models.Meta as MetaDatabaseModel


fun Meta.toMetaDatabaseModel() = MetaDatabaseModel(
        dayChangeHour = dayChangeHour,
        dayChangeMinute = dayChangeMinute,
        eTag = eTag,
        numDays = numDays,
        subtitle = subtitle,
        title = title,
        version = version
)

fun MetaDatabaseModel.toMetaAppModel() = Meta(
        dayChangeHour = dayChangeHour,
        dayChangeMinute = dayChangeMinute,
        eTag = eTag,
        numDays = numDays,
        subtitle = subtitle,
        title = title,
        version = version
)
