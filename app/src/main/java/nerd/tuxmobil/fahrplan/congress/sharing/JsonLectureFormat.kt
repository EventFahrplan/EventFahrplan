package nerd.tuxmobil.fahrplan.congress.sharing

import com.squareup.moshi.Moshi
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import nerd.tuxmobil.fahrplan.congress.sharing.models.FavoritesExport
import nerd.tuxmobil.fahrplan.congress.sharing.models.LectureExport

object JsonLectureFormat {

    private val moshi: Moshi by lazy { Moshi.Builder().build() }
    private val jsonAdapter by lazy { moshi.adapter(FavoritesExport::class.java) }

    @JvmStatic
    fun format(lecture: Lecture): String {
        val export = FavoritesExport(listOf(LectureExport(lecture)))
        return jsonAdapter.toJson(export)
    }

    @JvmStatic
    fun format(lectures: List<Lecture>): String? {
        return when {
            lectures.isEmpty() -> null
            lectures.size == 1 -> format(lectures[0])
            else -> {
                val export = FavoritesExport(lectures.map { LectureExport(it) })
                return jsonAdapter.toJson(export)
            }
        }

    }
}
