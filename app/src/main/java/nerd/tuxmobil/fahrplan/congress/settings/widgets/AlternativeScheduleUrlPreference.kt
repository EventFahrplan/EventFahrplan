package nerd.tuxmobil.fahrplan.congress.settings.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.AlternativeScheduleUrlClicked

@Composable
internal fun AlternativeScheduleUrlPreference(
    alternativeScheduleUrl: String,
    showDivider: Boolean = false,
    onViewEvent: (SettingsViewEvent) -> Unit,
) {
    val subtitle = alternativeScheduleUrl.ifEmpty {
        stringResource(R.string.preference_summary_alternative_schedule_url)
    }

    ClickPreference(
        title = stringResource(R.string.preference_title_alternative_schedule_url),
        subtitle = subtitle,
        showDivider = showDivider,
        onClick = { onViewEvent(AlternativeScheduleUrlClicked) },
    )
}
