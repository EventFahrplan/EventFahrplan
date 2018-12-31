package nerd.tuxmobil.fahrplan.congress.utils

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

        private val PRETALX_EVENT = Event("32").apply {
            url = "https://fahrplan.chaos-west.de/35c3chaoswest/talk/KDYQEB"
            slug = "KDYQEB"
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
                .getFeedbackUrl()).isEqualTo("https://fahrplan.chaos-west.de/35c3chaoswest/talk/KDYQEB/feedback/")
    }

}
