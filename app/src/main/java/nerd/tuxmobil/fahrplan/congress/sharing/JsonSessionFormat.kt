package nerd.tuxmobil.fahrplan.congress.sharing

import com.squareup.moshi.Moshi
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.sharing.models.FavoritesExport
import nerd.tuxmobil.fahrplan.congress.sharing.models.SessionExport

object JsonSessionFormat {

    private val moshi: Moshi by lazy { Moshi.Builder().build() }
    private val jsonAdapter by lazy { moshi.adapter(FavoritesExport::class.java) }

    @JvmStatic
    fun format(lecture: Session): String {
        val export = FavoritesExport(listOf(SessionExport(lecture)))
        return jsonAdapter.toJson(export)
    }

    @JvmStatic
    fun format(lectures: List<Session>): String? {
        return when {
            lectures.isEmpty() -> null
            lectures.size == 1 -> format(lectures[0])
            else -> {
                val export = FavoritesExport(lectures.map { SessionExport(it) })
                return jsonAdapter.toJson(export)
            }
        }

    }
}
