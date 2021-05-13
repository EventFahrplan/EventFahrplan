package nerd.tuxmobil.fahrplan.congress.schedule

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import nerd.tuxmobil.fahrplan.congress.R
import nerd.tuxmobil.fahrplan.congress.extensions.requireViewByIdCompat
import nerd.tuxmobil.fahrplan.congress.models.Session
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository
import nerd.tuxmobil.fahrplan.congress.utils.Font
import nerd.tuxmobil.fahrplan.congress.utils.TypefaceFactory

internal class SessionViewDrawer @JvmOverloads constructor(

        context: Context,
        private val getSessionPadding: () -> Int,
        private val isAlternativeHighlightingEnabled: () -> Boolean = {
            // Must load the latest alternative highlighting value every time a session is redrawn.
            AppRepository.readAlternativeHighlightingEnabled()
        }

) {

    private val resources = context.resources
    private val boldCondensed = TypefaceFactory.getNewInstance(context).getTypeface(Font.Roboto.BoldCondensed)
    private val sessionDrawableInsetTop = resources.getDimensionPixelSize(R.dimen.session_drawable_inset_top)
    private val sessionDrawableInsetLeft = resources.getDimensionPixelSize(R.dimen.session_drawable_inset_left)
    private val sessionDrawableInsetRight = resources.getDimensionPixelSize(R.dimen.session_drawable_inset_right)
    private val sessionDrawableCornerRadius = resources.getDimensionPixelSize(R.dimen.session_drawable_corner_radius)
    private val sessionDrawableStrokeWidth = resources.getDimensionPixelSize(R.dimen.session_drawable_selection_stroke_width)
    private val sessionDrawableStrokeColor = ContextCompat.getColor(context, R.color.session_drawable_selection_stroke)
    private val sessionDrawableRippleColor = ContextCompat.getColor(context, R.color.session_drawable_ripple)
    private val trackNameBackgroundColorDefaultPairs = TrackBackgrounds.getTrackNameBackgroundColorDefaultPairs(context)
    private val trackNameBackgroundColorHighlightPairs = TrackBackgrounds.getTrackNameBackgroundColorHighlightPairs(context)

    fun updateSessionView(sessionView: View, session: Session) {
        val bell = sessionView.requireViewByIdCompat<ImageView>(R.id.session_bell_view)
        bell.isVisible = session.hasAlarm
        var title = sessionView.requireViewByIdCompat<TextView>(R.id.session_title_view)
        title.typeface = boldCondensed
        title.text = session.title
        title = sessionView.requireViewByIdCompat(R.id.session_subtitle_view)
        title.text = session.subtitle
        title = sessionView.requireViewByIdCompat(R.id.session_speakers_view)
        title.text = session.formattedSpeakers
        title = sessionView.requireViewByIdCompat(R.id.session_track_view)
        title.text = session.formattedTrackLanguageText
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
        val sessionIsFavored = session.highlight
        @ColorRes val backgroundColorResId = if (sessionIsFavored) {
            trackNameBackgroundColorHighlightPairs[session.track] ?: R.color.track_background_highlight
        } else {
            trackNameBackgroundColorDefaultPairs[session.track] ?: R.color.track_background_default
        }
        @ColorInt val backgroundColor = ContextCompat.getColor(context, backgroundColorResId)
        val sessionDrawable = if (sessionIsFavored && isAlternativeHighlightingEnabled()) {
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
        ViewCompat.setBackground(sessionView, sessionDrawable)
        val padding = getSessionPadding()
        sessionView.setPadding(padding, padding, padding, padding)
    }

    companion object {
        const val LOG_TAG = "SessionViewDrawer"

        @JvmStatic
        fun setSessionTextColor(session: Session, view: View) {
            val title = view.requireViewByIdCompat<TextView>(R.id.session_title_view)
            val subtitle = view.requireViewByIdCompat<TextView>(R.id.session_subtitle_view)
            val speakers = view.requireViewByIdCompat<TextView>(R.id.session_speakers_view)
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