package nerd.tuxmobil.fahrplan.congress.net.errors

import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.window.DialogProperties
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.ButtonText
import nerd.tuxmobil.fahrplan.congress.designsystem.dialogs.AlertDialog
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.net.errors.ErrorMessage.TitledMessage

@Composable
internal fun ErrorMessageScreen(
    errorMessage: TitledMessage,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = { Text(errorMessage.title) },
        text = { Text(errorMessage.message) },
        confirmButton = {
            ButtonText(onConfirm) {
                Text(stringResource(R.string.OK))
            }
        },
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.safeContentPadding(),
    )
}

@PreviewLightDark
@Composable
private fun ErrorMessageScreenTimeoutPreview() {
    EventFahrplanTheme {
        ErrorMessageScreen(
            errorMessage = TitledMessage(
                title = stringResource(R.string.dlg_err_connection_failed),
                message = stringResource(R.string.dlg_err_failed_timeout),
            ),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
