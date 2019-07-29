package nerd.tuxmobil.fahrplan.congress.dataconverters

import nerd.tuxmobil.fahrplan.congress.models.Lecture
import info.metadude.android.eventfahrplan.database.models.Lecture as LectureDatabaseModel
import info.metadude.android.eventfahrplan.network.models.Lecture as LectureNetworkModel


fun List<Lecture>.toDateInfos() = map(Lecture::toDateInfo)

fun List<Lecture>.toLecturesDatabaseModel() = map(Lecture::toLectureDatabaseModel)

fun List<LectureNetworkModel>.toLecturesAppModel2(): List<Lecture> = map(LectureNetworkModel::toLectureAppModel)

fun List<LectureDatabaseModel>.toLecturesAppModel() = map(LectureDatabaseModel::toLectureAppModel)

fun List<Lecture>.sanitize(): List<Lecture> = map(Lecture::sanitize)
