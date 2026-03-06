package nerd.tuxmobil.fahrplan.congress.favorites

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving

class MultiSelectState(
    private val resourceResolving: ResourceResolving,
    private val onUpdateTitleText: (String) -> Unit,
    @param:StringRes private val multiSelectTitle: Int,
    @param:StringRes private val defaultTitle: Int,
    @param:PluralsRes private val sessionsCountPlurals: Int,
) {
    var multiSelectEnabled: Boolean = false
        private set

    private var lastKnownSessionsCount = 0
    private var lastDisplayedTitleText: String? = null

    fun updateTitleText(sessionsCount: Int) {
        lastKnownSessionsCount = sessionsCount
        updateTitleText(getTitleText(sessionsCount))
    }

    fun onMultiSelectChanged(
        selectedCount: Int,
        onMultiSelectChanged: (Boolean) -> Unit,
    ) {
        val wasEnabled = multiSelectEnabled
        multiSelectEnabled = selectedCount > 0
        if (multiSelectEnabled) {
            updateTitleText(resourceResolving.getString(multiSelectTitle))
        } else if (wasEnabled) {
            updateTitleText(getTitleText(lastKnownSessionsCount))
        }
        onMultiSelectChanged(multiSelectEnabled)
    }

    private fun updateTitleText(text: String) {
        if (text == lastDisplayedTitleText) return
        lastDisplayedTitleText = text
        onUpdateTitleText(text)
    }

    private fun getTitleText(count: Int) = when (count == 0) {
        true -> resourceResolving.getString(defaultTitle)
        false -> resourceResolving.getQuantityString(sessionsCountPlurals, count, count)
    }

}
