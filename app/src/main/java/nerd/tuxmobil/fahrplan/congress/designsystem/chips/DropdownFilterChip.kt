package nerd.tuxmobil.fahrplan.congress.designsystem.chips

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChipDefaults.IconSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconArrowDropDown
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconCheck
import nerd.tuxmobil.fahrplan.congress.designsystem.menues.DropdownMenu
import nerd.tuxmobil.fahrplan.congress.designsystem.menues.DropdownMenuItem
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

data class DropdownFilterChipOption<T>(
    val key: T,
    val label: String,
    val selected: Boolean,
)

@Composable
fun <T> DropdownFilterChip(
    selected: Boolean,
    label: @Composable () -> Unit,
    options: ImmutableList<DropdownFilterChipOption<T>>,
    onOptionClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FilterChip(
            onClick = { expanded = !expanded },
            label = label,
            selected = selected,
            enabled = enabled,
            trailingIcon = {
                IconArrowDropDown(Modifier.size(IconSize))
            },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            for ((key, label, selected) in options) {
                DropdownMenuItem(
                    modifier = Modifier.semantics { this.selected = selected },
                    text = { DropdownFilterChipMenuItemContent(label, selected) },
                    onClick = { onOptionClick(key) },
                )
            }
        }
    }
}

@Composable
private fun DropdownFilterChipMenuItemContent(
    label: String,
    selected: Boolean,
) {
    Row(
        horizontalArrangement = spacedBy(8.dp),
        verticalAlignment = CenterVertically,
    ) {
        if (selected) {
            IconCheck(modifier = Modifier.size(18.dp))
        } else {
            Box(modifier = Modifier.size(18.dp))
        }
        Text(label)
    }
}

@PreviewLightDark
@Composable
private fun DropdownFilterChipEnabledSelectedPreview() {
    DropdownFilterChip(enabled = true, selected = true)
}

@PreviewLightDark
@Composable
private fun DropdownFilterChipDisabledSelectedPreview() {
    DropdownFilterChip(enabled = false, selected = true)
}

@PreviewLightDark
@Composable
private fun DropdownFilterChipEnabledUnselectedPreview() {
    DropdownFilterChip(enabled = true, selected = false)
}

@PreviewLightDark
@Composable
private fun DropdownFilterChipDisabledUnselectedPreview() {
    DropdownFilterChip(enabled = false, selected = false)
}

@Composable
private fun DropdownFilterChip(enabled: Boolean, selected: Boolean) {
    EventFahrplanTheme {
        DropdownFilterChip(
            enabled = enabled,
            selected = selected,
            label = { Text("Language (1)") },
            options = persistentListOf(
                DropdownFilterChipOption(key = "en", label = "English", selected = true),
                DropdownFilterChipOption(key = "de", label = "German", selected = false),
            ),
            onOptionClick = {},
        )
    }
}
