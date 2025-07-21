package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.database.models.ScheduleGenerator as ScheduleGeneratorDatabaseModel
import info.metadude.android.eventfahrplan.network.models.ScheduleGenerator as ScheduleGeneratorNetworkModel
import nerd.tuxmobil.fahrplan.congress.models.ScheduleGenerator as ScheduleGeneratorAppModel

fun ScheduleGeneratorAppModel.toScheduleGeneratorNetworkModel() =
    ScheduleGeneratorNetworkModel(
        name = name,
        version = version,
    )

fun ScheduleGeneratorDatabaseModel.toScheduleGeneratorAppModel() = when (name == null || version == null) {
    true -> null
    false -> ScheduleGeneratorAppModel(name = name!!, version = version!!)
}

fun ScheduleGeneratorNetworkModel.toScheduleGeneratorDatabaseModel() =
    ScheduleGeneratorDatabaseModel(
        name = name,
        version = version,
    )
