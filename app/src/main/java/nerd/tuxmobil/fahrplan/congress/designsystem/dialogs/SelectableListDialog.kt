package nerd.tuxmobil.fahrplan.congress.designsystem.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role.Companion.RadioButton
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.collections.immutable.ImmutableMap
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.getAlarmTimeEntries
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.ButtonText
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.RadioButton
import nerd.tuxmobil.fahrplan.congress.designsystem.modifiers.minimumInteractiveComponentSize
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
internal fun <T> SelectableListDialog(
    title: String,
    entries: ImmutableMap<T, String>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = { Text(title) },
        text = {
            RadioButtonGroup(entries, selectedOption, onOptionSelected)
        },
        confirmButton = {
            ButtonText(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.safeContentPadding(),
    )
}

@Composable
private fun <T> RadioButtonGroup(
    entries: ImmutableMap<T, String>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
) {
    Column(
        modifier = Modifier
            .selectableGroup()
            .verticalScroll(rememberScrollState())
    ) {
        entries.forEach { (value, description) ->
            val isSelected = value == selectedOption
            Row(
                verticalAlignment = CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        onClick = { onOptionSelected(value) },
                        role = RadioButton,
                    )
                    .minimumInteractiveComponentSize()
                    .padding(horizontal = 16.dp),
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null,
                )
                Text(
                    text = description,
                    style = EventFahrplanTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ChooseAlarmTimeSelectableListDialogPreview() {
    EventFahrplanTheme {
        val context = LocalContext.current
        val entries = remember(context) { getAlarmTimeEntries(context) }
        SelectableListDialog(
            title = stringResource(R.string.choose_alarm_time),
            entries = entries,
            selectedOption = 15,
            onOptionSelected = {},
            onDismiss = {},
        )
    }
}
