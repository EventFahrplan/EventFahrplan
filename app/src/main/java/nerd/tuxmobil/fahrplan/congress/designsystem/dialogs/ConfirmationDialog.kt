package nerd.tuxmobil.fahrplan.congress.designsystem.dialogs

import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.window.DialogProperties
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.ButtonText
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
fun ConfirmationDialog(
    title: String,
    confirmationButtonText: String,
    dismissButtonText: String = stringResource(android.R.string.cancel),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = { Text(title) },
        confirmButton = {
            ButtonText(onConfirm) {
                Text(confirmationButtonText)
            }
        },
        dismissButton = {
            ButtonText(onDismiss) {
                Text(dismissButtonText)
            }
        },
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.safeContentPadding(),
    )
}

@PreviewLightDark
@Composable
private fun ConfirmationDialogPreview() {
    EventFahrplanTheme {
        ConfirmationDialog(
            title = stringResource(R.string.dlg_delete_all_favorites),
            confirmationButtonText = stringResource(R.string.dlg_delete_all_favorites_delete_all),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
