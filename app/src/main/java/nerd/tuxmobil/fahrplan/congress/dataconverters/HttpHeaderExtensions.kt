package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.database.models.HttpHeader as HttpHeaderDatabaseModel
import info.metadude.android.eventfahrplan.network.models.HttpHeader as HttpHeaderNetworkModel
import nerd.tuxmobil.fahrplan.congress.models.HttpHeader as HttpHeaderAppModel

fun HttpHeaderAppModel.toHttpHeaderNetworkModel() = HttpHeaderNetworkModel(
    eTag = eTag,
    lastModified = lastModified,
)

fun HttpHeaderDatabaseModel.toHttpHeaderAppModel() = HttpHeaderAppModel(
    eTag = eTag,
    lastModified = lastModified,
)

fun HttpHeaderNetworkModel.toHttpHeaderDatabaseModel() = HttpHeaderDatabaseModel(
    eTag = eTag,
    lastModified = lastModified,
)
