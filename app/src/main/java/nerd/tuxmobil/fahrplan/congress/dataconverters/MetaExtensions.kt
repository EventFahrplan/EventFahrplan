package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.database.models.Meta as MetaDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Meta as MetaNetworkModel
import nerd.tuxmobil.fahrplan.congress.models.Meta as MetaAppModel


fun MetaAppModel.toMetaNetworkModel() = MetaNetworkModel(
        eTag = eTag,
        numDays = numDays,
        subtitle = subtitle,
        title = title,
        version = version
)

fun MetaDatabaseModel.toMetaAppModel() = MetaAppModel(
        eTag = eTag,
        numDays = numDays,
        subtitle = subtitle,
        title = title,
        version = version
)

fun MetaNetworkModel.toMetaDatabaseModel() = MetaDatabaseModel(
        eTag = eTag,
        numDays = numDays,
        subtitle = subtitle,
        title = title,
        version = version
)
