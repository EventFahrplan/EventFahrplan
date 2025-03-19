package nerd.tuxmobil.fahrplan.congress.settings

import android.content.res.Configuration
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import info.metadude.android.eventfahrplan.commons.temporal.Duration
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_DAY
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_HOUR
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_MINUTE
import info.metadude.android.eventfahrplan.commons.temporal.Moment.Companion.MILLISECONDS_OF_ONE_SECOND
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolver
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.of
import org.junit.jupiter.params.provider.MethodSource
import java.util.Locale

class IntervalFormatterTest {

    private companion object {

        @JvmStatic
        fun englishData() = listOf(
            of(MILLISECONDS_OF_ONE_DAY, "every day"),
            of((1.3 * MILLISECONDS_OF_ONE_DAY).toLong(), "every 1.3 days"),
            of(2 * MILLISECONDS_OF_ONE_DAY, "every 2 days"),

            of(MILLISECONDS_OF_ONE_HOUR, "every hour"),
            of(2 * MILLISECONDS_OF_ONE_HOUR, "every 2 hours"),
            of((2.2 * MILLISECONDS_OF_ONE_HOUR).toLong(), "every 2.2 hours"),

            of(MILLISECONDS_OF_ONE_MINUTE.toLong(), "every minute"),
            of((1.5 * MILLISECONDS_OF_ONE_MINUTE).toLong(), "every 1.5 minutes"),
            of(2 * MILLISECONDS_OF_ONE_MINUTE.toLong(), "every 2 minutes"),

            of(MILLISECONDS_OF_ONE_SECOND.toLong(), "every second"),
            of(2 * MILLISECONDS_OF_ONE_SECOND.toLong(), "every 2 seconds"),
        )

        @JvmStatic
        fun germanData() = listOf(
            of(MILLISECONDS_OF_ONE_DAY, "jeden Tag"),
            of((1.3 * MILLISECONDS_OF_ONE_DAY).toLong(), "jede 1,3 Tage"),
            of(2 * MILLISECONDS_OF_ONE_DAY, "jede 2 Tage"),

            of(MILLISECONDS_OF_ONE_HOUR, "jede Stunde"),
            of(2 * MILLISECONDS_OF_ONE_HOUR, "jede 2 Stunden"),
            of((2.2 * MILLISECONDS_OF_ONE_HOUR).toLong(), "jede 2,2 Stunden"),

            of(MILLISECONDS_OF_ONE_MINUTE.toLong(), "jede Minute"),
            of((1.5 * MILLISECONDS_OF_ONE_MINUTE).toLong(), "jede 1,5 Minuten"),
            of(2 * MILLISECONDS_OF_ONE_MINUTE.toLong(), "jede 2 Minuten"),

            of(MILLISECONDS_OF_ONE_SECOND.toLong(), "jede Sekunde"),
            of(2 * MILLISECONDS_OF_ONE_SECOND.toLong(), "jede 2 Sekunden"),
        )
    }

    private val systemLocale = Locale.getDefault()

    @AfterEach
    fun cleanUp() {
        Locale.setDefault(systemLocale)
    }

    @MethodSource("englishData")
    @DisplayName("EN: getFormattedInterval")
    @ParameterizedTest(name = """{index}: durationValue = "{0}" -> "{1}"""")
    fun getFormattedIntervalEnglish(durationValue: Long, expectedText: String) {
        val locale = Locale("en", "US")
        Locale.setDefault(locale)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val config = Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
        val germanContext = context.createConfigurationContext(config)
        val formatter = IntervalFormatter(ResourceResolver(germanContext))
        val duration = Duration.ofMilliseconds(durationValue)
        assertThat(formatter.getFormattedInterval(duration)).isEqualTo(expectedText)
    }

    @MethodSource("germanData")
    @DisplayName("DE: getFormattedInterval")
    @ParameterizedTest(name = """{index}: durationValue = "{0}" -> "{1}"""")
    fun getFormattedIntervalGerman(durationValue: Long, expectedText: String) {
        val locale = Locale("de", "DE")
        Locale.setDefault(locale)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val config = Configuration(context.resources.configuration).apply {
            setLocale(locale)
        }
        val germanContext = context.createConfigurationContext(config)
        val formatter = IntervalFormatter(ResourceResolver(germanContext))
        val duration = Duration.ofMilliseconds(durationValue)
        assertThat(formatter.getFormattedInterval(duration)).isEqualTo(expectedText)
    }

}