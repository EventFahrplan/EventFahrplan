package nerd.tuxmobil.fahrplan.congress.settings.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.AlternativeScheduleUrlClicked

@Composable
internal fun AlternativeScheduleUrlPreference(
    alternativeScheduleUrl: String?,
    onViewEvent: (SettingsEvent) -> Unit,
) {
    val subtitle = if (alternativeScheduleUrl.isNullOrEmpty()) {
        stringResource(R.string.preference_summary_alternative_schedule_url)
    } else {
        alternativeScheduleUrl
    }

    ClickPreference(
        title = stringResource(R.string.preference_title_alternative_schedule_url),
        subtitle = subtitle,
        onClick = { onViewEvent(AlternativeScheduleUrlClicked) },
    )
}
