package nerd.tuxmobil.fahrplan.congress.designsystem.texts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text as Material3Text

@Composable
fun TextOverline(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    Material3Text(
        text = text,
        modifier = modifier,
        color = color,
    )
}
