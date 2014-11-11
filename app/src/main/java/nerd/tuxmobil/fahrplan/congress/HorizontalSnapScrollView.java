package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
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

    private static final int SWIPE_MIN_DISTANCE = 5;

    private static final int SWIPE_THRESHOLD_VELOCITY = 2800;

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
        public boolean onDown(MotionEvent e) {
            xStart = (int) e.getX();
//    		MyApp.LogDebug(LOG_TAG, "onDown xStart:"+xStart+" getMeasuredWidth:"+getMeasuredWidth());
            float ofs = (float) (getScrollX() * max_cols) / getMeasuredWidth();
            activeItem = Math.round(ofs);
            return super.onDown(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float scale = getResources().getDisplayMetrics().density;
            int columns = (int) Math
                    .ceil((Math.abs(velocityX / scale) / SWIPE_THRESHOLD_VELOCITY) * 3);
//	    	MyApp.LogDebug(LOG_TAG, "onFling " + velocityX + "/" + velocityY + " " + velocityX/scale + " " + columns);
            try {
                //right to left
                if ((e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) && (Math.abs(velocityX / scale)
                        > SWIPE_THRESHOLD_VELOCITY)) {
                    scrollToColumn(activeItem + columns, false);
                    return true;
                }
                //left to right
                else if ((e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) && (
                        Math.abs(velocityX / scale) > SWIPE_THRESHOLD_VELOCITY)) {
                    scrollToColumn(activeItem - columns, false);
                    return true;
                }
            } catch (Exception e) {
                MyApp.LogDebug(LOG_TAG,
                        "There was an error processing the Fling event:" + e.getMessage());
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean result = super.onInterceptTouchEvent(ev);
        gestureDetector.onTouchEvent(ev);
        return result;
    }

    public void scrollToColumn(int col, boolean fast) {
        int max;
        if (itemWidth == 0) {
            max = 0;
        } else {
            max = (getChildAt(0).getMeasuredWidth() - itemWidth) / itemWidth;
        }
        if (col < 0) {
            col = 0;
        }
        if (col > max) {
            col = max;
        }
        final int scrollTo = col * itemWidth;
        MyApp.LogDebug(LOG_TAG,
                "scroll to col " + col + "/" + scrollTo + " " + getChildAt(0).getMeasuredWidth());
        if (!fast) {
            this.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    smoothScrollTo(scrollTo, 0);

                }
            });
        } else {
            scrollTo(scrollTo, 0);
            if (roomNames != null) {
                roomNames.scrollTo(scrollTo, 0);
            }
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
//	            	return false;
                MyApp.LogDebug(LOG_TAG, "onTouch");
                if (gestureDetector.onTouchEvent(event)) {
//	            		MyApp.LogDebug(LOG_TAG, "gesture detector consumed event");
                    return false;
                } else if (event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    int scrollX = (int) event.getX();
                    int distance = scrollX - xStart;
                    MyApp.LogDebug(LOG_TAG,
                            "item width:" + itemWidth + " scrollX:" + scrollX + " distance:"
                                    + distance + " activeItem:" + activeItem);
                    int newItem = activeItem;
                    int col_dist;
                    if (max_cols > 1) {
                        col_dist = (int) Math.round(Math.abs((float) distance) / itemWidth);
                        MyApp.LogDebug(LOG_TAG, "col dist: " + col_dist);
                        if (distance > 0) {
                            newItem = activeItem - col_dist;
                        } else {
                            newItem = activeItem + col_dist;
                        }
                    } else {
                        if (Math.abs(distance) > itemWidth / 4) {
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
            dip = (int) ((((float) availPixels) / max_cols) / scale);
            MyApp.LogDebug(LOG_TAG, "calcMaxCols: " + dip + " on " + max_cols + " cols.");
            max_cols--;
        } while ((dip < min_dip) && (max_cols > 0));
        return max_cols + 1;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        MyApp.LogDebug(LOG_TAG,
                "onSizeChanged " + oldw + ", " + oldh + ", " + w + ", " + h + " getMW:"
                        + getMeasuredWidth());
        super.onSizeChanged(w, h, oldw, oldh);
        max_cols = calcMaxCols(getResources(), getMeasuredWidth());

        int newItemWidth = Math.round((float) getMeasuredWidth() / max_cols);
        float scale = getResources().getDisplayMetrics().density;

        MyApp.LogDebug(LOG_TAG,
                "item width: " + newItemWidth + " " + ((float) newItemWidth) / scale + "dp");
        setColumnWidth(newItemWidth);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
//		MyApp.LogDebug(LOG_TAG, "scrolled from " + oldl + " to " + l);
        if (roomNames != null) {
            roomNames.scrollTo(l, 0);
        }
    }

    public void setChildScroller(HorizontalScrollView h) {
        roomNames = h;
    }

    public int getColumnWidth() {
        return itemWidth;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        scrollToColumn(activeItem, true);
    }

    public void setColumnWidth(int pixels) {

        MyApp.LogDebug(LOG_TAG, "setColumnWidth " + pixels);
        itemWidth = pixels;
        if (pixels == 0) {
            return;
        }

        ViewGroup container = (ViewGroup) getChildAt(0);
        int childs = container.getChildCount();
        for (int i = 0; i < childs; i++) {
            ViewGroup c = (ViewGroup) container.getChildAt(i);
            ViewGroup.LayoutParams p = c.getLayoutParams();
            p.width = itemWidth;
            c.setLayoutParams(p);
        }
        container.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        if (roomNames != null) {
            View v;
            int numChilds = ((ViewGroup) roomNames.getChildAt(0)).getChildCount();

            for (int i = 0; i < numChilds; i++) {
                v = ((ViewGroup) roomNames.getChildAt(0)).getChildAt(i);
                LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) v.getLayoutParams();
                p.width = itemWidth;
                v.setLayoutParams(p);
            }
        }
        scrollToColumn(activeItem, true);
    }
}
