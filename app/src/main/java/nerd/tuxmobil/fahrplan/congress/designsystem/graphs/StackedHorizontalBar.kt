package nerd.tuxmobil.fahrplan.congress.designsystem.graphs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Magenta
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextAlign.Companion.End
import androidx.compose.ui.text.style.TextAlign.Companion.Start
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import nerd.tuxmobil.fahrplan.congress.designsystem.graphs.StackedHorizontalBar.BarHeight
import nerd.tuxmobil.fahrplan.congress.designsystem.graphs.StackedHorizontalBar.Colors
import nerd.tuxmobil.fahrplan.congress.designsystem.graphs.StackedHorizontalBar.Item
import nerd.tuxmobil.fahrplan.congress.designsystem.graphs.StackedHorizontalBar.TextAligns
import nerd.tuxmobil.fahrplan.congress.designsystem.graphs.StackedHorizontalBar.TextMinWidth
import nerd.tuxmobil.fahrplan.congress.designsystem.texts.Text
import nerd.tuxmobil.fahrplan.congress.designsystem.themes.EventFahrplanTheme

@Composable
fun StackedHorizontalBar(
    item: Item,
    contentDescription: String,
    colors: Colors,
    modifier: Modifier = Modifier,
    textAligns: TextAligns = TextAligns(),
    barHeight: Dp = BarHeight,
    textMinWidth: Dp = TextMinWidth,
) {
    Row(
        modifier = modifier.clearAndSetSemantics {
            this.contentDescription = contentDescription
        },
        verticalAlignment = CenterVertically,
    ) {
        if (!item.text1.isNullOrEmpty()) {
            Text(
                text = item.text1,
                textAlign = textAligns.text1,
                modifier = Modifier
                    .defaultMinSize(minWidth = textMinWidth)
                    .padding(end = 8.dp),
            )
        }
        if (item.value1 > 0) {
            Box(
                modifier = Modifier
                    .weight(item.value1Fraction)
                    .height(barHeight)
                    .background(colors.value1)
            )
        }
        if (item.value2 > 0) {
            Box(
                modifier = Modifier
                    .weight(item.value2Fraction)
                    .height(barHeight)
                    .background(colors.value2)
            )
        }
        if (!item.text2.isNullOrEmpty()) {
            Text(
                text = item.text2,
                textAlign = textAligns.text2,
                modifier = Modifier
                    .defaultMinSize(minWidth = textMinWidth)
                    .padding(start = 8.dp)
            )
        }
    }
}

object StackedHorizontalBar {

    val TextMinWidth: Dp = 50.dp
    val BarHeight: Dp = 20.dp

    @Immutable
    data class Item(
        val value1: Int,
        val value2: Int,
        val totalValue: Int,
        val text1: String? = "$value1",
        val text2: String? = "$value2",
    ) {
        val value1Fraction = if (totalValue == 0) 0f else value1.toFloat() / totalValue
        val value2Fraction = if (totalValue == 0) 0f else value2.toFloat() / totalValue
        val value1Percentage = value1Fraction * 100
    }

    @Immutable
    data class Colors(
        val value1: Color,
        val value2: Color,
    )

    @Immutable
    data class TextAligns(
        val text1: TextAlign = Start,
        val text2: TextAlign = Start,
    )

}

@PreviewLightDark
@Composable
private fun StackedHorizontalBarScheduleStatisticPreview() {
    val item = Item(
        value1 = 100,
        value2 = 150,
        totalValue = 250,
    )
    EventFahrplanTheme {
        StackedHorizontalBar(
            item = item,
            contentDescription = "",
            textAligns = TextAligns(
                text1 = End,
                text2 = Start,
            ),
            colors = Colors(
                value1 = Magenta,
                value2 = EventFahrplanTheme.colorScheme.background,
            ),
        )
    }
}

@PreviewLightDark
@Composable
private fun StackedHorizontalBarScheduleChangePreview() {
    val item = Item(
        value1 = 50,
        value2 = 200,
        totalValue = 250,
        text1 = "New",
        text2 = "",
    )
    EventFahrplanTheme {
        StackedHorizontalBar(
            item = item,
            contentDescription = "",
            colors = Colors(
                value1 = Green,
                value2 = EventFahrplanTheme.colorScheme.background,
            ),
        )
    }
}
