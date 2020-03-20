package nerd.tuxmobil.fahrplan.congress.dataconverters

import nerd.tuxmobil.fahrplan.congress.models.Meta
import info.metadude.android.eventfahrplan.database.models.Meta as MetaDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Meta as MetaNetworkModel


fun Meta.toMetaDatabaseModel() = MetaDatabaseModel(
        eTag = eTag,
        numDays = numDays,
        subtitle = subtitle,
        title = title,
        version = version
)

fun Meta.toMetaNetworkModel() = MetaNetworkModel(
        eTag = eTag,
        numDays = numDays,
        subtitle = subtitle,
        title = title,
        version = version
)

fun MetaDatabaseModel.toMetaAppModel() = Meta(
        eTag = eTag,
        numDays = numDays,
        subtitle = subtitle,
        title = title,
        version = version
)

fun MetaNetworkModel.toMetaAppModel() = Meta(
        eTag = eTag,
        numDays = numDays,
        subtitle = subtitle,
        title = title,
        version = version
)
