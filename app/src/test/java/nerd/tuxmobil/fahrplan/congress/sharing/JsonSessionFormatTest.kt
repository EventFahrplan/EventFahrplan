package nerd.tuxmobil.fahrplan.congress.sharing

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Session
import org.junit.Test

class JsonSessionFormatTest {

    @Test
    fun formatSingle() {
        val lecture = Session("lecture1")
        val json = JsonSessionFormat.format(lecture)
        assertThat(json).isEqualTo(EXPECTED_JSON_SINGLE)
    }

    @Test
    fun formatList() {
        val lectures = listOf(
                Session("lecture1"),
                Session("lecture2"),
                Session("lecture3"))
        val json = JsonSessionFormat.format(lectures)
        assertThat(json).isEqualTo(EXPECTED_JSON_LIST)
    }

    companion object {

        private const val EXPECTED_JSON_SINGLE = "{\"lectures\":[{\"lecture_id\":\"lecture1\"," +
                "\"title\":\"\",\"subtitle\":\"\",\"day\":0,\"room\":\"\",\"slug\":\"\"," +
                "\"speakers\":\"\",\"track\":\"\",\"type\":\"\",\"lang\":\"\",\"abstract\":\"\"," +
                "\"description\":\"\",\"links\":\"\"}]}"
        private const val EXPECTED_JSON_LIST = "{\"lectures\":[{\"lecture_id\":\"lecture1\"," +
                "\"title\":\"\",\"subtitle\":\"\",\"day\":0,\"room\":\"\",\"slug\":\"\"," +
                "\"speakers\":\"\",\"track\":\"\",\"type\":\"\",\"lang\":\"\",\"abstract\":\"\"," +
                "\"description\":\"\",\"links\":\"\"},{\"lecture_id\":\"lecture2\",\"title\":\"\"," +
                "\"subtitle\":\"\",\"day\":0,\"room\":\"\",\"slug\":\"\",\"speakers\":\"\"," +
                "\"track\":\"\",\"type\":\"\",\"lang\":\"\",\"abstract\":\"\",\"description\":\"\"," +
                "\"links\":\"\"},{\"lecture_id\":\"lecture3\",\"title\":\"\",\"subtitle\":\"\"," +
                "\"day\":0,\"room\":\"\",\"slug\":\"\",\"speakers\":\"\",\"track\":\"\"," +
                "\"type\":\"\",\"lang\":\"\",\"abstract\":\"\",\"description\":\"\",\"links\":\"\"}]}"

    }

}
