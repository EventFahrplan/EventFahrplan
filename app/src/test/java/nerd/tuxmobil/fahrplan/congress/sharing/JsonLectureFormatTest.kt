package nerd.tuxmobil.fahrplan.congress.sharing

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import org.junit.Test

class JsonLectureFormatTest {

    @Test
    fun formatSingle() {
        val lecture = Lecture("lecture1")
        val json = JsonLectureFormat.format(lecture)
        assertThat(json).isEqualTo(EXPECTED_JSON_SINGLE)
    }

    @Test
    fun formatList() {
        val lectures = listOf(
                Lecture("lecture1"),
                Lecture("lecture2"),
                Lecture("lecture3"))
        val json = JsonLectureFormat.format(lectures)
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
