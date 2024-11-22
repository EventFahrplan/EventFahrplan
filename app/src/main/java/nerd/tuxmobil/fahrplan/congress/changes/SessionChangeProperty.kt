package nerd.tuxmobil.fahrplan.congress.changes

import androidx.annotation.ColorRes
import nerd.tuxmobil.fahrplan.congress.R

data class SessionChangeProperty<T>(
    val value: T,
    val contentDescription: String,
    val changeState: ChangeState,
) {

    enum class ChangeState(@ColorRes val colorOnLight: Int, @ColorRes val colorOnDark: Int) {
        UNCHANGED(R.color.session_list_item_text_inverted, R.color.session_list_item_text),
        NEW(R.color.schedule_change_new, R.color.schedule_change_new),
        CANCELED(R.color.schedule_change_canceled, R.color.schedule_change_canceled),
        CHANGED(R.color.schedule_change, R.color.schedule_change_canceled),
    }

}
