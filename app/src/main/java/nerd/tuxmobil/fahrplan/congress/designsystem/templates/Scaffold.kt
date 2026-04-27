package nerd.tuxmobil.fahrplan.congress.designsystem.templates

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Scaffold as Material3Scaffold
import androidx.compose.material3.ScaffoldDefaults as Material3ScaffoldDefaults

@Composable
fun Scaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    Material3Scaffold(
        content = content,
        modifier = modifier,
        contentWindowInsets = contentWindowInsets,
        topBar = topBar,
    )
}

object ScaffoldDefaults {

    val contentWindowInsets: WindowInsets
        @Composable get() = Material3ScaffoldDefaults.contentWindowInsets

}
