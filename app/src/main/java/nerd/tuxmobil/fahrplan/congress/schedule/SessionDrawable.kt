package nerd.tuxmobil.fahrplan.congress.schedule

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import androidx.annotation.ColorInt

class SessionDrawable @JvmOverloads constructor(

    @ColorInt backgroundColor: Int,
    cornerRadius: Float,
    @ColorInt rippleColor: Int,
    @ColorInt strokeColor: Int = Color.TRANSPARENT,
    strokeWidth: Float = 0f

) : LayerDrawable(
    setupLayers(backgroundColor, cornerRadius, rippleColor, strokeColor, strokeWidth)
) {

    companion object {

        const val BACKGROUND_LAYER_INDEX = 0
        const val STROKE_LAYER_INDEX = 1

        private fun setupLayers(
            @ColorInt backgroundColor: Int,
            cornerRadius: Float,
            @ColorInt rippleColor: Int,
            @ColorInt strokeColor: Int,
            strokeWidth: Float
        ): Array<Drawable> {

            val radii = FloatArray(8) { cornerRadius }

            // Background
            val backgroundShape = RoundRectShape(radii, null, null)
            val backgroundDrawable = ShapeDrawable(backgroundShape).apply { paint.color = backgroundColor }

            // Stroke
            val strokeInset = RectF(strokeWidth, strokeWidth, strokeWidth, strokeWidth)
            val strokeShape = RoundRectShape(radii, strokeInset, radii)
            val strokeDrawable = ShapeDrawable(strokeShape).apply { paint.color = strokeColor }

            // Ripples
            val backgroundRippleDrawable = RippleDrawable(
                    ColorStateList.valueOf(rippleColor), backgroundDrawable, backgroundDrawable
                )

            // Layers
            return Array(2) {
                when (it) {
                    BACKGROUND_LAYER_INDEX -> backgroundRippleDrawable
                    STROKE_LAYER_INDEX -> strokeDrawable
                    else -> error("Array must only have two entries.")
                }
            }
        }

    }

}
