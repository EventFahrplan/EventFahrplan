package nerd.tuxmobil.fahrplan.congress.extensions

import nerd.tuxmobil.fahrplan.congress.models.Lecture as Event

val Event.originatesFromPretalx
    get() = !url.isNullOrEmpty() && !slug.isNullOrEmpty() && url.endsWith(slug)
