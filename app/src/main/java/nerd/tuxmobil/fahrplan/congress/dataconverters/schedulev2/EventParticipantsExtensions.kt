package nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev2

import info.metadude.android.eventfahrplan.commons.contracts.Delimiters.SPEAKER_NAMES_DELIMITER
import info.metadude.android.eventfahrplan.commons.extensions.sanitize
import info.metadude.kotlin.library.schedule.v2.models.Participant

internal fun List<Participant>.toDelimitedSpeakersString() = this
    .map { participant -> participant.name.sanitize() }
    .filter { it.isNotBlank() }
    .joinToString(SPEAKER_NAMES_DELIMITER)
