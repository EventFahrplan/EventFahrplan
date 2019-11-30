package nerd.tuxmobil.fahrplan.congress.commons

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.DrawableRes
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet
import nerd.tuxmobil.fahrplan.congress.R


/**
 * TextView capable of rendering compound drawables for Android 4 (API 14) and newer.
 *
 * Use the following attributes in your layout if your need to support
 * Android 4.x (API 14-19) and Android 5 (API 20):
 *
 * - app:drawableLeftCompat
 * - app:drawableStartCompat
 * - app:drawableTopCompat
 * - app:drawableEndCompat
 * - app:drawableRightCompat
 * - app:drawableBottomCompat
 */
class TextViewCompat : AppCompatTextView {

    constructor(context: Context?) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        applyAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        applyAttributes(context, attrs)
    }

    private fun applyAttributes(context: Context, attrs: AttributeSet?) {
        if (attrs == null) {
            return
        }
        val attributeArray = context.obtainStyledAttributes(attrs, R.styleable.TextViewCompat)

        var drawableStart: Drawable? = null
        var drawableEnd: Drawable? = null
        var drawableBottom: Drawable? = null
        var drawableTop: Drawable? = null
        var drawableLeft: Drawable? = null
        var drawableRight: Drawable? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawableStart = attributeArray.getDrawable(R.styleable.TextViewCompat_drawableStartCompat)
                drawableEnd = attributeArray.getDrawable(R.styleable.TextViewCompat_drawableEndCompat)
                drawableBottom = attributeArray.getDrawable(R.styleable.TextViewCompat_drawableBottomCompat)
                drawableTop = attributeArray.getDrawable(R.styleable.TextViewCompat_drawableTopCompat)
                drawableLeft = attributeArray.getDrawable(R.styleable.TextViewCompat_drawableLeftCompat)
                drawableRight = attributeArray.getDrawable(R.styleable.TextViewCompat_drawableRightCompat)
            } else {
                val drawableStartResId = attributeArray.getResourceId(R.styleable.TextViewCompat_drawableStartCompat, UNDEFINED)
                val drawableEndResId = attributeArray.getResourceId(R.styleable.TextViewCompat_drawableEndCompat, UNDEFINED)
                val drawableBottomResId = attributeArray.getResourceId(R.styleable.TextViewCompat_drawableBottomCompat, UNDEFINED)
                val drawableTopResId = attributeArray.getResourceId(R.styleable.TextViewCompat_drawableTopCompat, UNDEFINED)
                val drawableLeftResId = attributeArray.getResourceId(R.styleable.TextViewCompat_drawableLeftCompat, UNDEFINED)
                val drawableRightResId = attributeArray.getResourceId(R.styleable.TextViewCompat_drawableRightCompat, UNDEFINED)

                if (drawableStartResId != UNDEFINED) {
                    drawableStart = context.getDrawableCompat(drawableStartResId)
                }
                if (drawableLeftResId != UNDEFINED) {
                    drawableLeft = context.getDrawableCompat(drawableLeftResId)
                }
                if (drawableEndResId != UNDEFINED) {
                    drawableEnd = context.getDrawableCompat(drawableEndResId)
                }
                if (drawableRightResId != UNDEFINED) {
                    drawableRight = context.getDrawableCompat(drawableRightResId)
                }
                if (drawableBottomResId != UNDEFINED) {
                    drawableBottom = context.getDrawableCompat(drawableBottomResId)
                }
                if (drawableTopResId != UNDEFINED) {
                    drawableTop = context.getDrawableCompat(drawableTopResId)
                }
            }
            val drawables = compoundDrawables
            drawableStart = drawableStart ?: drawables[START]
            drawableLeft = drawableLeft ?: drawables[LEFT]
            drawableStart = drawableStart ?: drawableLeft
            drawableEnd = drawableEnd ?: drawables[END]
            drawableRight = drawableRight ?: drawables[RIGHT]
            drawableEnd = drawableEnd ?: drawableRight
            drawableBottom = drawableBottom ?: drawables[BOTTOM]
            drawableTop = drawableTop ?: drawables[TOP]

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                setCompoundDrawablesRelativeWithIntrinsicBounds(drawableStart, drawableTop, drawableEnd, drawableBottom)
            } else {
                setCompoundDrawables(drawableStart, drawableTop, drawableEnd, drawableBottom)
            }
        } finally {
            attributeArray.recycle()
        }
    }

    private fun Context.getDrawableCompat(@DrawableRes drawable: Int): Drawable? =
            AppCompatResources.getDrawable(this, drawable)

    /**
     * The drawable indices correspond to the constants in
     * [android.widget.TextView]. See 'static class Drawables'.
     */
    companion object DrawableIndex {
        private const val UNDEFINED = -1
        private const val LEFT = 0
        private const val START = 0
        private const val TOP = 1
        private const val RIGHT = 2
        private const val END = 2
        private const val BOTTOM = 3
    }

}
