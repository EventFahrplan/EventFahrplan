package nerd.tuxmobil.fahrplan.congress.wiki

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class WikiEventUtilsTest {

    @Test
    fun linksContainWikiLinkWithEmptyString() {
        assertThat(WikiEventUtils.linksContainWikiLink("")).isFalse()
    }

    @Test
    fun linksContainWikiLinkWithSimple2017WikiLink() {
        val wikiLink = "https://events.ccc.de/congress/2017/wiki/index.php/Session:Tor_relays_operators_meetup"
        assertThat(WikiEventUtils.linksContainWikiLink(wikiLink)).isTrue()
    }

    @Test
    fun linksContainWikiLinkWithWikiHtmlLink() {
        val wikiLink = "<a href=\"https://events.ccc.de/congress/2017/wiki/index.php/Session:Tor_relays_operators_meetup\">https://events.ccc.de/congress/2017/wiki/index.php/Session:Tor_relays_operators_meetup</a>"
        assertThat(WikiEventUtils.linksContainWikiLink(wikiLink)).isTrue()
    }

    @Test
    fun linksContainWikiLinkWithVariousHtmlLinks() {
        val variousLinks = "<a href=\"https://events.ccc.de/congress/2017/wiki/index.php/Projects:Junghackertag\">https://events.ccc.de/congress/2017/wiki/index.php/Projects:Junghackertag</a><br><a href=\"https://events.ccc.de/congress/2017/wiki/index.php/Session:Koordinierungstreffen_Junghackertag\">https://events.ccc.de/congress/2017/wiki/index.php/Session:Koordinierungstreffen_Junghackertag</a>"
        assertThat(WikiEventUtils.linksContainWikiLink(variousLinks)).isTrue()
    }

}
