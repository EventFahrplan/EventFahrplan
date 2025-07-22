package nerd.tuxmobil.fahrplan.congress.models

data class ScheduleGenerator(
    val name: String,
    val version: String,
) {

    fun isValid() = name.isNotEmpty() && version.isNotEmpty()

}
