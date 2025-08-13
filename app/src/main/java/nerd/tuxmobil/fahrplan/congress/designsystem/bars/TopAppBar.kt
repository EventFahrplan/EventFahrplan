package nerd.tuxmobil.fahrplan.congress.designsystem.bars

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import androidx.compose.material3.TopAppBar as Material3TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
) {
    Material3TopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = EventFahrplanTheme.colorScheme.topAppBarContainer,
        ),
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
    )
}
