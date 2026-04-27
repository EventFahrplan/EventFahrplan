package nerd.tuxmobil.fahrplan.congress.details

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsToolbarAction.AddToCalendar
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsToolbarAction.Alarm
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsToolbarAction.Favorite
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsToolbarAction.Feedback
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsToolbarAction.Navigate
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsToolbarAction.Share
import org.junit.jupiter.api.Test

class SessionDetailsActionsCreateToolbarActionsTest {

    private val default = SelectedSessionParameter()

    @Test
    fun `createToolbarActions matches default action set and order`() {
        val actions = createToolbarActions(default)
        assertThat(actions).containsExactly(
            Favorite(isFavored = false),
            Alarm(hasAlarm = false),
            AddToCalendar,
            Share.Direct,
        ).inOrder()
    }

    @Test
    fun `createToolbarActions adds feedback when supported`() {
        val actions = createToolbarActions(default.copy(supportsFeedback = true))
        assertThat(actions).contains(Feedback)
    }

    @Test
    fun `createToolbarActions adds ChaosflixSubmenu when supported`() {
        val actions = createToolbarActions(default.copy(supportsChaosflixExport = true))
        assertThat(actions).contains(Share.ChaosflixSubmenu)
        assertThat(actions).doesNotContain(Share.Direct)
    }

    @Test
    fun `createToolbarActions uses ChaosflixSubmenu not Direct when all optional flags are true`() {
        val actions = createToolbarActions(
            default.copy(
                supportsFeedback = true,
                supportsChaosflixExport = true,
                supportsIndoorNavigation = true,
            ),
        )
        assertThat(actions).containsExactly(
            Favorite(isFavored = false),
            Alarm(hasAlarm = false),
            Feedback,
            AddToCalendar,
            Share.ChaosflixSubmenu,
            Navigate,
        ).inOrder()
        assertThat(actions).doesNotContain(Share.Direct)
    }

    @Test
    fun `createToolbarActions adds navigate when supported`() {
        val actions = createToolbarActions(default.copy(supportsIndoorNavigation = true))
        assertThat(actions).contains(Navigate)
    }

}
