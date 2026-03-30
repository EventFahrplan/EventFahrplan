package nerd.tuxmobil.fahrplan.congress.utils

import androidx.annotation.VisibleForTesting

enum class ScheduleFileFormat(
    val value: String,
) {

    ScheduleV1Xml("schedule_v1_xml");

    companion object {
        fun of(name: String) = entries.firstOrNull { it.value == name }
            ?: throw UnknownScheduleFileFormatException(name)
    }

}

@VisibleForTesting
internal class UnknownScheduleFileFormatException(name: String) : IllegalArgumentException("""Unknown schedule file format: "$name".""")
