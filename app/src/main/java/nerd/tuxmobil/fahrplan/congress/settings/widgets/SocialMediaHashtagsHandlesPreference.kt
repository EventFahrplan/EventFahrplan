package nerd.tuxmobil.fahrplan.congress.settings.widgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent
import nerd.tuxmobil.fahrplan.congress.settings.SettingsEvent.SocialMediaHashtagsHandlesClicked

@Composable
internal fun SocialMediaHashtagsHandlesPreference(
    socialMediaHashtagsHandles: String,
    showDivider: Boolean = false,
    onViewEvent: (SettingsEvent) -> Unit,
) {
    val subtitle = socialMediaHashtagsHandles.ifEmpty {
        stringResource(R.string.preference_summary_social_media_hashtags_handles)
    }

    ClickPreference(
        title = stringResource(R.string.preference_title_social_media_hashtags_handles),
        subtitle = subtitle,
        showDivider = showDivider,
        onClick = { onViewEvent(SocialMediaHashtagsHandlesClicked) },
    )
}
