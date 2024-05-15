package nerd.tuxmobil.fahrplan.congress.sharing

import com.squareup.moshi.Moshi
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.sharing.models.FavoritesExport
import nerd.tuxmobil.fahrplan.congress.sharing.models.SessionExport

class JsonSessionFormat(

    private val moshi: Moshi = Moshi.Builder().build()

) {

    private val jsonAdapter by lazy { moshi.adapter(FavoritesExport::class.java) }

    fun format(session: Session): String {
        val export = FavoritesExport(listOf(SessionExport(session)))
        return jsonAdapter.toJson(export)
    }

    fun format(sessions: List<Session>): String? {
        return when {
            sessions.isEmpty() -> null
            sessions.size == 1 -> format(sessions[0])
            else -> {
                val export = FavoritesExport(sessions.map { SessionExport(it) })
                return jsonAdapter.toJson(export)
            }
        }

    }
}
