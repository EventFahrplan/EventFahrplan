package nerd.tuxmobil.fahrplan.congress.search

import androidx.annotation.StringRes

data class SearchFilterUiState(
    @get:StringRes
    val label: Int,
    val selected: Boolean,
)
