package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;


public class HorizontalSnapScrollView extends HorizontalScrollView {
	private static final String LOG_TAG = "HorizontalScrollView";
	private GestureDetector gestureDetector;
	private int activeItem = 0;
	private int xStart;

	/**
	 * get currently displayed column index
	 *
	 * @return index (0..n)
	 */
	public int getColumn() {
		return activeItem;
	}

	class YScrollDetector extends SimpleOnGestureListener {

	    @Override
	    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
	        try {
	            if (Math.abs(distanceX) > Math.abs(distanceY)) {
	                return true;
	            } else {
	                return false;
	            }
	        } catch (Exception e) {
	            // nothing
	        }
	        return false;
	    }

	    @Override
	    public boolean onDown(MotionEvent e) {
    		xStart = (int) e.getX();
//    		MyApp.LogDebug(LOG_TAG, "onDown xStart:"+xStart+" getMeasuredWidth:"+getMeasuredWidth());
    		return super.onDown(e);
	    }
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

	public void scrollToColumn(int col) {
		int max = (getChildAt(0).getMeasuredWidth()-getMeasuredWidth())/getMeasuredWidth();
		if ((col < 0) || (col > max)) col = activeItem;
		int scrollTo = col * getMeasuredWidth();
//		MyApp.LogDebug(LOG_TAG, "scroll to " + scrollTo + " " + getChildAt(0).getMeasuredWidth());
	    smoothScrollTo(scrollTo, 0);
        FahrplanFragment.updateRoomTitle(col);
        activeItem = col;
	}

	public HorizontalSnapScrollView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    gestureDetector = new GestureDetector(new YScrollDetector());
	    setOnTouchListener(new View.OnTouchListener() {

	            public boolean onTouch(View v, MotionEvent event) {
	                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL ){
	                    int scrollX = (int) event.getX();
	                    int itemWidth = getMeasuredWidth();
	                    int distance = scrollX - xStart;
//	                    MyApp.LogDebug(LOG_TAG, "item width:" + itemWidth + " scrollX:" + scrollX + " distance:" + distance + " activeItem:" + activeItem);
	                    int newItem = activeItem;
	                    if (Math.abs(distance) > (itemWidth/4)) {
		                    if (distance > 0) {
		                    	newItem = activeItem - 1;
		                    } else {
		                    	newItem = activeItem + 1;
		                    }
	                    }
	                    scrollToColumn(newItem);

	                    return true;
	                } else {
	                    return false;
	                }
	            }
	        });
	    }

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		MyApp.LogDebug(LOG_TAG, "onSizeChanged " + oldw + ", " + oldh + ", " + w + ", " + h);
		super.onSizeChanged(w, h, oldw, oldh);
		ViewGroup container = (ViewGroup) getChildAt(0);
		int childs = container.getChildCount();
		for (int i = 0; i < childs; i++) {
			ViewGroup c = (ViewGroup) container.getChildAt(i);
			ViewGroup.LayoutParams p = c.getLayoutParams();
			p.width = w;
			c.setLayoutParams(p);
		}
	}

}
