package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;


public class HorizontalSnapScrollView extends HorizontalScrollView {
	private static final String LOG_TAG = "HorizontalScrollView";
	private GestureDetector gestureDetector;
	private int activeItem = 0;
	private int xStart;
	private int itemWidth;
	private HorizontalScrollView roomNames = null;
	private int max_cols;

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
    		float ofs = (float)(getScrollX() * max_cols)/getMeasuredWidth();
    		activeItem = Math.round(ofs);
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

	public void scrollToColumn(int col, boolean fast) {
		int max;
		if (itemWidth == 0) max = 0; else max = (getChildAt(0).getMeasuredWidth()-itemWidth)/itemWidth;
		if (col < 0) col = 0;
		if (col > max) col = max;
		int scrollTo = col * itemWidth;
		MyApp.LogDebug(LOG_TAG, "scroll to col " + col + "/" + scrollTo + " " + getChildAt(0).getMeasuredWidth());
		if (!fast) {
		    smoothScrollTo(scrollTo, 0);
		} else {
			scrollTo(scrollTo, 0);
			if (roomNames != null) roomNames.scrollTo(scrollTo, 0);
		}
        activeItem = col;
	}

	public HorizontalSnapScrollView(Context context, AttributeSet attrs) {
	    super(context, attrs);
		max_cols = getResources().getInteger(R.integer.max_cols);
		itemWidth = 0;
	    gestureDetector = new GestureDetector(new YScrollDetector());
	    setOnTouchListener(new View.OnTouchListener() {

	            public boolean onTouch(View v, MotionEvent event) {
	                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL ){
	                    int scrollX = (int) event.getX();
	                    int distance = scrollX - xStart;
	                    MyApp.LogDebug(LOG_TAG, "item width:" + itemWidth + " scrollX:" + scrollX + " distance:" + distance + " activeItem:" + activeItem);
	                    int newItem = activeItem;
						int col_dist;
	                    if (max_cols > 1) {
	                    	col_dist = (int) Math.round(Math.abs((float)distance)/itemWidth);
	                    	MyApp.LogDebug(LOG_TAG, "col dist: " + col_dist);
		                    if (distance > 0) {
		                    	newItem = activeItem - col_dist;
		                    } else {
		                    	newItem = activeItem + col_dist;
		                    }
	                    } else {
		                    if (Math.abs(distance) > itemWidth/4) {
			                    if (distance > 0) {
			                    	newItem = activeItem - 1;
			                    } else {
			                    	newItem = activeItem + 1;
			                    }
		                    }
	                    }
	                    scrollToColumn(newItem, false);

	                    return true;
	                } else {
	                    return false;
	                }
	            }
	        });
	    }

	public static int calcMaxCols(Resources res, int availPixels) {
		int max_cols = res.getInteger(R.integer.max_cols);
		int min_dip = res.getInteger(R.integer.min_width_dip);
		float scale = res.getDisplayMetrics().density;
		MyApp.LogDebug(LOG_TAG, "calcMaxCols: avail " + availPixels + " min dip " + min_dip);
		int dip;
		do {
			dip = (int) ((((float)availPixels)/max_cols)/scale);
			MyApp.LogDebug(LOG_TAG, "calcMaxCols: " + dip + " on " + max_cols + " cols.");
			max_cols--;
		} while ((dip < min_dip) && (max_cols > 0));
		return max_cols + 1;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		MyApp.LogDebug(LOG_TAG, "onSizeChanged " + oldw + ", " + oldh + ", " + w + ", " + h + " getMW:" + getMeasuredWidth());
		super.onSizeChanged(w, h, oldw, oldh);
		max_cols = calcMaxCols(getResources(), getMeasuredWidth());

		int newItemWidth = Math.round((float)getMeasuredWidth()/max_cols);
		float scale = getResources().getDisplayMetrics().density;

		MyApp.LogDebug(LOG_TAG, "item width: " + newItemWidth + " " + ((float)newItemWidth)/scale + "dp");
		setColumnWidth(newItemWidth);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
//		MyApp.LogDebug(LOG_TAG, "scrolled from " + oldl + " to " + l);
		if (roomNames != null) roomNames.scrollTo(l, 0);
	}

	public void setChildScroller(HorizontalScrollView h) {
		MyApp.LogDebug(LOG_TAG, "roomNames="+h);
		roomNames = h;
	}

	public int getColumnWidth() {
		MyApp.LogDebug(LOG_TAG, "getColumnWidth: " + itemWidth);
		return itemWidth;
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		MyApp.LogDebug(LOG_TAG, "onLayout " + changed + "/" + l + "/" + r);
		super.onLayout(changed, l, t, r, b);
		scrollToColumn(activeItem, true);
	}

	public void setColumnWidth(int pixels) {

		MyApp.LogDebug(LOG_TAG, "setColumnWidth " + pixels);
		itemWidth = pixels;
		if (pixels == 0) return;

		ViewGroup container = (ViewGroup) getChildAt(0);
		int childs = container.getChildCount();
		for (int i = 0; i < childs; i++) {
			ViewGroup c = (ViewGroup) container.getChildAt(i);
			ViewGroup.LayoutParams p = c.getLayoutParams();
			p.width = itemWidth;
			c.setLayoutParams(p);
		}
		container.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		if (roomNames != null) {
			View v;
			int numChilds = ((ViewGroup)roomNames.getChildAt(0)).getChildCount();

			for (int i = 0; i < numChilds; i++) {
				v = ((ViewGroup)roomNames.getChildAt(0)).getChildAt(i);
				LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) v.getLayoutParams();
				p.width = itemWidth;
				v.setLayoutParams(p);
			}
		}
		scrollToColumn(activeItem, true);
	}

}
