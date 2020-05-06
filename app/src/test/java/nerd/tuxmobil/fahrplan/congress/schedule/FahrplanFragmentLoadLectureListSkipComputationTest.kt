package nerd.tuxmobil.fahrplan.congress.schedule

import android.support.v4.util.SparseArrayCompat
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import nerd.tuxmobil.fahrplan.congress.MyApp
import nerd.tuxmobil.fahrplan.congress.NoLogging
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class FahrplanFragmentLoadLectureListSkipComputationTest(

        private val shouldForce: Boolean,
        private val lectureList: List<Lecture>?,
        private val dayIndex: Int

) {

    private val onDoneDoNothing = {}

    companion object {

        private const val DAY_INDEX_NOT_INITIALIZED = 0
        private val LECTURE_LIST_NOT_INITIALIZED = null
        private val LECTURE_LIST_EMPTY = emptyList<Lecture>()

        @JvmStatic
        @Parameterized.Parameters(name = "#{index}: shouldForce={0}, lecturesList={1}, dayIndex={2}")
        fun data() = listOf(
                arrayOf(false, LECTURE_LIST_NOT_INITIALIZED, DAY_INDEX_NOT_INITIALIZED),
                arrayOf(false, LECTURE_LIST_EMPTY, DAY_INDEX_NOT_INITIALIZED),
                arrayOf(false, LECTURE_LIST_EMPTY, 1),
                arrayOf(false, LECTURE_LIST_NOT_INITIALIZED, 1),
                arrayOf(true, LECTURE_LIST_EMPTY, DAY_INDEX_NOT_INITIALIZED),
                arrayOf(true, LECTURE_LIST_EMPTY, 1),
                arrayOf(true, LECTURE_LIST_NOT_INITIALIZED, DAY_INDEX_NOT_INITIALIZED),
                arrayOf(true, LECTURE_LIST_NOT_INITIALIZED, 1)
        )
    }

    @Test
    fun `Skips modifying static "MyApp" fields`() {
        MyApp.lectureList = lectureList
        MyApp.lectureListDay = DAY_INDEX_NOT_INITIALIZED
        MyApp.roomCount = 0
        MyApp.roomsMap.clear()
        MyApp.roomList.clear()

        FahrplanFragment.loadLectureList(NoLogging, mock(), dayIndex, shouldForce, onDoneDoNothing)

        assertThat(MyApp.lectureList as List<*>).isEqualTo(emptyList<Lecture>())
        assertThat(MyApp.lectureListDay).isEqualTo(DAY_INDEX_NOT_INITIALIZED)
        assertThat(MyApp.roomsMap).isEqualTo(emptyMap<String, Int>())
        assertThat(MyApp.roomList.toString()).isEqualTo(SparseArrayCompat<Int>().toString())
        assertThat(MyApp.roomCount).isEqualTo(0)
    }

}
