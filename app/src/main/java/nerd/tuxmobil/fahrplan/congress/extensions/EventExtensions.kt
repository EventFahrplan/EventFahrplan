package nerd.tuxmobil.fahrplan.congress.extensions

import nerd.tuxmobil.fahrplan.congress.models.Lecture as Event

/**
 * Heuristic to detect whether the event originates from Pretalx.
 * url: https://domain/<slug>/talk/<alphanumeric identifier>
 * subtitle: not supported, therefore empty
 * See: https://github.com/EventFahrplan/EventFahrplan/pull/157
 */
val Event.originatesFromPretalx
    get() = !url.isNullOrEmpty() && !slug.isNullOrEmpty() && url.contains("/talk/") && subtitle.isNullOrEmpty()
