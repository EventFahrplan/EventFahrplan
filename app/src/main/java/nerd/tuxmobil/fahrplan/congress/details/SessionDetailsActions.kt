package nerd.tuxmobil.fahrplan.congress.details

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsToolbarAction.AddToCalendar
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsToolbarAction.Alarm
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsToolbarAction.Favorite
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsToolbarAction.Feedback
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsToolbarAction.Navigate
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsToolbarAction.Share
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsViewEvent.OnAddAlarmWithChecks
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsViewEvent.OnAddFavoriteClick
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsViewEvent.OnAddToCalendarClick
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsViewEvent.OnDeleteAlarmClick
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsViewEvent.OnDeleteFavoriteClick
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsViewEvent.OnNavigateToRoomClick
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsViewEvent.OnOpenFeedbackClick
import nerd.tuxmobil.fahrplan.congress.details.SessionDetailsViewEvent.OnShareClick

internal sealed interface SessionDetailsToolbarAction {

    @get:DrawableRes
    val icon: Int
        get() = when (this) {
            is Favorite -> if (isFavored) R.drawable.ic_star_filled else R.drawable.ic_star_outline
            is Alarm -> if (hasAlarm) R.drawable.ic_bell_filled else R.drawable.ic_bell_outline
            Feedback -> R.drawable.ic_action_feedback
            AddToCalendar -> R.drawable.ic_calendar
            is Share -> R.drawable.ic_share
            Navigate -> R.drawable.ic_action_navigate
        }

    @get:StringRes
    val contentDescription: Int
        get() = when (this) {
            is Favorite -> if (isFavored) R.string.menu_item_title_unflag_as_favorite else R.string.menu_item_title_flag_as_favorite
            is Alarm -> if (hasAlarm) R.string.menu_item_title_delete_alarm else R.string.menu_item_title_set_alarm
            Feedback -> R.string.menu_item_title_feedback
            AddToCalendar -> R.string.menu_item_title_add_to_calendar
            is Share -> R.string.menu_item_title_share_session
            Navigate -> R.string.menu_item_title_navigate
        }

    val viewEvent: SessionDetailsViewEvent?
        get() = when (this) {
            is Favorite -> if (isFavored) OnDeleteFavoriteClick else OnAddFavoriteClick
            is Alarm -> if (hasAlarm) OnDeleteAlarmClick else OnAddAlarmWithChecks
            Feedback -> OnOpenFeedbackClick
            AddToCalendar -> OnAddToCalendarClick
            is Share.Direct -> OnShareClick
            is Share.ChaosflixSubmenu -> null
            Navigate -> OnNavigateToRoomClick
        }

    data class Favorite(val isFavored: Boolean) : SessionDetailsToolbarAction
    data class Alarm(val hasAlarm: Boolean) : SessionDetailsToolbarAction
    data object Feedback : SessionDetailsToolbarAction
    data object AddToCalendar : SessionDetailsToolbarAction
    data object Navigate : SessionDetailsToolbarAction

    sealed interface Share : SessionDetailsToolbarAction {
        data object Direct : Share
        data object ChaosflixSubmenu : Share
    }
}

internal fun createToolbarActions(
    selectedSessionParameter: SelectedSessionParameter,
): List<SessionDetailsToolbarAction> = buildList {
    with(selectedSessionParameter) {
        add(Favorite(isFlaggedAsFavorite))
        add(Alarm(hasAlarm))
        if (supportsFeedback) {
            add(Feedback)
        }
        add(AddToCalendar)
        add(if (supportsChaosflixExport) Share.ChaosflixSubmenu else Share.Direct)
        if (supportsIndoorNavigation) {
            add(Navigate)
        }
    }
}
