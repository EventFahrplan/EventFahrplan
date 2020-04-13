package nerd.tuxmobil.fahrplan.congress.schedule

import nerd.tuxmobil.fahrplan.congress.models.Lecture
import java.util.HashMap

/**
 * Groups given [lectures] by their [Lecture.roomIndex].
 * Each group is accessible via [LecturesByRoomIndex.get].
 *
 * The sorted list of all room indices might not be consecutive.
 */
data class LecturesByRoomIndex(val lectures: List<Lecture>) : HashMap<Int, List<Lecture>>() {

    init {
        lectures.distinctBy { it.roomIndex }
                .map { it.roomIndex }
                .forEach { roomIndex ->
                    this[roomIndex] = mutableListOf()
                }

        lectures.forEach { lecture ->
            (this[lecture.roomIndex] as MutableList).add(lecture)
        }
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun get(roomIndex: Int) = super.get(roomIndex) ?: emptyList()
}