package nerd.tuxmobil.fahrplan.congress.settings.widgets

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import kotlinx.collections.immutable.ImmutableMap
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.ButtonText
import nerd.tuxmobil.fahrplan.congress.designsystem.buttons.RadioButton
import nerd.tuxmobil.fahrplan.congress.designsystem.dialogs.AlertDialog
import nerd.tuxmobil.fahrplan.congress.designsystem.modifiers.minimumInteractiveComponentSize
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
internal fun <T> PreferenceListDialog(
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
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        onClick = { onOptionSelected(value) },
                        role = Role.RadioButton,
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
