package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.extensions.WIKI_SESSION_TRACK_NAME
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FeedbackUrlComposerTest {

    private companion object {

        const val FRAB_SCHEDULE_FEEDBACK_URL = "https://frab.cccv.de/en/35C3/public/events/%s/feedback/new"

        val HUB_PRETALX_SESSION = Session("3723").apply {
            url = "https://fahrplan.events.ccc.de/congress/2023/hub/events/37c3_opening.html"
            slug = "37c3_opening"
            feedbackUrl = "https://talks.c3voc.de/2023/talk/7B2KMD/feedback/"
        }

        val HUB_SOS_SESSION = Session("3724").apply {
            url = "https://fahrplan.events.ccc.de/congress/2023/hub/events/lockpicking_workshop.html"
            slug = "lockpicking_workshop"
            feedbackUrl = ""
        }

        val FRAB_SESSION = Session("9985").apply {
            url = "https://fahrplan.events.ccc.de/congress/2018/Fahrplan/events/9985.html"
            slug = "35c3-9985-opening_ceremony"
            feedbackUrl = null
        }

        val PRETALX_SESSION = Session("202").apply {
            url = "https://talks.mrmcd.net/2019/talk/9XL7SP/"
            slug = "2019-202-board-games-of-medieval-europe"
            feedbackUrl = null
        }

        val WIKI_SESSION = Session("1346").apply {
            track = WIKI_SESSION_TRACK_NAME
            url = "https://events.ccc.de/congress/2019/wiki/index.php/Session:Mobile_Apps"
            feedbackUrl = null
        }

    }

    @Test
    fun `getFeedbackUrl returns valid string for Hub Pretalx session`() {
        assertThat(FeedbackUrlComposer("")
                .getFeedbackUrl(HUB_PRETALX_SESSION)).isEqualTo("https://talks.c3voc.de/2023/talk/7B2KMD/feedback/")
    }

    @Test
    fun `getFeedbackUrl returns empty string for Hub SOS session`() {
        assertThat(FeedbackUrlComposer("")
            .getFeedbackUrl(HUB_SOS_SESSION)).isEmpty()
    }

    @Test
    fun `getFeedbackUrl returns empty string for Frab session if schedule feedback URL is missing`() {
        assertThat(FeedbackUrlComposer("")
                .getFeedbackUrl(FRAB_SESSION)).isEmpty()
    }

    @Test
    fun `getFeedbackUrl returns valid feedback URL for Frab session if schedule feedback URL is present`() {
        assertThat(FeedbackUrlComposer(FRAB_SCHEDULE_FEEDBACK_URL)
                .getFeedbackUrl(FRAB_SESSION)).isEqualTo("https://frab.cccv.de/en/35C3/public/events/9985/feedback/new")
    }

    @Test
    fun `getFeedbackUrl returns valid feedback URL for Pretalx session if schedule feedback URL is present`() {
        assertThat(FeedbackUrlComposer(FRAB_SCHEDULE_FEEDBACK_URL)
                .getFeedbackUrl(PRETALX_SESSION)).isEqualTo("https://talks.mrmcd.net/2019/talk/9XL7SP/feedback/")
    }

    @Test
    fun `getFeedbackUrl returns empty string for wiki session`() {
        assertThat(FeedbackUrlComposer("")
                .getFeedbackUrl(WIKI_SESSION)).isEmpty()
    }

}
