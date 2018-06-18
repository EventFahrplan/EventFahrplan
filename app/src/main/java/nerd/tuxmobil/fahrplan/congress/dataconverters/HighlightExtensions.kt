package nerd.tuxmobil.fahrplan.congress.dataconverters

import nerd.tuxmobil.fahrplan.congress.models.Highlight
import info.metadude.android.eventfahrplan.database.models.Highlight as HighlightDatabaseModel

fun HighlightDatabaseModel.toHighlightAppModel() = Highlight(
        eventId = eventId,
        isHighlight = isHighlight
)
