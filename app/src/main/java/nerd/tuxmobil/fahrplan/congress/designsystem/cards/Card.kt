package nerd.tuxmobil.fahrplan.congress.designsystem.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardElevation
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.Card as Material3Card
import androidx.compose.material3.CardDefaults as Material3CardDefaults

@Composable
fun Card(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(),
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Material3Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        content = content,
    )
}

object CardDefaults {

    @Composable
    fun cardColors(
        containerColor: Color = Color.Unspecified,
        contentColor: Color = contentColorFor(containerColor),
    ) = Material3CardDefaults.cardColors(
        containerColor = containerColor,
        contentColor = contentColor,
    )

    @Composable
    fun cardElevation() = Material3CardDefaults.cardElevation()

    val shape: Shape
        @Composable get() = Material3CardDefaults.shape

}
