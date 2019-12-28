package nerd.tuxmobil.fahrplan.congress.sharing.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FavoritesExport(val lectures: List<LectureExport>)
