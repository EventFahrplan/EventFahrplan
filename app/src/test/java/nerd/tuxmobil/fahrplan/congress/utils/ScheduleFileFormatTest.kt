package nerd.tuxmobil.fahrplan.congress.utils

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.utils.ScheduleFileFormat.SCHEDULE_V1_JSON
import nerd.tuxmobil.fahrplan.congress.utils.ScheduleFileFormat.SCHEDULE_V1_XML
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ScheduleFileFormatTest {

    @Test
    fun `of returns ScheduleV1Xml for schedule_v1_xml`() {
        val format = ScheduleFileFormat.of("schedule_v1_xml")
        assertThat(format).isEqualTo(SCHEDULE_V1_XML)
    }

    @Test
    fun `of returns ScheduleV1Json for schedule_v1_json`() {
        val format = ScheduleFileFormat.of("schedule_v1_json")
        assertThat(format).isEqualTo(SCHEDULE_V1_JSON)
    }

    @Test
    fun `of throws exception for unknown schedule file format string`() {
        val exception = assertThrows<UnknownScheduleFileFormatException> {
            ScheduleFileFormat.of("unknown")
        }
        assertThat(exception).hasMessageThat().isEqualTo("""Unknown schedule file format: "unknown".""")
    }

    @Test
    fun `of throws exception for empty schedule file format string`() {
        val exception = assertThrows<UnknownScheduleFileFormatException> {
            ScheduleFileFormat.of("")
        }
        assertThat(exception).hasMessageThat().isEqualTo("""Unknown schedule file format: "".""")
    }

}
