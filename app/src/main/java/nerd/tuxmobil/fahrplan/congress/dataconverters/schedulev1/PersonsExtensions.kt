package nerd.tuxmobil.fahrplan.congress.dataconverters.schedulev1

import info.metadude.android.eventfahrplan.commons.contracts.Delimiters.SPEAKER_NAMES_DELIMITER
import info.metadude.android.eventfahrplan.commons.extensions.sanitize
import info.metadude.kotlin.library.schedule.v1.models.Person

fun List<Person>.toDelimitedSpeakersString() = this
    .map { person ->
        person
            .publicName
            ?.sanitize()
            ?.takeIf { it.isNotEmpty() }
            ?: person.name.sanitize()
    }
    .filter { it.isNotBlank() }
    .joinToString(SPEAKER_NAMES_DELIMITER)
