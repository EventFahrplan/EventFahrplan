package nerd.tuxmobil.fahrplan.congress.designsystem.chips

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilterChipDefaults.filterChipBorder
import androidx.compose.material3.FilterChipDefaults.filterChipColors
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import androidx.compose.material3.FilterChip as Material3FilterChip

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    selectedIcon: ImageVector,
) {
    Material3FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
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
            enabled = true,
            selected = selected,
            borderColor = EventFahrplanTheme.colorScheme.searchFilterChipBorder,
        ),
        modifier = modifier,
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = selectedIcon,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        },
    )

}
