package nerd.tuxmobil.fahrplan.congress.schedule

import android.content.Context
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.graphics.Typeface
import android.preference.PreferenceManager
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.contract.BundleKeys
import nerd.tuxmobil.fahrplan.congress.models.Session

internal class SessionViewDrawer(

        context: Context

) {

    private val scale: Float
    private val resources = context.resources
    private val boldCondensed = Typeface.createFromAsset(context.assets, "Roboto-BoldCondensed.ttf")
    private val sessionDrawableInsetTop: Int
    private val sessionDrawableInsetLeft: Int
    private val sessionDrawableInsetRight: Int
    private val sessionDrawableCornerRadius: Int
    private val sessionDrawableStrokeWidth: Int
    private val sessionDrawableStrokeColor: Int
    private val sessionDrawableRippleColor: Int
    private val trackNameBackgroundColorDefaultPairs: Map<String, Int>
    private val trackNameBackgroundColorHighlightPairs: Map<String, Int>

    private val sessionPadding: Int
        get() {
            val factor = if (resources.configuration.orientation == ORIENTATION_LANDSCAPE) 8 else 10
            return (factor * scale).toInt()
        }

    init {
        scale = resources.displayMetrics.density
        sessionDrawableInsetTop = resources.getDimensionPixelSize(
                R.dimen.session_drawable_inset_top)
        sessionDrawableInsetLeft = resources.getDimensionPixelSize(
                R.dimen.session_drawable_inset_left)
        sessionDrawableInsetRight = resources.getDimensionPixelSize(
                R.dimen.session_drawable_inset_right)
        sessionDrawableCornerRadius = resources.getDimensionPixelSize(
                R.dimen.session_drawable_corner_radius)
        sessionDrawableStrokeWidth = resources.getDimensionPixelSize(
                R.dimen.session_drawable_selection_stroke_width)
        sessionDrawableStrokeColor = ContextCompat.getColor(
                context, R.color.session_drawable_selection_stroke)
        sessionDrawableRippleColor = ContextCompat.getColor(
                context, R.color.session_drawable_ripple)
        trackNameBackgroundColorDefaultPairs = TrackBackgrounds.getTrackNameBackgroundColorDefaultPairs(context)
        trackNameBackgroundColorHighlightPairs = TrackBackgrounds.getTrackNameBackgroundColorHighlightPairs(context)
    }

    fun updateSessionView(sessionView: View, session: Session) {
        val bell = sessionView.findViewById<ImageView>(R.id.session_bell_view)
        bell.isVisible = session.hasAlarm
        var title = sessionView.findViewById<TextView>(R.id.session_title_view)
        title.typeface = boldCondensed
        title.text = session.title
        title = sessionView.findViewById(R.id.session_subtitle_view)
        title.text = session.subtitle
        title = sessionView.findViewById(R.id.session_speakers_view)
        title.text = session.formattedSpeakers
        title = sessionView.findViewById(R.id.session_track_view)
        title.text = session.formattedTrackText
        title.contentDescription = session.getFormattedTrackContentDescription(sessionView.context)
        val recordingOptOut = sessionView.findViewById<View>(R.id.session_no_video_view)
        if (recordingOptOut != null) {
            recordingOptOut.isVisible = session.recordingOptOut
        }
        setSessionBackground(session, sessionView)
        setSessionTextColor(session, sessionView)
        sessionView.tag = session
    }

    fun setSessionBackground(session: Session, sessionView: View) {
        val context = sessionView.context
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val defaultValue = resources.getBoolean(R.bool.preferences_alternative_highlight_enabled_default_value)
        val alternativeHighlightingIsEnabled = prefs.getBoolean(
                BundleKeys.PREFS_ALTERNATIVE_HIGHLIGHT, defaultValue)
        val sessionIsFavored = session.highlight
        @ColorRes val backgroundColorResId = if (sessionIsFavored) {
            trackNameBackgroundColorHighlightPairs[session.track] ?: R.color.track_background_highlight
        } else {
            trackNameBackgroundColorDefaultPairs[session.track] ?: R.color.track_background_default
        }
        @ColorInt val backgroundColor = ContextCompat.getColor(context, backgroundColorResId)
        val sessionDrawable: SessionDrawable
        sessionDrawable = if (sessionIsFavored && alternativeHighlightingIsEnabled) {
            SessionDrawable(
                    backgroundColor,
                    sessionDrawableCornerRadius.toFloat(),
                    sessionDrawableRippleColor,
                    sessionDrawableStrokeColor,
                    sessionDrawableStrokeWidth.toFloat())
        } else {
            SessionDrawable(
                    backgroundColor,
                    sessionDrawableCornerRadius.toFloat(),
                    sessionDrawableRippleColor)
        }
        sessionDrawable.setLayerInset(SessionDrawable.BACKGROUND_LAYER_INDEX,
                sessionDrawableInsetLeft,
                sessionDrawableInsetTop,
                sessionDrawableInsetRight,
                0)
        sessionDrawable.setLayerInset(SessionDrawable.STROKE_LAYER_INDEX,
                sessionDrawableInsetLeft,
                sessionDrawableInsetTop,
                sessionDrawableInsetRight,
                0)
        sessionView.setBackgroundDrawable(sessionDrawable)
        val padding = sessionPadding
        sessionView.setPadding(padding, padding, padding, padding)
    }

    companion object {
        const val LOG_TAG = "SessionViewDrawer"

        @JvmStatic
        fun setSessionTextColor(session: Session, view: View) {
            val title = view.findViewById<TextView>(R.id.session_title_view)
            val subtitle = view.findViewById<TextView>(R.id.session_subtitle_view)
            val speakers = view.findViewById<TextView>(R.id.session_speakers_view)
            val colorResId = if (session.highlight)
                R.color.session_title_on_highlight_background
            else
                R.color.session_title_on_default_background
            val textColor = ContextCompat.getColor(view.context, colorResId)
            title.setTextColor(textColor)
            subtitle.setTextColor(textColor)
            speakers.setTextColor(textColor)
        }
    }
}