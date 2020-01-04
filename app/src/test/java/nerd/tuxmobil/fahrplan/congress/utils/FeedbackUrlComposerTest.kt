package nerd.tuxmobil.fahrplan.congress.utils

import nerd.tuxmobil.fahrplan.congress.extensions.WIKI_EVENT_TRACK_NAME
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import nerd.tuxmobil.fahrplan.congress.models.Lecture as Event

class FeedbackUrlComposerTest {

    companion object {

        private const val FRAB_SCHEDULE_FEEDBACK_URL = "https://frab.cccv.de/en/35C3/public/events/%s/feedback/new"

        private val FRAB_EVENT = Event("9985").apply {
            url = "https://fahrplan.events.ccc.de/congress/2018/Fahrplan/events/9985.html"
            slug = "35c3-9985-opening_ceremony"
        }

        private val PRETALX_EVENT = Event("202").apply {
            url = "https://talks.mrmcd.net/2019/talk/9XL7SP/"
            slug = "2019-202-board-games-of-medieval-europe"
        }

        private val WIKI_EVENT = Event("1346").apply {
            track = WIKI_EVENT_TRACK_NAME
            url = "https://events.ccc.de/congress/2019/wiki/index.php/Session:Mobile_Apps"
        }

    }

    @Test
    fun getFeedbackUrlWithFrabEventWithoutScheduleFeedbackUrl() {
        assertThat(FeedbackUrlComposer(FRAB_EVENT, "")
                .getFeedbackUrl()).isEmpty()
    }

    @Test
    fun getFeedbackUrlWithFrabEventWithFrabScheduleFeedbackUrl() {
        assertThat(FeedbackUrlComposer(FRAB_EVENT, FRAB_SCHEDULE_FEEDBACK_URL)
                .getFeedbackUrl()).isEqualTo("https://frab.cccv.de/en/35C3/public/events/9985/feedback/new")
    }

    @Test
    fun getFeedbackUrlWithPretalxEventWithFrabScheduleFeedbackUrl() {
        assertThat(FeedbackUrlComposer(PRETALX_EVENT, FRAB_SCHEDULE_FEEDBACK_URL)
                .getFeedbackUrl()).isEqualTo("https://talks.mrmcd.net/2019/talk/9XL7SP/feedback/")
    }

    @Test
    fun getFeedbackUrlWithWikiEvent() {
        assertThat(FeedbackUrlComposer(WIKI_EVENT, "")
                .getFeedbackUrl()).isEqualTo("")
    }

}
