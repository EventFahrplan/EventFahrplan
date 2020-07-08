package nerd.tuxmobil.fahrplan.congress.schedule

import android.content.Context
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.graphics.Typeface
import android.preference.PreferenceManager
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.models.Lecture

internal class LectureViewDrawer(

        context: Context

) {

    private val scale: Float
    private val resources = context.resources
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
                context, R.color.event_drawable_selection_stroke)
        eventDrawableRippleColor = ContextCompat.getColor(
                context, R.color.event_drawable_ripple)
        trackNameBackgroundColorDefaultPairs = TrackBackgrounds.getTrackNameBackgroundColorDefaultPairs(context)
        trackNameBackgroundColorHighlightPairs = TrackBackgrounds.getTrackNameBackgroundColorHighlightPairs(context)
    }

    fun updateEventView(eventView: View, lecture: Lecture) {
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
        eventView.tag = lecture
    }

    fun setLectureBackground(event: Lecture, eventView: View) {
        val context = eventView.context
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val defaultValue = resources.getBoolean(R.bool.preferences_alternative_highlight_enabled_default_value)
        val alternativeHighlightingIsEnabled = prefs.getBoolean(
                BundleKeys.PREFS_ALTERNATIVE_HIGHLIGHT, defaultValue)
        val eventIsFavored = event.highlight
        @ColorRes val backgroundColorResId = if (eventIsFavored) {
            trackNameBackgroundColorHighlightPairs[event.track] ?: R.color.event_border_highlight
        } else {
            trackNameBackgroundColorDefaultPairs[event.track] ?: R.color.event_border_default
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

    companion object {
        const val LOG_TAG = "LectureViewDrawer"

        @JvmStatic
        fun setLectureTextColor(lecture: Lecture, view: View) {
            val title = view.findViewById<TextView>(R.id.event_title)
            val subtitle = view.findViewById<TextView>(R.id.event_subtitle)
            val speakers = view.findViewById<TextView>(R.id.event_speakers)
            val colorResId = if (lecture.highlight)
                R.color.event_title_on_highlight_background
            else
                R.color.event_title_on_default_background
            val textColor = ContextCompat.getColor(view.context, colorResId)
            title.setTextColor(textColor)
            subtitle.setTextColor(textColor)
            speakers.setTextColor(textColor)
        }
    }
}