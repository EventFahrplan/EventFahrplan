package nerd.tuxmobil.fahrplan.congress.changes

import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.models.Session

/**
 * Statistic about lecture changes (canceled, changed, new).
 */
class ChangeStatistic @JvmOverloads constructor(

        val lectures: List<Session>,
        val logging: Logging = Logging.get()

) {

    /**
     * Returns how many lectures are marked as [canceled][Session.changedIsCanceled].
     */
    fun getCanceledLecturesCount() = lectures
            .count { it.changedIsCanceled }
            .also { log("$it canceled lectures") }

    /**
     * Returns how many lectures are marked as [changed][Session.isChanged].
     */
    fun getChangedLecturesCount() = lectures
            .count { it.isChanged }
            .also { log("$it changed lectures") }

    /**
     * Returns how many lectures are marked as [new][Session.changedIsNew].
     */
    fun getNewLecturesCount() = lectures
            .count { it.changedIsNew }
            .also { log("$it new lectures") }

    /**
     * Returns how many favorites are marked as [canceled][Session.changedIsCanceled],
     * [changed][Session.isChanged] or [new][Session.changedIsNew].
     */
    fun getChangedFavoritesCount() = lectures
            .count { it.highlight && (it.changedIsCanceled || it.isChanged || it.changedIsNew) }
            .also { log("$it changed favorites") }

    private fun log(message: String) = logging.d(javaClass.simpleName, message)

}
