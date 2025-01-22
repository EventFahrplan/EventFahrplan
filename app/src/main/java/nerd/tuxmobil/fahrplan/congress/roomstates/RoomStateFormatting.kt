package nerd.tuxmobil.fahrplan.congress.roomstates

import info.metadude.kotlin.library.roomstates.base.models.State

interface RoomStateFormatting {
    fun getText(state: State?): String
    fun getFailureText(throwable: Throwable): String
}
