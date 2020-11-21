package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.extensions.WIKI_SESSION_TRACK_NAME
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FeedbackUrlComposerTest {

    private companion object {

        const val FRAB_SCHEDULE_FEEDBACK_URL = "https://frab.cccv.de/en/35C3/public/events/%s/feedback/new"

        val FRAB_SESSION = Session("9985").apply {
            url = "https://fahrplan.events.ccc.de/congress/2018/Fahrplan/events/9985.html"
            slug = "35c3-9985-opening_ceremony"
        }

        val PRETALX_SESSION = Session("202").apply {
            url = "https://talks.mrmcd.net/2019/talk/9XL7SP/"
            slug = "2019-202-board-games-of-medieval-europe"
        }

        val WIKI_SESSION = Session("1346").apply {
            track = WIKI_SESSION_TRACK_NAME
            url = "https://events.ccc.de/congress/2019/wiki/index.php/Session:Mobile_Apps"
        }

    }

    @Test
    fun getFeedbackUrlWithFrabSessionWithoutScheduleFeedbackUrl() {
        assertThat(FeedbackUrlComposer(FRAB_SESSION, "")
                .getFeedbackUrl()).isEmpty()
    }

    @Test
    fun getFeedbackUrlWithFrabSessionWithFrabScheduleFeedbackUrl() {
        assertThat(FeedbackUrlComposer(FRAB_SESSION, FRAB_SCHEDULE_FEEDBACK_URL)
                .getFeedbackUrl()).isEqualTo("https://frab.cccv.de/en/35C3/public/events/9985/feedback/new")
    }

    @Test
    fun getFeedbackUrlWithPretalxSessionWithFrabScheduleFeedbackUrl() {
        assertThat(FeedbackUrlComposer(PRETALX_SESSION, FRAB_SCHEDULE_FEEDBACK_URL)
                .getFeedbackUrl()).isEqualTo("https://talks.mrmcd.net/2019/talk/9XL7SP/feedback/")
    }

    @Test
    fun getFeedbackUrlWithWikiSession() {
        assertThat(FeedbackUrlComposer(WIKI_SESSION, "")
                .getFeedbackUrl()).isEqualTo("")
    }

}
