package nerd.tuxmobil.fahrplan.congress.schedule;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.support.annotation.ColorInt;

import java.util.Arrays;

public class EventDrawable extends LayerDrawable {

    public final static int BACKGROUND_LAYER_INDEX = 0;

    public final static int STROKE_LAYER_INDEX = 1;

    public EventDrawable(@ColorInt int backgroundColor, float cornerRadius,
                         @ColorInt int rippleColor) {
        this(backgroundColor, cornerRadius,
                rippleColor,
                Color.TRANSPARENT, 0);
    }

    public EventDrawable(@ColorInt int backgroundColor, float cornerRadius,
                         @ColorInt int rippleColor,
                         @ColorInt int strokeColor, float strokeWidth) {
        super(setupLayers(backgroundColor, cornerRadius,
                rippleColor,
                strokeColor, strokeWidth));
    }

    private static Drawable[] setupLayers(
            @ColorInt int backgroundColor, float cornerRadius,
            @ColorInt int rippleColor,
            @ColorInt int strokeColor, float strokeWidth) {

        float[] radii = new float[8];
        Arrays.fill(radii, cornerRadius);

        // Background
        RoundRectShape backgroundShape = new RoundRectShape(radii, null, null);
        ShapeDrawable backgroundDrawable = new ShapeDrawable(backgroundShape);
        backgroundDrawable.getPaint().setColor(backgroundColor);

        // Stroke
        //noinspection SuspiciousNameCombination
        RectF strokeInset = new RectF(strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        RoundRectShape strokeShape = new RoundRectShape(radii, strokeInset, radii);
        ShapeDrawable strokeDrawable = new ShapeDrawable(strokeShape);
        strokeDrawable.getPaint().setColor(strokeColor);

        // Ripples
        Drawable backgroundRippleDrawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            backgroundRippleDrawable = new RippleDrawable(
                    ColorStateList.valueOf(rippleColor), backgroundDrawable, backgroundDrawable);
        } else {
            backgroundRippleDrawable = backgroundDrawable;
        }

        // Layers
        Drawable[] layers = new Drawable[2];
        layers[BACKGROUND_LAYER_INDEX] = backgroundRippleDrawable;
        layers[STROKE_LAYER_INDEX] = strokeDrawable;
        return layers;
    }

}
