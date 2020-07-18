package nerd.tuxmobil.fahrplan.congress.extensions

import nerd.tuxmobil.fahrplan.congress.models.Session

/**
 * Heuristic to detect whether the session originates from Pretalx.
 * url: https://domain/<slug>/talk/<alphanumeric identifier>
 * subtitle: not supported, therefore empty
 * See: https://github.com/EventFahrplan/EventFahrplan/pull/157
 */
val Session.originatesFromPretalx
    get() = !url.isNullOrEmpty() && !slug.isNullOrEmpty() && url.contains("/talk/") && subtitle.isNullOrEmpty()

// The track name constant must match the "track" name in the schedule.xml!
const val WIKI_SESSION_TRACK_NAME = "self organized sessions"

val Session.originatesFromWiki
    get() = WIKI_SESSION_TRACK_NAME == track
