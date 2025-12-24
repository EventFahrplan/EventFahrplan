package nerd.tuxmobil.fahrplan.congress.details

sealed interface SessionDetailsViewEvent {
    data class OnSessionLinkClick(val link: String) : SessionDetailsViewEvent
}
