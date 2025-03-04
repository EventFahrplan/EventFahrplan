package nerd.tuxmobil.fahrplan.congress.designsystem.texts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.SemiBold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Text as Material3Text

@Composable
fun TextTableHeader(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
) {
    Material3Text(
        text = text,
        modifier = modifier,
        fontWeight = SemiBold,
        textAlign = textAlign,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
    )
}
