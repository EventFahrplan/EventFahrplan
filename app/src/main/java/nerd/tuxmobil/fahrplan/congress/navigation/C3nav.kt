package nerd.tuxmobil.fahrplan.congress.navigation

import android.net.Uri
import androidx.core.net.toUri
import nerd.tuxmobil.fahrplan.congress.models.Room

/**
 * The c3nav indoor navigation service is available at https://c3nav.de.
 */
class C3nav(
    private val baseUrl: String,
    private val roomForC3navConverter: RoomForC3NavConverter,
) : IndoorNavigation {

    /**
     * Returns `true` if the [baseUrl] is configured and the room supports indoor navigation.
     * Otherwise `false`.
     */
    override fun isSupported(room: Room) = baseUrl.isNotEmpty() &&
            (roomForC3navConverter.convert(room.name).isNotEmpty() || room.identifier.isNotEmpty())

    /**
     * Returns the URI for the given [room] that can be used to send a c3nav specific indoor
     * navigation intent. Mind that the room name part can be missing if the stored room name
     * cannot be converted to a c3nav room name. In this case the base URI is returned. If
     * the base URI is empty then an empty URI is returned.
     */
    override fun getUri(room: Room): Uri {
        val roomPart = room.identifier.ifEmpty {
            roomForC3navConverter.convert(room.name)
        }
        return if (baseUrl.isEmpty()) Uri.EMPTY else "$baseUrl$roomPart".toUri()
    }

}
