package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class CustomScrollView extends ScrollView {

    private GestureDetector gestureDetector;

    View.OnTouchListener gestureListener;

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(new YScrollDetector());
        setFadingEdgeLength(0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //Call super first because it does some hidden motion event handling
        boolean result = super.onInterceptTouchEvent(ev);
        //Now see if we are scrolling vertically with the custom gesture detector
        if (gestureDetector.onTouchEvent(ev)) {
            return result;
        }
        //If not scrolling vertically (more y than x), don't hijack the event.
        else {
            return false;
        }
    }

    // Return false if we're scrolling in the x direction
    class YScrollDetector extends SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            try {
                if (Math.abs(distanceY) > Math.abs(distanceX)) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

        // Scrollen stoppen durch Tippen
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }
}
