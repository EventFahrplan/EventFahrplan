package nerd.tuxmobil.fahrplan.congress.designsystem.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.designsystem.icons.IconDecorativeVector
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
fun ButtonBox(
    modifier: Modifier = Modifier,
    color: Color = EventFahrplanTheme.colorScheme.surfaceContainerHighest,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color),
        contentAlignment = Center,
        content = content,
    )
}

@PreviewLightDark
@Composable
private fun ButtonBoxPreview() {
    EventFahrplanTheme {
        ButtonBox {
            IconDecorativeVector(
                imageVector = Icons.Default.Close,
            )
        }
    }
}
