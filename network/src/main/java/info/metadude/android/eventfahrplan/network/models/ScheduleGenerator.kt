package info.metadude.android.eventfahrplan.network.models

import info.metadude.android.eventfahrplan.network.fetching.FetchFahrplan

/**
 * Network model that wraps the name of the schedule generator software and its version.
 * Values can be null if the the "generator" property is not present or its "attributes" are missing.
 * Values in this class are parsed from HTTP responses in [FetchFahrplan].
 */
data class ScheduleGenerator(
    val name: String?,
    val version: String?,
)
