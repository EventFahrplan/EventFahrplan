package nerd.tuxmobil.fahrplan.congress.designsystem.search

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import androidx.compose.material3.SearchBar as Material3SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    inputField: @Composable () -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    dividerColor: Color = EventFahrplanTheme.colorScheme.searchBarDivider,
    content: @Composable ColumnScope.() -> Unit,
) {
    Material3SearchBar(
        inputField = inputField,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier,
        colors = SearchBarColors(
            containerColor = containerColor,
            dividerColor = dividerColor,
        ),
        content = content,
    )
}
