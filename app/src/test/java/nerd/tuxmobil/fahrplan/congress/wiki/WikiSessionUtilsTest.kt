package nerd.tuxmobil.fahrplan.congress.wiki

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class WikiSessionUtilsTest {

    @Test
    fun linksContainWikiLinkWithEmptyString() {
        assertThat("".containsWikiLink()).isFalse
    }

    @Test
    fun linksContainWikiLinkWithSimple2017WikiLink() {
        val wikiLink = "https://events.ccc.de/congress/2017/wiki/index.php/Session:Tor_relays_operators_meetup"
        assertThat(wikiLink.containsWikiLink()).isTrue
    }

    @Test
    fun linksContainWikiLinkWithSimple2018WikiLink() {
        val wikiLink = "https://events.ccc.de/congress/2018/wiki/index.php/Session:Advanced_Bondage_Workshop_-_Day_2"
        assertThat(wikiLink.containsWikiLink()).isTrue
    }

    @Test
    fun linksContainWikiLinkWithWikiHtmlLink() {
        val wikiLink = "<a href=\"https://events.ccc.de/congress/2017/wiki/index.php/Session:Tor_relays_operators_meetup\">https://events.ccc.de/congress/2017/wiki/index.php/Session:Tor_relays_operators_meetup</a>"
        assertThat(wikiLink.containsWikiLink()).isTrue
    }

    @Test
    fun linksContainWikiLinkWithVariousHtmlLinks() {
        val variousLinks = "<a href=\"https://events.ccc.de/congress/2017/wiki/index.php/Projects:Junghackertag\">https://events.ccc.de/congress/2017/wiki/index.php/Projects:Junghackertag</a><br><a href=\"https://events.ccc.de/congress/2017/wiki/index.php/Session:Koordinierungstreffen_Junghackertag\">https://events.ccc.de/congress/2017/wiki/index.php/Session:Koordinierungstreffen_Junghackertag</a>"
        assertThat(variousLinks.containsWikiLink()).isTrue
    }

}
