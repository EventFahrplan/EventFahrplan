package nerd.tuxmobil.fahrplan.congress.utils.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop

private const val LAZY_LIST_SCROLL_PACK = 1_000_000L

/**
 * Abstraction over a scroll position for [rememberAutoHideOnScrollDown].
 *
 * - [LazyList]: [androidx.compose.foundation.lazy.LazyListState] (e.g. [androidx.compose.foundation.lazy.LazyColumn]).
 * - [Scroll]: [androidx.compose.foundation.ScrollState] (e.g. [androidx.compose.foundation.verticalScroll]).
 *
 * [snapshot] is a monotonic scalar used to compare scroll direction; [LazyList] combines
 * first visible index and pixel offset.
 */
internal sealed interface ScrollPosition {
    /** `true` while the user (or animation) is actively changing scroll position. */
    val isScrollInProgress: Boolean

    /** Packed scroll position; larger means further scrolled toward the end of the content. */
    fun snapshot(): Long

    data class LazyList(val state: LazyListState) : ScrollPosition {
        override val isScrollInProgress get() = state.isScrollInProgress
        override fun snapshot() = state.firstVisibleItemIndex.toLong() * LAZY_LIST_SCROLL_PACK +
                state.firstVisibleItemScrollOffset
    }

    data class Scroll(val state: ScrollState) : ScrollPosition {
        override val isScrollInProgress get() = state.isScrollInProgress
        override fun snapshot() = state.value.toLong()
    }
}

/**
 * Observes [source] and returns whether a floating control (e.g. bottom toolbar) should be visible:
 * hides while the user scrolls down, shows again when scrolling up.
 *
 * @param source Lazy list or scroll state to observe.
 * @param enabled When `false`, the returned state stays `true` (use for layouts where the bar is
 * vertical or always visible, e.g. side pane / landscape).
 * @param minDelta Minimum absolute change in [ScrollPosition.snapshot] before toggling; filters
 * small jitter.
 *
 * When [ScrollPosition.isScrollInProgress] is `false`, [ScrollPosition.snapshot] still advances the
 * internal baseline so programmatic jumps (e.g. [LazyListState.scrollToItem]) do not flip visibility.
 */
@Composable
internal fun rememberAutoHideOnScrollDown(
    source: ScrollPosition,
    enabled: Boolean = true,
    minDelta: Int = 4,
): State<Boolean> {
    val visible = remember { mutableStateOf(true) }
    LaunchedEffect(source, enabled, minDelta) {
        if (!enabled) {
            visible.value = true
            return@LaunchedEffect
        }
        var previous = source.snapshot()
        snapshotFlow { source.snapshot() to source.isScrollInProgress }
            .drop(1)
            .collectLatest { (current, inProgress) ->
                if (!inProgress) {
                    previous = current
                    return@collectLatest
                }
                val delta = current - previous
                when {
                    delta < -minDelta -> visible.value = true
                    delta > minDelta -> visible.value = false
                }
                previous = current
            }
    }
    return visible
}