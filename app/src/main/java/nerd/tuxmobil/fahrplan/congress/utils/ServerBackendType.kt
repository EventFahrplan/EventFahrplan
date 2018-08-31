package nerd.tuxmobil.fahrplan.congress.utils

sealed class ServerBackendType(val name: String) {

    object PENTABARF : ServerBackendType("pentabarf")
    object FRAB : ServerBackendType("frab")
    object PRETALX : ServerBackendType("pretalx")

}
