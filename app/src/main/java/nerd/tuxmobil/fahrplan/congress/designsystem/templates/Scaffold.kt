package nerd.tuxmobil.fahrplan.congress.designsystem.templates

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.Scaffold as Material3Scaffold

@Composable
fun Scaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Material3Scaffold(
        content = content,
        modifier = modifier,
        topBar = topBar,
    )
}
