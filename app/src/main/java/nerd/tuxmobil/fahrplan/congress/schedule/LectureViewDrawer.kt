package nerd.tuxmobil.fahrplan.congress.schedule

import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.content.res.Resources
import android.graphics.Typeface
import android.preference.PreferenceManager
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import info.metadude.android.eventfahrplan.commons.logging.Logging
import info.metadude.android.eventfahrplan.commons.temporal.Moment
import nerd.tuxmobil.fahrplan.congress.MyApp
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.extensions.getLayoutInflater
import nerd.tuxmobil.fahrplan.congress.extensions.getNormalizedBoxHeight
import nerd.tuxmobil.fahrplan.congress.models.Lecture
import org.threeten.bp.Duration

internal class LectureViewDrawer(context: Context,
                                 private val onLectureClick: View.OnClickListener,
                                 private val onCreateContextMenu: View.OnCreateContextMenuListener,
                                 val conference: Conference
) {
    private val scale: Float
    private var standardHeight: Int
    private val resources = context.resources
    private val inflater = context.getLayoutInflater()
    private val boldCondensed = Typeface.createFromAsset(context.assets, "Roboto-BoldCondensed.ttf")
    private val eventDrawableInsetTop: Int
    private val eventDrawableInsetLeft: Int
    private val eventDrawableInsetRight: Int
    private val eventDrawableCornerRadius: Int
    private val eventDrawableStrokeWidth: Int
    private val eventDrawableStrokeColor: Int
    private val eventDrawableRippleColor: Int
    private val trackNameBackgroundColorDefaultPairs: Map<String, Int>
    private val trackNameBackgroundColorHighlightPairs: Map<String, Int>
    private val LOG_TAG = "LectureViewDrawer"

    private val eventPadding: Int
        get() {
            val factor = if (resources.configuration.orientation == ORIENTATION_LANDSCAPE) 8 else 10
            return (factor * scale).toInt()
        }

    init {
        scale = resources.displayMetrics.density
        eventDrawableInsetTop = resources.getDimensionPixelSize(
                R.dimen.event_drawable_inset_top)
        eventDrawableInsetLeft = resources.getDimensionPixelSize(
                R.dimen.event_drawable_inset_left)
        eventDrawableInsetRight = resources.getDimensionPixelSize(
                R.dimen.event_drawable_inset_right)
        eventDrawableCornerRadius = resources.getDimensionPixelSize(
                R.dimen.event_drawable_corner_radius)
        eventDrawableStrokeWidth = resources.getDimensionPixelSize(
                R.dimen.event_drawable_selection_stroke_width)
        eventDrawableStrokeColor = ContextCompat.getColor(
                FahrplanFragment.context, R.color.event_drawable_selection_stroke)
        eventDrawableRippleColor = ContextCompat.getColor(
                FahrplanFragment.context, R.color.event_drawable_ripple)
        trackNameBackgroundColorDefaultPairs = TrackBackgrounds.getTrackNameBackgroundColorDefaultPairs(context)
        trackNameBackgroundColorHighlightPairs = TrackBackgrounds.getTrackNameBackgroundColorHighlightPairs(context)
        standardHeight = resources.getNormalizedBoxHeight(resources.displayMetrics.density, LOG_TAG)
    }

    private fun updateEventView(eventView: View, lecture: Lecture) {
        val bell = eventView.findViewById<ImageView>(R.id.bell)
        bell.visibility = if (lecture.hasAlarm) View.VISIBLE else View.GONE
        var title = eventView.findViewById<TextView>(R.id.event_title)
        title.typeface = boldCondensed
        title.text = lecture.title
        title = eventView.findViewById(R.id.event_subtitle)
        title.text = lecture.subtitle
        title = eventView.findViewById(R.id.event_speakers)
        title.text = lecture.formattedSpeakers
        title = eventView.findViewById(R.id.event_track)
        title.text = lecture.formattedTrackText
        title.contentDescription = lecture.getFormattedTrackContentDescription(eventView.context)
        val recordingOptOut = eventView.findViewById<View>(R.id.novideo)
        if (recordingOptOut != null) {
            recordingOptOut.visibility = if (lecture.recordingOptOut) View.VISIBLE else View.GONE
        }
        setLectureBackground(lecture, eventView)
        setLectureTextColor(lecture, eventView)
        eventView.setOnClickListener(onLectureClick)
        eventView.setOnCreateContextMenuListener(onCreateContextMenu)
        eventView.isLongClickable = true
        eventView.tag = lecture
    }

    fun createLectureViews(room: LinearLayout, lectureLayoutParams: Map<Lecture, LinearLayout.LayoutParams>) {
        room.removeAllViews()

        for ((lecture, params) in lectureLayoutParams) {
            val eventView = inflater.inflate(R.layout.event_layout, null)
            val height = standardHeight * (lecture.duration / 5)
            room.addView(eventView, LinearLayout.LayoutParams.MATCH_PARENT, height)

            val lp = eventView.layoutParams as LinearLayout.LayoutParams
            lp.bottomMargin = params.bottomMargin
            lp.topMargin = params.topMargin
            eventView.layoutParams = lp

            updateEventView(eventView, lecture)
        }
    }

    fun calculateLayoutParams(roomIndex: Int, lectures: List<Lecture>): Map<Lecture, LinearLayout.LayoutParams> {
        var endTime: Int = conference.firstEventStartsAt
        var startTime: Int
        var margin: Int
        var previousLecture: Lecture? = null
        val lectureLayoutParams = mutableMapOf<Lecture, LinearLayout.LayoutParams>()

        for (idx in lectures.indices) {
            val lecture = lectures[idx]

            if (lecture.roomIndex == roomIndex) {
                if (lecture.dateUTC > 0) {
                    startTime = Moment(lecture.dateUTC).minuteOfDay
                    if (startTime < endTime) {
                        startTime += Duration.ofDays(1).toMinutes().toInt()
                    }
                } else {
                    startTime = lecture.relStartTime
                }
                if (startTime > endTime) {
                    margin = standardHeight * (startTime - endTime) / 5
                    if (previousLecture != null) {
                        lectureLayoutParams[previousLecture]!!.bottomMargin = margin
                        margin = 0
                    }
                } else {
                    margin = 0
                }

                // fix overlapping events
                var next: Lecture? = null
                for (nextIndex in idx + 1 until lectures.size) {
                    next = lectures[nextIndex]
                    if (next.roomIndex == roomIndex) {
                        break
                    }
                    next = null
                }
                if (next != null) {
                    if (next.dateUTC > 0) {
                        if (lecture.dateUTC + lecture.duration * 60000 > next.dateUTC) {
                            Logging.get().d(LOG_TAG, "${lecture.title} collides with ${next.title}")
                            lecture.duration = ((next.dateUTC - lecture.dateUTC) / 60000).toInt()
                        }
                    }
                }

                if (!lectureLayoutParams.containsKey(lecture)) {
                    lectureLayoutParams[lecture] = LinearLayout.LayoutParams(0, 0)
                }
                lectureLayoutParams[lecture]!!.topMargin = margin
                endTime = startTime + lecture.duration
                previousLecture = lecture
            }
        }


        return lectureLayoutParams
    }

    fun setLectureBackground(event: Lecture, eventView: View) {
        val context = eventView.context
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val defaultValue = resources.getBoolean(R.bool.preferences_alternative_highlight_enabled_default_value)
        val alternativeHighlightingIsEnabled = prefs.getBoolean(
                BundleKeys.PREFS_ALTERNATIVE_HIGHLIGHT, defaultValue)
        val eventIsFavored = event.highlight
        @ColorRes val backgroundColorResId: Int
        backgroundColorResId = if (eventIsFavored) {
            val colorResId = trackNameBackgroundColorHighlightPairs[event.track]
            colorResId ?: R.color.event_border_highlight
        } else {
            val colorResId = trackNameBackgroundColorDefaultPairs[event.track]
            colorResId ?: R.color.event_border_default
        }
        @ColorInt val backgroundColor = ContextCompat.getColor(context, backgroundColorResId)
        val eventDrawable: EventDrawable
        eventDrawable = if (eventIsFavored && alternativeHighlightingIsEnabled) {
            EventDrawable(
                    backgroundColor,
                    eventDrawableCornerRadius.toFloat(),
                    eventDrawableRippleColor,
                    eventDrawableStrokeColor,
                    eventDrawableStrokeWidth.toFloat())
        } else {
            EventDrawable(
                    backgroundColor,
                    eventDrawableCornerRadius.toFloat(),
                    eventDrawableRippleColor)
        }
        eventDrawable.setLayerInset(EventDrawable.BACKGROUND_LAYER_INDEX,
                eventDrawableInsetLeft,
                eventDrawableInsetTop,
                eventDrawableInsetRight,
                0)
        eventDrawable.setLayerInset(EventDrawable.STROKE_LAYER_INDEX,
                eventDrawableInsetLeft,
                eventDrawableInsetTop,
                eventDrawableInsetRight,
                0)
        eventView.setBackgroundDrawable(eventDrawable)
        val padding = eventPadding
        eventView.setPadding(padding, padding, padding, padding)
    }

    fun setLectureTextColor(lecture: Lecture, view: View) {
        val title = view.findViewById<TextView>(R.id.event_title)
        val subtitle = view.findViewById<TextView>(R.id.event_subtitle)
        val speakers = view.findViewById<TextView>(R.id.event_speakers)
        val colorResId = if (lecture.highlight) R.color.event_title_highlight else R.color.event_title
        val textColor = ContextCompat.getColor(view.context, colorResId)
        title.setTextColor(textColor)
        subtitle.setTextColor(textColor)
        speakers.setTextColor(textColor)
    }
}