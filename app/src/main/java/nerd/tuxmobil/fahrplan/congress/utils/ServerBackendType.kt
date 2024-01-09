package nerd.tuxmobil.fahrplan.congress.utils

sealed class ServerBackendType(val name: String) {

    data object PENTABARF : ServerBackendType("pentabarf")
    data object FRAB : ServerBackendType("frab")
    data object PRETALX : ServerBackendType("pretalx")

}
