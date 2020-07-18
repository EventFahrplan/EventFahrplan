@file:JvmName("WikiSessionUtils")

package nerd.tuxmobil.fahrplan.congress.wiki

private val WIKI_LINK_REGEX by lazy {
    // language=regex
    Regex("(.*)(https://events.ccc.de/congress/)(\\d{4})(/wiki/index.php/Session:)(.*)")
}

/**
 * Returns true if the [receiver][this] contains a link to the
 * corresponding wiki session; Otherwise false.
 *
 * Background information: Self-organized sessions are exported
 * from the CCC wiki. The export file then contains regular sessions
 * plus wiki sessions. For wiki sessions the valid link to the wiki
 * is contained in the links whereas for regular sessions the link
 * is generated based on the session id.
 * Export script: https://github.com/voc/schedule
 */
fun String.containsWikiLink(): Boolean = matches(WIKI_LINK_REGEX)
