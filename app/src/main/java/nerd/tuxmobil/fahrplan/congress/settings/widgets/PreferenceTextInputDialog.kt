package nerd.tuxmobil.fahrplan.congress.settings.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.ButtonText
import nerd.tuxmobil.fahrplan.congress.designsystem.dialogs.AlertDialog
import nerd.tuxmobil.fahrplan.congress.designsystem.inputs.TextFieldOutlined
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.utils.Validation
import nerd.tuxmobil.fahrplan.congress.utils.Validation.ValidationResult.Error
import nerd.tuxmobil.fahrplan.congress.utils.Validation.ValidationResult.Success
import nerd.tuxmobil.fahrplan.congress.utils.compose.RequestFocusOnLaunch

@Composable
internal fun PreferenceTextInputDialog(
    title: String,
    value: String,
    resetValue: String? = null,
    placeholder: String,
    validator: Validation,
    onValueChanged: (String) -> Unit,
    onReset: (() -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(value))
    }
    var isReset by rememberSaveable { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val trimmedText = textFieldValue.text.trim()

    AlertDialog(
        title = { Text(title) },
        text = {
            RequestFocusOnLaunch(focusRequester)

            Column {
                errorText = if (trimmedText.isBlank()) {
                    null
                } else {
                    when (val validationResult = validator.validate(trimmedText)) {
                        Success -> null
                        is Error -> validationResult.errorMessage
                    }
                }

                TextFieldOutlined(
                    value = textFieldValue,
                    placeholder = { Text(text = placeholder, maxLines = 1) },
                    onValueChange = {
                        textFieldValue = it
                        isReset = false
                    },
                    singleLine = true,
                    isError = errorText != null,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth(),
                )

                errorText?.let { errorText ->
                    Text(
                        text = errorText,
                        color = EventFahrplanTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            Row(modifier = Modifier.fillMaxWidth()) {
                if (resetValue != null && onReset != null) {
                    ButtonText(
                        onClick = {
                            textFieldValue = TextFieldValue(resetValue)
                            isReset = true
                        }
                    ) {
                        Text(stringResource(R.string.preference_dialog_action_reset))
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                ButtonText(onClick = onDismiss) {
                    Text(stringResource(android.R.string.cancel))
                }
                ButtonText(
                    enabled = errorText == null,
                    onClick = {
                        if (isReset || trimmedText == resetValue) {
                            onReset?.invoke()
                        } else {
                            onValueChanged(trimmedText)
                        }
                    },
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            }
        },
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.safeContentPadding(),
    )
}
