package nerd.tuxmobil.fahrplan.congress.wiki

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class WikiSessionUtilsTest {

    @Test
    fun `containsWikiLink returns false if string is empty`() {
        assertThat("".containsWikiLink()).isFalse()
    }

    @Test
    fun `containsWikiLink returns true if plain 2017 link is a wiki link`() {
        val wikiLink = "https://events.ccc.de/congress/2017/wiki/index.php/Session:Tor_relays_operators_meetup"
        assertThat(wikiLink.containsWikiLink()).isTrue()
    }

    @Test
    fun `containsWikiLink returns true if plain 2018 link is a wiki link`() {
        val wikiLink = "https://events.ccc.de/congress/2018/wiki/index.php/Session:Advanced_Bondage_Workshop_-_Day_2"
        assertThat(wikiLink.containsWikiLink()).isTrue()
    }

    @Test
    fun `containsWikiLink returns true if HTML link is a wiki link`() {
        val wikiLink = "<a href=\"https://events.ccc.de/congress/2017/wiki/index.php/Session:Tor_relays_operators_meetup\">https://events.ccc.de/congress/2017/wiki/index.php/Session:Tor_relays_operators_meetup</a>"
        assertThat(wikiLink.containsWikiLink()).isTrue()
    }

    @Test
    fun `containsWikiLink returns true if HTML link contains at least one wiki link`() {
        val variousLinks = "<a href=\"https://events.ccc.de/congress/2017/wiki/index.php/Projects:Junghackertag\">https://events.ccc.de/congress/2017/wiki/index.php/Projects:Junghackertag</a><br><a href=\"https://events.ccc.de/congress/2017/wiki/index.php/Session:Koordinierungstreffen_Junghackertag\">https://events.ccc.de/congress/2017/wiki/index.php/Session:Koordinierungstreffen_Junghackertag</a>"
        assertThat(variousLinks.containsWikiLink()).isTrue()
    }

}
