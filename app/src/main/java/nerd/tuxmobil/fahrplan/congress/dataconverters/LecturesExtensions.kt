package nerd.tuxmobil.fahrplan.congress.dataconverters

import nerd.tuxmobil.fahrplan.congress.models.Lecture
import info.metadude.android.eventfahrplan.database.models.Lecture as LectureDatabaseModel


fun List<Lecture>.toDateInfos() = map(Lecture::toDateInfo)

fun List<Lecture>.toLecturesDatabaseModel() = map(Lecture::toLectureDatabaseModel)

fun List<LectureDatabaseModel>.toLecturesAppModel() = map(LectureDatabaseModel::toLectureAppModel)
