package nerd.tuxmobil.fahrplan.congress.schedule

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import androidx.annotation.IntRange
import androidx.annotation.VisibleForTesting
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.updateLayoutParams
import info.metadude.android.eventfahrplan.commons.logging.Logging
import nerd.tuxmobil.fahrplan.congress.R
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class HorizontalSnapScrollView(
    context: Context,
    attrs: AttributeSet
) : HorizontalScrollView(context, attrs) {

    companion object {
        private const val LOG_TAG = "HorizontalScrollView"
        private const val NOT_INITIALIZED = Int.MIN_VALUE
        private const val SWIPE_MIN_DISTANCE = 5
        private const val SWIPE_THRESHOLD_VELOCITY = 2800
        private const val MIN_PORTRAIT_SCROLLING_PERCENTAGE = 0.25

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
            val columnCountLimit = min(columnsCount, maxColumnCountForLayout)
            if (columnCountLimit == 1) {
                return 1
            }
            val availableDips = availablePixels.toFloat() / densityScaleFactor
            val minWidthColumnCount = floor((availableDips / minColumnWidthDip).toDouble()).toInt()
            val columnCount = min(minWidthColumnCount, columnCountLimit)
            return max(1, columnCount)
        }
    }

    private val gestureDetector: GestureDetector
    private val logging: Logging = Logging.get()
    private var xStart = 0
    private var displayColumnCount: Int
    private lateinit var roomNames: HorizontalScrollView

    private var roomsCount = NOT_INITIALIZED

    /**
     * Sets the rooms count so it can be used to calculate
     * the room column dimensions in the next layout phase.
     */
    fun setRoomsCount(@IntRange(from = 1) roomsCount: Int) {
        this.roomsCount = roomsCount
    }

    private var columnWidth: Int
    fun getColumnWidth(): Int {
        check(roomsCount != NOT_INITIALIZED){ """The "roomsCount" field must be initialized before invoking "getColumnWidth".""" }
        return columnWidth
    }

    // The currently displayed column index
    var activeColumnIndex = 0
        private set

    init {
        displayColumnCount = resources.getInteger(R.integer.max_cols)
        columnWidth = 0
        gestureDetector = GestureDetector(context, YScrollDetector())
        setOnTouchListener(OnTouchListener())
    }

    inner class YScrollDetector : SimpleOnGestureListener() {
        override fun onDown(event: MotionEvent): Boolean {
            xStart = event.x.toInt()
//            logging.d(LOG_TAG, """onDown xStart: ${xStart} getMeasuredWidth: ${getMeasuredWidth()}""");
            activeColumnIndex = ((scrollX * displayColumnCount).toFloat() / measuredWidth).roundToInt()
            return super.onDown(event)
        }

        override fun onFling(
            event1: MotionEvent,
            event2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val normalizedAbsoluteVelocityX = abs(velocityX / resources.displayMetrics.density)
            val columns = ceil((normalizedAbsoluteVelocityX / SWIPE_THRESHOLD_VELOCITY * 3).toDouble()).toInt()
//            logging.d(LOG_TAG, """onFling ${velocityX}/${velocityY} ${velocityX/resources.displayMetrics.density} ${columns}""");
            val exceedsVelocityThreshold = normalizedAbsoluteVelocityX > SWIPE_THRESHOLD_VELOCITY

            try {
                //right to left
                if (event1.x - event2.x > SWIPE_MIN_DISTANCE && exceedsVelocityThreshold) {
                    scrollToColumn(activeColumnIndex + columns, false)
                    return true
                }
                //left to right
                else if (event2.x - event1.x > SWIPE_MIN_DISTANCE && exceedsVelocityThreshold) {
                    scrollToColumn(activeColumnIndex - columns, false)
                    return true
                }
            } catch (exception: Exception) {
                logging.d(LOG_TAG, """There was an error processing the Fling event:${exception.message}""")
            }

            return super.onFling(event1, event2, velocityX, velocityY)
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onInterceptTouchEvent(event)
    }

    fun scrollToColumn(col: Int, fast: Boolean) {
        val maxColumnIndex = roomsCount - displayColumnCount
        val columnIndex = min(max(col, 0), maxColumnIndex)

        val scrollTo = columnIndex * columnWidth

        logging.d(LOG_TAG, """scroll to col ${columnIndex}/${scrollTo} ${firstChild.measuredWidth}""")

        if (fast) {
            scrollTo(scrollTo, 0)
            roomNames.scrollTo(scrollTo, 0)
        } else {
            post { smoothScrollTo(scrollTo, 0) }
        }
        activeColumnIndex = columnIndex
    }

    private inner class OnTouchListener : View.OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            logging.d(LOG_TAG, "onTouch");
            if (gestureDetector.onTouchEvent(event)) {
//                logging.d(LOG_TAG, "gesture detector consumed event");
                return false
            } else if (
                event.action == MotionEvent.ACTION_UP ||
                event.action == MotionEvent.ACTION_CANCEL
            ) {
                val scrollX = event.x.toInt()
                val distance = scrollX - xStart

                var tempColumnIndex = activeColumnIndex

                logging.d(
                    LOG_TAG,
                    "column width:$columnWidth " +
                            "scrollX:$scrollX " +
                            "distance:$distance " +
                            "active column index:$activeColumnIndex"
                )

                if (displayColumnCount > 1) {
                    val columnDistance = (abs(distance.toFloat()) / columnWidth).roundToInt()

                    logging.d(LOG_TAG, "column distance: $columnDistance")

                    tempColumnIndex = if (distance > 0) {
                        activeColumnIndex - columnDistance
                    } else {
                        activeColumnIndex + columnDistance
                    }
                } else {
                    if (abs(distance) > columnWidth * MIN_PORTRAIT_SCROLLING_PERCENTAGE) {
                        tempColumnIndex = if (distance > 0) {
                            activeColumnIndex - 1
                        } else {
                            activeColumnIndex + 1
                        }
                    }
                }
                scrollToColumn(tempColumnIndex, false)
                return true
            } else return false
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        logging.d(LOG_TAG,"onSizeChanged $oldWidth, $oldHeight, $width, $height getMW:$measuredWidth")
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        displayColumnCount = if (roomsCount == NOT_INITIALIZED) {
            1
        } else {
            calculateDisplayColumnCount(
                measuredWidth,
                roomsCount,
                resources.getInteger(R.integer.max_cols),
                resources.displayMetrics.density,
                resources.getInteger(R.integer.min_width_dip)
            )
        }
        val newItemWidth = (measuredWidth.toFloat() / displayColumnCount).roundToInt()
        logging.d(LOG_TAG, """item width: $newItemWidth ${newItemWidth.toFloat() / resources.displayMetrics.density}dp""")
        setColumnWidth(newItemWidth)
    }

    override fun onScrollChanged(scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int) {
        super.onScrollChanged(scrollX, scrollY, oldScrollX, oldScrollY)
//        logging.d(LOG_TAG, """scrolled from ${oldScrollX} to ${scrollX}""");
        roomNames.scrollTo(scrollX, 0)
    }

    fun setChildScroller(scrollView: HorizontalScrollView) {
        scrollView.setOnTouchListener { _, _ -> true }
        roomNames = scrollView
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        scrollToColumn(activeColumnIndex, true)
    }

    private fun setColumnWidth(pixels: Int) {
        logging.d(LOG_TAG, "setColumnWidth $pixels")
        columnWidth = pixels
        if (pixels == 0) {
            return
        }

        val container = firstChild
        roomNames.firstChild.children.forEach { it.updateLayoutParams { width = columnWidth } }
        container.children.forEach { it.updateLayoutParams { width = columnWidth } }
        container.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )

        scrollToColumn(activeColumnIndex, true)
    }
}

private val ViewGroup.firstChild
    get() = get(0) as ViewGroup