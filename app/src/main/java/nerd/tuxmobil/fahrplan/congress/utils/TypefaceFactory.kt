package nerd.tuxmobil.fahrplan.congress.utils

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Typeface

/**
 * Provides the [Typeface] associated with a given [Font].
 *
 * Look-up is cached in this class.
 */
class TypefaceFactory private constructor(

        private val assetManager: AssetManager

) {

    companion object {

        fun getNewInstance(context: Context) = TypefaceFactory(context.assets)

    }

    private val typefaceByFont = mutableMapOf<Font, Typeface>()

    fun getTypeface(font: Font): Typeface {
        return if (typefaceByFont.contains(font)) {
            typefaceByFont[font]!!
        } else {
            val typeface = Typeface.createFromAsset(assetManager, font.fileName)
            typefaceByFont[font] = typeface
            typeface
        }
    }

}
