package nerd.tuxmobil.fahrplan.congress.schedule

import nerd.tuxmobil.fahrplan.congress.models.Lecture
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LectureViewDrawerTest {

    @Test
    fun calculateLayoutParams() {
        val conference = Conference()
        val lectures = listOf(Lecture("0"))

        LectureViewDrawer.calculateLayoutParams(0, lectures, 13, conference)
    }
}