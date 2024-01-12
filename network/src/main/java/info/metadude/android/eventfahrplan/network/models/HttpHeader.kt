package info.metadude.android.eventfahrplan.network.models

import info.metadude.android.eventfahrplan.network.fetching.FetchFahrplan

/**
 * Network model that wraps HTTP header values for the purpose of easily passing them around.
 * Values in this class are parsed from HTTP responses in [FetchFahrplan].
 */
data class HttpHeader(
    val eTag: String = "",
)
