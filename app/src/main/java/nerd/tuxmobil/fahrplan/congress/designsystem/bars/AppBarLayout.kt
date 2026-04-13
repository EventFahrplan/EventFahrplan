package nerd.tuxmobil.fahrplan.congress.designsystem.bars

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.AppBarColumn as Material3AppBarColumn
import androidx.compose.material3.AppBarColumnScope as Material3AppBarColumnScope
import androidx.compose.material3.AppBarRow as Material3AppBarRow
import androidx.compose.material3.AppBarRowScope as Material3AppBarRowScope
import androidx.compose.material3.AppBarScope as Material3AppBarScope

typealias AppBarScope = Material3AppBarScope
typealias AppBarRowScope = Material3AppBarRowScope
typealias AppBarColumnScope = Material3AppBarColumnScope

@Composable
fun AppBarRow(
    modifier: Modifier = Modifier,
    maxItemCount: Int = Int.MAX_VALUE,
    content: AppBarRowScope.() -> Unit,
) {
    Material3AppBarRow(
        modifier = modifier,
        maxItemCount = maxItemCount,
        content = content,
    )
}

@Composable
fun AppBarColumn(
    modifier: Modifier = Modifier,
    maxItemCount: Int = Int.MAX_VALUE,
    content: AppBarColumnScope.() -> Unit,
) {
    Material3AppBarColumn(
        modifier = modifier,
        maxItemCount = maxItemCount,
        content = content,
    )
}
