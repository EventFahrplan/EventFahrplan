package nerd.tuxmobil.fahrplan.congress.designsystem.bars

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme
import nerd.tuxmobil.fahrplan.congress.extensions.safeDrawingHorizontalAndTop
import androidx.compose.material3.TopAppBar as Material3TopAppBar
import androidx.compose.material3.TopAppBarDefaults as Material3TopAppBarDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
    Material3TopAppBar(
        modifier = modifier,
        colors = Material3TopAppBarDefaults.topAppBarColors(
            containerColor = EventFahrplanTheme.colorScheme.appBarContainer,
        ),
        title = title,
        navigationIcon = navigationIcon,
        windowInsets = windowInsets,
        actions = actions,
    )
}

object TopAppBarDefaults {
    val windowInsets: WindowInsets
        @Composable
        get() = WindowInsets.safeDrawingHorizontalAndTop()
}
