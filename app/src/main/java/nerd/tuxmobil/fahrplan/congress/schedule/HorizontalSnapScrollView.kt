package nerd.tuxmobil.fahrplan.congress.schedule

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import androidx.annotation.IntRange
import androidx.annotation.VisibleForTesting
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.core.view.updateLayoutParams
import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.schedule.HorizontalSnapScrollView.Companion.SWIPE_MIN_DISTANCE
import nerd.tuxmobil.fahrplan.congress.schedule.HorizontalSnapScrollView.Companion.SWIPE_THRESHOLD_VELOCITY
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

class HorizontalSnapScrollView(context: Context?, attrs: AttributeSet?) : HorizontalScrollView(context, attrs) {

    companion object {
        private const val LOG_TAG = "HorizontalSnapScrollView"
        private const val FLING_COLUMN_MULTIPLIER = 3

        @VisibleForTesting
        const val SWIPE_MIN_DISTANCE = 5

        @VisibleForTesting
        const val SWIPE_THRESHOLD_VELOCITY = 2800

        /**
         * Calculates the number of columns to display at a time based on the physical dimensions and
         * the screen density of the device and the column count of the schedule. Further limiting
         * factors are a maximum column count to be displayed and a minimum column width.
         */
        @VisibleForTesting
        fun calculateDisplayColumnCount(
            availablePixels: Int,
            columnsCount: Int,
            maxColumnCountForLayout: Int,
            densityScaleFactor: Float,
            minColumnWidthDip: Int
        ): Int {
            val columnCountLimit = columnsCount.coerceAtMost(maxColumnCountForLayout)
            if (columnCountLimit == 1) {
                return 1
            }
            val availableDips = availablePixels.toFloat() / densityScaleFactor
            val minWidthColumnCount = floor((availableDips / minColumnWidthDip).toDouble()).toInt()
            val columnCount = minWidthColumnCount.coerceAtMost(columnCountLimit)
            return 1.coerceAtLeast(columnCount)
        }
    }

    private val logging = Logging.get()
    private val gestureDetector: GestureDetector
    private var horizontalSnapScrollState = HorizontalSnapScrollState(logging)
    private lateinit var roomNames: HorizontalScrollView

    /**
     * Get currently displayed column index
     * @return index (0..n)
     */
    val columnIndex: Int
        get() = horizontalSnapScrollState.activeColumnIndex

    init {
        horizontalSnapScrollState = horizontalSnapScrollState.copy(
            logging = horizontalSnapScrollState.logging,
            xStart = horizontalSnapScrollState.xStart,
            displayColumnCount = resources.getInteger(R.integer.max_cols),
            roomsCount = horizontalSnapScrollState.roomsCount,
            columnWidth = 0,
            activeColumnIndex = horizontalSnapScrollState.activeColumnIndex
        )
        gestureDetector = GestureDetector(YScrollDetector())
        setOnTouchListener(OnTouchListener())
    }

    internal inner class YScrollDetector : SimpleOnGestureListener() {
        override fun onDown(motionEvent: MotionEvent): Boolean {
            val xStart = motionEvent.x.toInt()
            logging.d(LOG_TAG, "onDown -> xStart: $xStart, measuredWidth: $measuredWidth")
            val ofs = (scrollX * horizontalSnapScrollState.displayColumnCount).toFloat() / measuredWidth
            horizontalSnapScrollState = horizontalSnapScrollState.copy(
                logging = horizontalSnapScrollState.logging,
                xStart = xStart,
                displayColumnCount = horizontalSnapScrollState.displayColumnCount,
                roomsCount = horizontalSnapScrollState.roomsCount,
                columnWidth = horizontalSnapScrollState.columnWidth,
                activeColumnIndex = ofs.roundToInt()
            )
            return super.onDown(motionEvent)
        }

        override fun onFling(
            start: MotionEvent,
            end: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val normalizedVelocityX = velocityX / resources.displayMetrics.density
            val columns = ceil((normalizedVelocityX / SWIPE_THRESHOLD_VELOCITY * FLING_COLUMN_MULTIPLIER).toDouble()).toInt()

            logging.d(LOG_TAG, "onFling -> $velocityX/$velocityY $normalizedVelocityX $columns")

            if (checkScrollDistance(start.x, end.x) && checkFlingVelocity(normalizedVelocityX)) {
                scrollToColumn(horizontalSnapScrollState.activeColumnIndex - columns, fast = false)
                return true
            }

            return super.onFling(start, end, velocityX, velocityY)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onInterceptTouchEvent(event)
    }

    /**
     * Sets the rooms count so it can be used to calculate
     * the room column dimensions in the next layout phase.
     */
    fun setRoomsCount(@IntRange(from = 1) roomsCount: Int) {
        horizontalSnapScrollState = horizontalSnapScrollState.copy(
            logging = horizontalSnapScrollState.logging,
            xStart = horizontalSnapScrollState.xStart,
            displayColumnCount = horizontalSnapScrollState.displayColumnCount,
            roomsCount = roomsCount,
            columnWidth = horizontalSnapScrollState.columnWidth,
            activeColumnIndex = horizontalSnapScrollState.activeColumnIndex
        )
    }

    fun scrollToColumn(col: Int, fast: Boolean) {
        val scrollTo = horizontalSnapScrollState.calculateScrollToXPosition(col)
        logging.d(LOG_TAG, "scroll to col $col/$scrollTo ${getChildAt(0).measuredWidth}")
        if (fast) {
            scrollTo(scrollTo, 0)
            roomNames.scrollTo(scrollTo, 0)
        } else {
            post { smoothScrollTo(scrollTo, 0) }
        }
        horizontalSnapScrollState = horizontalSnapScrollState.copy(
            logging = horizontalSnapScrollState.logging,
            xStart = horizontalSnapScrollState.xStart,
            displayColumnCount = horizontalSnapScrollState.displayColumnCount,
            roomsCount = horizontalSnapScrollState.roomsCount,
            columnWidth = horizontalSnapScrollState.columnWidth,
            activeColumnIndex = col
        )
    }

    private inner class OnTouchListener : View.OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            logging.d(LOG_TAG, "onTouch")
            if (gestureDetector.onTouchEvent(event)) {
                // logging.d(LOG_TAG, "gesture detector consumed event")
                return false
            } else if (event.action == ACTION_UP || event.action == ACTION_CANCEL) {
                val scrollX = event.x.toInt()
                val columnIndex = horizontalSnapScrollState.calculateOnTouchColumnIndex(scrollX)
                scrollToColumn(columnIndex, fast = false)
                return true
            }
            return false
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        logging.d(LOG_TAG, "onSizeChanged -> $oldWidth, $oldHeight, $width, $height, measuredWidth: $measuredWidth")
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        val displayColumnCount = if (horizontalSnapScrollState.isRoomsCountInitialized()) {
            calculateDisplayColumnCount(
                availablePixels = measuredWidth,
                columnsCount = horizontalSnapScrollState.roomsCount,
                maxColumnCountForLayout = resources.getInteger(R.integer.max_cols),
                densityScaleFactor = resources.displayMetrics.density,
                minColumnWidthDip = resources.getInteger(R.integer.min_width_dip)
            )
        } else {
            1
        }
        horizontalSnapScrollState = horizontalSnapScrollState.copy(
            logging = horizontalSnapScrollState.logging,
            xStart = horizontalSnapScrollState.xStart,
            displayColumnCount = displayColumnCount,
            roomsCount = horizontalSnapScrollState.roomsCount,
            columnWidth = horizontalSnapScrollState.columnWidth,
            activeColumnIndex = horizontalSnapScrollState.activeColumnIndex
        )
        val newItemWidth = horizontalSnapScrollState.calculateColumnWidth(measuredWidth)
        logging.d(LOG_TAG, """item width: $newItemWidth ${newItemWidth.toFloat() / resources.displayMetrics.density}dp""")
        columnWidth = newItemWidth
    }

    override fun onScrollChanged(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY)
        // logging.d(LOG_TAG, "onScrollChanged -> scrolled from $oldScrollX to $scrollX")
        roomNames.scrollTo(scrollX, 0)
    }

    fun setChildScroller(titleScrollView: HorizontalScrollView) {
        roomNames = titleScrollView
    }

    var columnWidth: Int
        get() {
            check(horizontalSnapScrollState.isRoomsCountInitialized()) {
                """The "roomsCount" field must be initialized before invoking "getColumnWidth"."""
            }
            return horizontalSnapScrollState.columnWidth
        }
        private set(pixels) {
            logging.d(LOG_TAG, "setColumnWidth: $pixels")
            horizontalSnapScrollState = horizontalSnapScrollState.copy(
                logging = horizontalSnapScrollState.logging,
                xStart = horizontalSnapScrollState.xStart,
                displayColumnCount = horizontalSnapScrollState.displayColumnCount,
                roomsCount = horizontalSnapScrollState.roomsCount,
                columnWidth = pixels,
                activeColumnIndex = horizontalSnapScrollState.activeColumnIndex
            )

            if (pixels == 0) {
                return
            }

            val container = firstChild
            container.forEach {
                it.updateLayoutParams {
                    width = horizontalSnapScrollState.columnWidth
                }
            }
            container.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )

            roomNames.firstChild.children.forEach {
                it.updateLayoutParams {
                    width = horizontalSnapScrollState.columnWidth
                }
            }

            scrollToColumn(horizontalSnapScrollState.activeColumnIndex, fast = true)
        }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        scrollToColumn(horizontalSnapScrollState.activeColumnIndex, fast = true)
    }
}

private val ViewGroup.firstChild
    get() = get(0) as ViewGroup

/**
 * Checks if the velocity of a fling event was big enough to initiate a fling-scroll.
 * @param normalizedVelocity the x velocity of the fling independent from the display pixel density
 */
@VisibleForTesting
fun checkFlingVelocity(normalizedVelocity: Float) =
    abs(normalizedVelocity) > SWIPE_THRESHOLD_VELOCITY

/**
 * Checks if the distance between two x-values is big enough for a fling-scroll.
 */
@VisibleForTesting
fun checkScrollDistance(startX: Float, endX: Float) =
    abs(startX - endX) > SWIPE_MIN_DISTANCE
