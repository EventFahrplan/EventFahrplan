package nerd.tuxmobil.fahrplan.congress.models

/**
 * Application model that wraps HTTP header values for the purpose of easily passing them around.
 */
data class HttpHeader(
    val eTag: String = "",
)
