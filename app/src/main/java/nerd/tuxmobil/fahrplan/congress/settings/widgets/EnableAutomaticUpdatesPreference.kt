package nerd.tuxmobil.fahrplan.congress.settings.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.models.NextFetch
import nerd.tuxmobil.fahrplan.congress.settings.NextFetchFormatter
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent
import nerd.tuxmobil.fahrplan.congress.settings.SettingsViewEvent.AutoUpdateClicked

@Composable
internal fun EnableAutomaticUpdatesPreference(
    isAutoUpdateEnabled: Boolean,
    nextFetch: NextFetch,
    showDivider: Boolean = false,
    onViewEvent: (SettingsViewEvent) -> Unit,
) {
    val context = LocalContext.current
    val nextFetchFormatter = remember(context) { NextFetchFormatter.newInstance(context) }

    val subtitle = if (isAutoUpdateEnabled) {
        nextFetchFormatter.format(nextFetch)
    } else {
        stringResource(R.string.preference_summary_auto_update_enabled)
    }

    SwitchPreference(
        title = stringResource(R.string.preference_title_auto_update_enabled),
        subtitle = subtitle,
        checked = isAutoUpdateEnabled,
        showDivider = showDivider,
        onCheckedChange = { onViewEvent(AutoUpdateClicked) },
    )
}
