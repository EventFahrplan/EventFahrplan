package nerd.tuxmobil.fahrplan.congress.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import nerd.tuxmobil.fahrplan.congress.BuildConfig
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.settings.widgets.PreferenceTextInputDialog
import nerd.tuxmobil.fahrplan.congress.utils.Validation
import nerd.tuxmobil.fahrplan.congress.utils.Validation.ValidationResult

@Composable
internal fun SocialMediaHashtagsHandlesDialog(
    currentValue: String,
    onValueChanged: (String) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    PreferenceTextInputDialog(
        title = stringResource(R.string.preference_title_social_media_hashtags_handles),
        value = currentValue,
        placeholder = stringResource(R.string.preference_hint_social_media_hashtags_handles),
        validator = NoOpValidator,
        resetValue = BuildConfig.SOCIAL_MEDIA_HASHTAGS_HANDLES,
        onValueChanged = onValueChanged,
        onReset = onReset,
        onDismiss = onDismiss,
    )
}

private object NoOpValidator : Validation {
    override fun validate(input: String) = ValidationResult.Success
}

@PreviewLightDark
@Composable
internal fun SocialMediaHashtagsHandlesDialogPreview() {
    EventFahrplanTheme {
        SocialMediaHashtagsHandlesDialog(
            currentValue = "",
            onValueChanged = {},
            onReset = {},
            onDismiss = {},
        )
    }
}
