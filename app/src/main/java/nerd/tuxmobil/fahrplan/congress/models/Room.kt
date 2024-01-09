package nerd.tuxmobil.fahrplan.congress.models

/**
 * Encapsulates all information about a virtual or physical room.
 * Gradually to be used as an abstraction for the app layer.
 */
data class Room(
    val identifier: String,
    val name: String,
)
