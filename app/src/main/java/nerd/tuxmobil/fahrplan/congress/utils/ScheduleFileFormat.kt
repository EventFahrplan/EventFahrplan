package nerd.tuxmobil.fahrplan.congress.utils

import androidx.annotation.VisibleForTesting

enum class ScheduleFileFormat(
    val value: String,
) {

    SCHEDULE_V1_XML("schedule_v1_xml"),
    SCHEDULE_V1_JSON("schedule_v1_json");

    companion object {
        fun of(name: String) = entries.firstOrNull { it.value == name }
            ?: throw UnknownScheduleFileFormatException(name)
    }

}

@VisibleForTesting
internal class UnknownScheduleFileFormatException(name: String) : IllegalArgumentException("""Unknown schedule file format: "$name".""")
