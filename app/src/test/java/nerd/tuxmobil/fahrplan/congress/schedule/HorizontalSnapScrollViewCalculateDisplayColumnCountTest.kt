package nerd.tuxmobil.fahrplan.congress.schedule

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.R
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Parameterized unit test for [HorizontalSnapScrollView.calculateDisplayColumnCount].
 */
@RunWith(Parameterized::class)
class HorizontalSnapScrollViewCalculateDisplayColumnCountTest(
        private val availablePixels: Int,
        private val totalColumnCount: Int,
        private val maxColumnCountForLayout: Int,
        private val densityScaleFactor: Float,
        private val minColumnWidthDip: Int,
        private val calculatedColumnCount: Int,
        @Suppress("unused") private val testDescription: String
) {

    companion object {

        private class Device(
                val title: String,
                private val availableHeightInDip: Int,
                private val availableWidthInDip: Int,
                val screenDensity: Float
        ) {
            fun getAvailablePixel(isPortraitMode: Boolean) = if (isPortraitMode) availableHeightInDip else availableWidthInDip
        }

        private fun scenario(
                device: Device,
                isPortraitMode: Boolean,
                totalColumnCount: Int,
                maxColumnCountForLayout: Int,
                minColumnWidthDip: Int,
                calculatedColumnCount: Int
        ): Array<Any> {
            val orientationText = if (isPortraitMode) "Portrait" else "Landscape"
            val testDescription = "${device.title}, $orientationText, totalColumnCount=$totalColumnCount, maxColumnCountForLayout=$maxColumnCountForLayout --> $calculatedColumnCount"
            return arrayOf(
                    device.getAvailablePixel(isPortraitMode),
                    totalColumnCount,
                    maxColumnCountForLayout,
                    device.screenDensity,
                    minColumnWidthDip,
                    calculatedColumnCount,
                    testDescription
            )
        }

        // Device metrics are retrieved from debugging values at the actual devices.
        private val nexus5 = Device("Nexus 5", availableHeightInDip = 978, availableWidthInDip = 1706, screenDensity = 3.0f)
        private val pixel2 = Device("Pixel 2", availableHeightInDip = 993, availableWidthInDip = 1731, screenDensity = 2.625f)
        private val nexus9 = Device("Nexus 9", availableHeightInDip = 1462, availableWidthInDip = 1974, screenDensity = 2.0f)

        /**
         * See [R.integer.min_width_dip]
         */
        private const val MIN_COLUMN_WIDTH_140_DIP = 140

        /**
         * See [R.integer.min_width_dip]
         */
        private const val MIN_COLUMN_WIDTH_160_DIP = 160

        /**
         * See [R.integer.max_cols]
         */
        private const val MAX_COLUMN_COUNT_FOR_LAYOUT_1 = 1

        /**
         * See [R.integer.max_cols]
         */
        private const val MAX_COLUMN_COUNT_FOR_LAYOUT_4 = 4

        /**
         * See [R.integer.max_cols]
         */
        private const val MAX_COLUMN_COUNT_FOR_LAYOUT_6 = 6

        private fun pixel2Portrait(totalColumnCount: Int, @Suppress("SameParameterValue") calculatedColumnCount: Int) = scenario(
                device = pixel2,
                isPortraitMode = true,
                totalColumnCount = totalColumnCount,
                maxColumnCountForLayout = MAX_COLUMN_COUNT_FOR_LAYOUT_1,
                minColumnWidthDip = MIN_COLUMN_WIDTH_160_DIP,
                calculatedColumnCount = calculatedColumnCount
        )

        private fun pixel2Landscape(totalColumnCount: Int, calculatedColumnCount: Int) = scenario(
                device = pixel2,
                isPortraitMode = false,
                totalColumnCount = totalColumnCount,
                maxColumnCountForLayout = MAX_COLUMN_COUNT_FOR_LAYOUT_4,
                minColumnWidthDip = MIN_COLUMN_WIDTH_140_DIP,
                calculatedColumnCount = calculatedColumnCount
        )

        private fun nexus5Portrait(totalColumnCount: Int, @Suppress("SameParameterValue") calculatedColumnCount: Int) = scenario(
                device = nexus5,
                isPortraitMode = true,
                totalColumnCount = totalColumnCount,
                maxColumnCountForLayout = MAX_COLUMN_COUNT_FOR_LAYOUT_1,
                minColumnWidthDip = MIN_COLUMN_WIDTH_160_DIP,
                calculatedColumnCount = calculatedColumnCount
        )

        private fun nexus5Landscape(totalColumnCount: Int, calculatedColumnCount: Int) = scenario(
                device = nexus5,
                isPortraitMode = false,
                totalColumnCount = totalColumnCount,
                maxColumnCountForLayout = MAX_COLUMN_COUNT_FOR_LAYOUT_4,
                minColumnWidthDip = MIN_COLUMN_WIDTH_140_DIP,
                calculatedColumnCount = calculatedColumnCount
        )

        private fun nexus9Portrait(totalColumnCount: Int, calculatedColumnCount: Int) = scenario(
                device = nexus9,
                isPortraitMode = true,
                totalColumnCount = totalColumnCount,
                maxColumnCountForLayout = MAX_COLUMN_COUNT_FOR_LAYOUT_6,
                minColumnWidthDip = MIN_COLUMN_WIDTH_160_DIP,
                calculatedColumnCount = calculatedColumnCount
        )

        private fun nexus9Landscape(totalColumnCount: Int, calculatedColumnCount: Int) = scenario(
                device = nexus9,
                isPortraitMode = false,
                totalColumnCount = totalColumnCount,
                maxColumnCountForLayout = MAX_COLUMN_COUNT_FOR_LAYOUT_6,
                minColumnWidthDip = MIN_COLUMN_WIDTH_160_DIP,
                calculatedColumnCount = calculatedColumnCount
        )

        @JvmStatic
        @Parameterized.Parameters(name = "{index}: \"{6}\"")
        fun data() = listOf(
                // Pixel 2 portrait (maxColumnCountForLayout=1)
                pixel2Portrait(totalColumnCount = 1, calculatedColumnCount = 1),
                pixel2Portrait(totalColumnCount = 2, calculatedColumnCount = 1),
                pixel2Portrait(totalColumnCount = 3, calculatedColumnCount = 1),
                pixel2Portrait(totalColumnCount = 4, calculatedColumnCount = 1),
                pixel2Portrait(totalColumnCount = 5, calculatedColumnCount = 1),
                pixel2Portrait(totalColumnCount = 6, calculatedColumnCount = 1),
                pixel2Portrait(totalColumnCount = 7, calculatedColumnCount = 1),
                pixel2Portrait(totalColumnCount = 55, calculatedColumnCount = 1),

                // Pixel 2 landscape (maxColumnCountForLayout=4)
                pixel2Landscape(totalColumnCount = 1, calculatedColumnCount = 1),
                pixel2Landscape(totalColumnCount = 2, calculatedColumnCount = 2),
                pixel2Landscape(totalColumnCount = 3, calculatedColumnCount = 3),
                pixel2Landscape(totalColumnCount = 4, calculatedColumnCount = 4),
                pixel2Landscape(totalColumnCount = 5, calculatedColumnCount = 4),
                pixel2Landscape(totalColumnCount = 6, calculatedColumnCount = 4),
                pixel2Landscape(totalColumnCount = 7, calculatedColumnCount = 4),
                pixel2Landscape(totalColumnCount = 55, calculatedColumnCount = 4),

                // Nexus 5 portrait (maxColumnCountForLayout=1)
                nexus5Portrait(totalColumnCount = 1, calculatedColumnCount = 1),
                nexus5Portrait(totalColumnCount = 2, calculatedColumnCount = 1),
                nexus5Portrait(totalColumnCount = 3, calculatedColumnCount = 1),
                nexus5Portrait(totalColumnCount = 4, calculatedColumnCount = 1),
                nexus5Portrait(totalColumnCount = 5, calculatedColumnCount = 1),
                nexus5Portrait(totalColumnCount = 6, calculatedColumnCount = 1),
                nexus5Portrait(totalColumnCount = 7, calculatedColumnCount = 1),
                nexus5Portrait(totalColumnCount = 55, calculatedColumnCount = 1),

                // Nexus 5 landscape (maxColumnCountForLayout=4)
                nexus5Landscape(totalColumnCount = 1, calculatedColumnCount = 1),
                nexus5Landscape(totalColumnCount = 2, calculatedColumnCount = 2),
                nexus5Landscape(totalColumnCount = 3, calculatedColumnCount = 3),
                nexus5Landscape(totalColumnCount = 4, calculatedColumnCount = 4),
                nexus5Landscape(totalColumnCount = 5, calculatedColumnCount = 4),
                nexus5Landscape(totalColumnCount = 6, calculatedColumnCount = 4),
                nexus5Landscape(totalColumnCount = 7, calculatedColumnCount = 4),
                nexus5Landscape(totalColumnCount = 55, calculatedColumnCount = 4),

                // Nexus 9 portrait, sw720dp (maxColumnCountForLayout=6)
                nexus9Portrait(totalColumnCount = 1, calculatedColumnCount = 1),
                nexus9Portrait(totalColumnCount = 2, calculatedColumnCount = 2),
                nexus9Portrait(totalColumnCount = 3, calculatedColumnCount = 3),
                nexus9Portrait(totalColumnCount = 4, calculatedColumnCount = 4),
                nexus9Portrait(totalColumnCount = 5, calculatedColumnCount = 4),
                nexus9Portrait(totalColumnCount = 6, calculatedColumnCount = 4),
                nexus9Portrait(totalColumnCount = 7, calculatedColumnCount = 4),
                nexus9Portrait(totalColumnCount = 55, calculatedColumnCount = 4),

                // Nexus 9 landscape, sw720dp (maxColumnCountForLayout=6)
                nexus9Landscape(totalColumnCount = 1, calculatedColumnCount = 1),
                nexus9Landscape(totalColumnCount = 2, calculatedColumnCount = 2),
                nexus9Landscape(totalColumnCount = 3, calculatedColumnCount = 3),
                nexus9Landscape(totalColumnCount = 4, calculatedColumnCount = 4),
                nexus9Landscape(totalColumnCount = 5, calculatedColumnCount = 5),
                nexus9Landscape(totalColumnCount = 6, calculatedColumnCount = 6),
                nexus9Landscape(totalColumnCount = 7, calculatedColumnCount = 6),
                nexus9Landscape(totalColumnCount = 55, calculatedColumnCount = 6)
        )
    }

    @Test
    fun calculateDisplayColumnCount() {
        assertThat(HorizontalSnapScrollView.calculateDisplayColumnCount(
                availablePixels,
                totalColumnCount,
                maxColumnCountForLayout,
                densityScaleFactor,
                minColumnWidthDip
        )).isEqualTo(calculatedColumnCount)
    }

}
