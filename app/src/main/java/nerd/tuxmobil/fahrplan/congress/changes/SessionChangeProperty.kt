package nerd.tuxmobil.fahrplan.congress.changes

import androidx.annotation.ColorRes
import nerd.tuxmobil.fahrplan.congress.R

data class SessionChangeProperty<T>(
    val value: T,
    val contentDescription: String,
    val changeState: ChangeState,
) {

    // TODO Move into theme
    enum class ChangeState(@param:ColorRes val colorOnLight: Int, @param:ColorRes val colorOnDark: Int) {
        UNCHANGED(R.color.session_list_item_text_inverted, R.color.session_list_item_text),
        NEW(R.color.schedule_change_new_on_light, R.color.schedule_change_new_on_dark),
        CANCELED(R.color.schedule_change_canceled_on_light, R.color.schedule_change_canceled_on_dark),
        CHANGED(R.color.schedule_change_on_light, R.color.schedule_change_canceled_on_dark),
    }

}
