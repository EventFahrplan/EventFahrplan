package nerd.tuxmobil.fahrplan.congress.sharing

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.jupiter.api.Test

class JsonSessionFormatTest {

    @Test
    fun formatSingle() {
        val session = Session("session1")
        val json = JsonSessionFormat().format(session)
        assertThat(json).isEqualTo(EXPECTED_JSON_SINGLE)
    }

    @Test
    fun formatList() {
        val sessions = listOf(
                Session("session1"),
                Session("session2"),
                Session("session3", recordingOptOut = true))
        val json = JsonSessionFormat().format(sessions)
        assertThat(json).isEqualTo(EXPECTED_JSON_LIST)
    }

    private companion object {

        const val EXPECTED_JSON_SINGLE = "{\"lectures\":[{\"lecture_id\":\"session1\"," +
                "\"title\":\"\",\"subtitle\":\"\",\"day\":0,\"room\":\"\",\"slug\":\"\"," +
                "\"url\":\"\",\"speakers\":\"\",\"track\":\"\",\"type\":\"\",\"lang\":\"\"," +
                "\"abstract\":\"\",\"description\":\"\",\"links\":\"\"," +
                "\"starts_at\":\"1970-01-01T00:00:00Z\",\"recorded\":true}]}"
        const val EXPECTED_JSON_LIST = "{\"lectures\":[{\"lecture_id\":\"session1\"," +
                "\"title\":\"\",\"subtitle\":\"\",\"day\":0,\"room\":\"\",\"slug\":\"\"" +
                ",\"url\":\"\",\"speakers\":\"\",\"track\":\"\",\"type\":\"\",\"lang\":\"\"" +
                ",\"abstract\":\"\",\"description\":\"\",\"links\":\"\"," +
                "\"starts_at\":\"1970-01-01T00:00:00Z\",\"recorded\":true}," +
                "{\"lecture_id\":\"session2\",\"title\":\"\",\"subtitle\":\"\",\"day\":0," +
                "\"room\":\"\",\"slug\":\"\",\"url\":\"\",\"speakers\":\"\",\"track\":\"\"," +
                "\"type\":\"\",\"lang\":\"\",\"abstract\":\"\",\"description\":\"\"," +
                "\"links\":\"\",\"starts_at\":\"1970-01-01T00:00:00Z\",\"recorded\":true}," +
                "{\"lecture_id\":\"session3\",\"title\":\"\",\"subtitle\":\"\",\"day\":0," +
                "\"room\":\"\",\"slug\":\"\",\"url\":\"\",\"speakers\":\"\",\"track\":\"\"," +
                "\"type\":\"\",\"lang\":\"\",\"abstract\":\"\",\"description\":\"\",\"links\":\"\"," +
                "\"starts_at\":\"1970-01-01T00:00:00Z\",\"recorded\":false}]}"

    }

}
