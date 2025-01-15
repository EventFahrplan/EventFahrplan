package nerd.tuxmobil.fahrplan.congress.roomstates

import info.metadude.kotlin.library.roomstates.base.models.State
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving

class RoomStateFormatter(private val resourceResolving: ResourceResolving) : RoomStateFormatting {

    override fun getText(state: State?) = when (state) {
        null, State.UNKNOWN -> resourceResolving.getString(R.string.room_state_text, resourceResolving.getString(R.string.room_state_unknown))
        State.EMPTY -> resourceResolving.getString(R.string.room_state_text, resourceResolving.getString(R.string.room_state_empty))
        State.FULL -> resourceResolving.getString(R.string.room_state_text, resourceResolving.getString(R.string.room_state_full))
        State.TOO_FULL -> resourceResolving.getString(R.string.room_state_text, resourceResolving.getString(R.string.room_state_too_full))
    }

    override fun getFailureText(throwable: Throwable): String {
        return if ("HTTP 4" in "$throwable" || "HTTP 5" in "$throwable") {
            resourceResolving.getString(R.string.room_state_text, resourceResolving.getString(R.string.room_state_not_retrievable))
        } else {
            getText(null)
        }
    }
}
