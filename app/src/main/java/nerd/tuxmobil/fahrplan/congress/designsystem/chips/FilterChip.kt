package nerd.tuxmobil.fahrplan.congress.designsystem.chips

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChipDefaults.IconSize
import androidx.compose.material3.FilterChipDefaults.filterChipBorder
import androidx.compose.material3.FilterChipDefaults.filterChipColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconCheck
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import androidx.compose.material3.FilterChip as Material3FilterChip

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    Material3FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        enabled = enabled,
        colors = filterChipColors(
            containerColor = EventFahrplanTheme.colorScheme.searchFilterChipContainer,
            labelColor = EventFahrplanTheme.colorScheme.searchFilterChipLabel,
            iconColor = EventFahrplanTheme.colorScheme.searchFilterChipLabel,
            selectedContainerColor = EventFahrplanTheme.colorScheme.searchFilterChipSelectedContainer,
            selectedLabelColor = EventFahrplanTheme.colorScheme.searchFilterChipSelectedLabel,
            selectedLeadingIconColor = EventFahrplanTheme.colorScheme.searchFilterChipSelectedLabel,
            selectedTrailingIconColor = EventFahrplanTheme.colorScheme.searchFilterChipSelectedLabel,
        ),
        border = filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = EventFahrplanTheme.colorScheme.searchFilterChipBorder,
        ),
        modifier = modifier,
        leadingIcon = if (selected) {
            {
                IconCheck(Modifier.size(IconSize))
            }
        } else {
            null
        },
        trailingIcon = trailingIcon,
    )

}

@PreviewLightDark
@Composable
private fun FilterChipEnabledSelectedPreview() {
    FilterChip(enabled = true, selected = true)
}

@PreviewLightDark
@Composable
private fun FilterChipDisabledSelectedPreview() {
    FilterChip(enabled = false, selected = true)
}

@PreviewLightDark
@Composable
private fun FilterChipEnabledUnselectedPreview() {
    FilterChip(enabled = true, selected = false)
}

@PreviewLightDark
@Composable
private fun FilterChipDisabledUnselectedPreview() {
    FilterChip(enabled = false, selected = false)
}

@Composable
private fun FilterChip(enabled: Boolean, selected: Boolean) {
    EventFahrplanTheme {
        FilterChip(
            enabled = enabled,
            selected = selected,
            onClick = {},
            label = { Text("Lorem ipsum") },
        )
    }
}
