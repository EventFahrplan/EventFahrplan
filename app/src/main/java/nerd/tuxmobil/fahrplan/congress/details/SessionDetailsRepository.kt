package nerd.tuxmobil.fahrplan.congress.details

fun interface SessionDetailsRepository {
    fun readUseDeviceTimeZoneEnabled(): Boolean
}
