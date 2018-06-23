package nerd.tuxmobil.fahrplan.congress.dataconverters

import info.metadude.android.eventfahrplan.database.models.Highlight as HighlightDatabaseModel


fun List<HighlightDatabaseModel>.toHighlightsAppModel() = map(HighlightDatabaseModel::toHighlightAppModel)
