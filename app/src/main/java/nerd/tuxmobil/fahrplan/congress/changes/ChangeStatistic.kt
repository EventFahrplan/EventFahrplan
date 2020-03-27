package nerd.tuxmobil.fahrplan.congress.changes

import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.models.Lecture

/**
 * Statistic about lecture changes (canceled, changed, new).
 */
class ChangeStatistic @JvmOverloads constructor(

        val lectures: List<Lecture>,
        val logging: Logging = Logging.get()

) {

    /**
     * Returns how many lectures are marked as [canceled][Lecture.changedIsCanceled].
     */
    fun getCanceledLecturesCount() = lectures
            .count { it.changedIsCanceled }
            .also { log("$it canceled lectures") }

    /**
     * Returns how many lectures are marked as [changed][Lecture.isChanged].
     */
    fun getChangedLecturesCount() = lectures
            .count { it.isChanged }
            .also { log("$it changed lectures") }

    /**
     * Returns how many lectures are marked as [new][Lecture.changedIsNew].
     */
    fun getNewLecturesCount() = lectures
            .count { it.changedIsNew }
            .also { log("$it new lectures") }

    /**
     * Returns how many favorites are marked as [canceled][Lecture.changedIsCanceled],
     * [changed][Lecture.isChanged] or [new][Lecture.changedIsNew].
     */
    fun getChangedFavoritesCount() = lectures
            .count { it.highlight && (it.changedIsCanceled || it.isChanged || it.changedIsNew) }
            .also { log("$it changed favorites") }

    private fun log(message: String) = logging.d(javaClass.simpleName, message)

}
