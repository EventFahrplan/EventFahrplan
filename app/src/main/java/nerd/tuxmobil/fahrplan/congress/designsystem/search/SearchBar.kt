package nerd.tuxmobil.fahrplan.congress.designsystem.search

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.SearchBar as Material3SearchBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    inputField: @Composable () -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Material3SearchBar(
        inputField = inputField,
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier,
        content = content,
    )
}
