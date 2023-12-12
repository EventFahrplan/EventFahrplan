package nerd.tuxmobil.fahrplan.congress.navigation

import android.net.Uri
import nerd.tuxmobil.fahrplan.congress.models.Room

interface IndoorNavigation {
    fun isSupported(room: Room): Boolean
    fun getUri(room: Room): Uri
}
