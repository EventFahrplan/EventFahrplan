package nerd.tuxmobil.fahrplan.congress.about

sealed interface AboutViewEvent {

    data class OnPostalAddressClick(val textualAddress: String): AboutViewEvent

}
