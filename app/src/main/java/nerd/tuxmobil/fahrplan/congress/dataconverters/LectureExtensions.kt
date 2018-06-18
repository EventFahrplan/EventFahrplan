package nerd.tuxmobil.fahrplan.congress.dataconverters

import nerd.tuxmobil.fahrplan.congress.models.Lecture
import info.metadude.android.eventfahrplan.database.models.Highlight as HighlightDatabaseModel

fun Lecture.toHighlightDatabaseModel() = HighlightDatabaseModel(
        eventId = Integer.parseInt(lecture_id),
        isHighlight = highlight
)
