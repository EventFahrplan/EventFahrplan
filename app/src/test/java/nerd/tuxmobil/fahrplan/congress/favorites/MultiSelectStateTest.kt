package nerd.tuxmobil.fahrplan.congress.favorites

import com.google.common.truth.Truth.assertThat
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.commons.ResourceResolving
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MultiSelectStateTest {

    private val capturedTitleTexts = mutableListOf<String>()

    @Nested
    inner class UpdateTitleText {

        @Test
        fun `updateTitleText with zero sessions invokes callback with default title`() {
            val state = createState()
            state.updateTitleText(0)
            assertThat(capturedTitleTexts).containsExactly("Favorites")
        }

        @Test
        fun `updateTitleText with one session invokes callback with singular form`() {
            val state = createState()
            state.updateTitleText(1)
            assertThat(capturedTitleTexts).containsExactly("1 session")
        }

        @Test
        fun `updateTitleText with multiple sessions invokes callback with plural form`() {
            val state = createState()
            state.updateTitleText(5)
            assertThat(capturedTitleTexts).containsExactly("5 sessions")
        }

        @Test
        fun `updateTitleText with same count does not invoke callback again`() {
            val state = createState()
            state.updateTitleText(3)
            state.updateTitleText(3)
            assertThat(capturedTitleTexts).hasSize(1)
        }

        @Test
        fun `updateTitleText with different count invokes callback again`() {
            val state = createState()
            state.updateTitleText(1)
            state.updateTitleText(2)
            assertThat(capturedTitleTexts).hasSize(2)
        }

    }

    @Nested
    inner class OnMultiSelectChanged {

        @Test
        fun `onMultiSelectChanged with positive count sets multiSelectEnabled`() {
            val state = createState()
            state.onMultiSelectChanged(2)
            assertThat(state.multiSelectEnabled).isTrue()
            assertThat(capturedTitleTexts).containsExactly("Select sessions")
        }

        @Test
        fun `onMultiSelectChanged with zero count when multiSelectEnabled false does not invoke title callback`() {
            val state = createState()
            state.updateTitleText(3)
            capturedTitleTexts.clear()
            state.onMultiSelectChanged(0)
            assertThat(state.multiSelectEnabled).isFalse()
            assertThat(capturedTitleTexts).isEmpty()
        }

        @Test
        fun `onMultiSelectChanged with zero count when leaving multiSelectEnabled restores sessions title`() {
            val state = createState()
            state.updateTitleText(5)
            capturedTitleTexts.clear()
            state.onMultiSelectChanged(2) // enter multi-select
            capturedTitleTexts.clear()
            state.onMultiSelectChanged(0)
            assertThat(state.multiSelectEnabled).isFalse()
            assertThat(capturedTitleTexts).containsExactly("5 sessions")
        }

        @Test
        fun `onMultiSelectChanged toggling multiSelectEnabled off and on updates title correctly`() {
            val state = createState()
            state.updateTitleText(1)
            capturedTitleTexts.clear()
            state.onMultiSelectChanged(1) // enter multi-select
            assertThat(capturedTitleTexts).containsExactly("Select sessions")
            capturedTitleTexts.clear()
            state.onMultiSelectChanged(0) // leave multi-select
            assertThat(capturedTitleTexts).containsExactly("1 session")
        }

    }

    private fun createState() = MultiSelectState(
        resourceResolving = resourceResolving,
        onUpdateTitleText = { capturedTitleTexts.add(it) },
        multiSelectTitle = R.string.choose_to_delete,
        defaultTitle = R.string.favorites_screen_default_title,
        sessionsCountPlurals = R.plurals.favorites_screen_title,
    )

}

private val resourceResolving = object : ResourceResolving {
    override fun getString(id: Int, vararg formatArgs: Any) = when (id) {
        R.string.choose_to_delete -> "Select sessions"
        R.string.favorites_screen_default_title -> "Favorites"
        else -> error("unknown-$id")
    }

    override fun getQuantityString(id: Int, quantity: Int, vararg formatArgs: Any): String =
        if (quantity == 1) "1 session" else "$quantity sessions"
}
