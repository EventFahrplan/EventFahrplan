package nerd.tuxmobil.fahrplan.congress.repositories

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.MyApp
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import org.junit.Test

class AppRepositoryLegacyTest {

    @Test
    fun `updateLecturesLegacy marks matching lecture in lectureList as not starred`() {
        val lecture1 = Lecture("1001").apply { highlight = true }
        val lecture2 = Lecture("1002").apply { highlight = false }
        MyApp.lectureList = listOf(lecture1, lecture2)
        val updatedLecture = Lecture("1001").apply { highlight = false }
        AppRepository.updateLecturesLegacy(updatedLecture)
        MyApp.lectureList.forEach {
            assertThat(it.highlight).isFalse()
        }
    }

    @Test
    fun `updateLecturesLegacy marks matching lecture in lectureList as starred`() {
        val lecture1 = Lecture("1001").apply { highlight = true }
        val lecture2 = Lecture("1002").apply { highlight = false }
        MyApp.lectureList = listOf(lecture1, lecture2)
        val updatedLecture = Lecture("1002").apply { highlight = true }
        AppRepository.updateLecturesLegacy(updatedLecture)
        MyApp.lectureList.forEach {
            assertThat(it.highlight).isTrue()
        }
    }

    @Test
    fun `updateLecturesLegacy marks matching lecture in lectureList as has no alarms`() {
        val lecture1 = Lecture("1001").apply { hasAlarm = true }
        val lecture2 = Lecture("1002").apply { hasAlarm = false }
        MyApp.lectureList = listOf(lecture1, lecture2)
        val updatedLecture = Lecture("1001").apply { hasAlarm = false }
        AppRepository.updateLecturesLegacy(updatedLecture)
        MyApp.lectureList.forEach {
            assertThat(it.hasAlarm).isFalse()
        }
    }

    @Test
    fun `updateLecturesLegacy marks matching lecture in lectureList as has alarms`() {
        val lecture1 = Lecture("1001").apply { hasAlarm = true }
        val lecture2 = Lecture("1002").apply { hasAlarm = false }
        MyApp.lectureList = listOf(lecture1, lecture2)
        val updatedLecture = Lecture("1002").apply { hasAlarm = true }
        AppRepository.updateLecturesLegacy(updatedLecture)
        MyApp.lectureList.forEach {
            assertThat(it.hasAlarm).isTrue()
        }
    }

    @Test
    fun `updateLecturesLegacy leaves not matching lecture1 in lectureList unmodified`() {
        val lecture1 = Lecture("1001").apply {
            hasAlarm = true
            highlight = true
        }
        val lecture2 = Lecture("1002").apply {
            hasAlarm = true
            highlight = false
        }
        MyApp.lectureList = listOf(lecture1, lecture2)
        val updatedLecture = Lecture("1002").apply {
            hasAlarm = false
            highlight = false
        }
        AppRepository.updateLecturesLegacy(updatedLecture)
        assertThat(lecture1.hasAlarm).isTrue()
        assertThat(lecture1.highlight).isTrue()
        assertThat(lecture2.hasAlarm).isFalse()
        assertThat(lecture2.highlight).isFalse()
    }

    @Test
    fun `updateLecturesLegacy leaves not matching lecture2 in lectureList unmodified`() {
        val lecture1 = Lecture("1001").apply {
            hasAlarm = true
            highlight = true
        }
        val lecture2 = Lecture("1002").apply {
            hasAlarm = false
            highlight = false
        }
        MyApp.lectureList = listOf(lecture1, lecture2)
        val updatedLecture = Lecture("1001").apply {
            hasAlarm = true
            highlight = true
        }
        AppRepository.updateLecturesLegacy(updatedLecture)
        assertThat(lecture1.hasAlarm).isTrue()
        assertThat(lecture1.highlight).isTrue()
        assertThat(lecture2.hasAlarm).isFalse()
        assertThat(lecture2.highlight).isFalse()
    }

}
