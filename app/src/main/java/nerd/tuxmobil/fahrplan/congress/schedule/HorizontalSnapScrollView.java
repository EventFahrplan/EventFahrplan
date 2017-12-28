package nerd.tuxmobil.fahrplan.congress.schedule;

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

import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;


public class HorizontalSnapScrollView extends HorizontalScrollView {

    private static final String LOG_TAG = "HorizontalScrollView";

    private GestureDetector gestureDetector;

    private int activeColumnIndex = 0;

    private int xStart;

    private int columnWidth;

    private HorizontalScrollView roomNames = null;

    private int maximumColumns;

    private static final int SWIPE_MIN_DISTANCE = 5;

    private static final int SWIPE_THRESHOLD_VELOCITY = 2800;

    /**
     * get currently displayed column index
     *
     * @return index (0..n)
     */
    public int getColumn() {
        return activeColumnIndex;
    }

    class YScrollDetector extends SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            xStart = (int) e.getX();
//    		MyApp.LogDebug(LOG_TAG, "onDown xStart:"+xStart+" getMeasuredWidth:"+getMeasuredWidth());
            float ofs = (float) (getScrollX() * maximumColumns) / getMeasuredWidth();
            activeColumnIndex = Math.round(ofs);
            return super.onDown(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float scale = getResources().getDisplayMetrics().density;
            float normalizedAbsoluteVelocityX = Math.abs(velocityX / scale);
            int columns = (int) Math.ceil((normalizedAbsoluteVelocityX / SWIPE_THRESHOLD_VELOCITY) * 3);
//	    	MyApp.LogDebug(LOG_TAG, "onFling " + velocityX + "/" + velocityY + " " + velocityX/scale + " " + columns);
            boolean exceedsVelocityThreshold = normalizedAbsoluteVelocityX > SWIPE_THRESHOLD_VELOCITY;
            try {
                //right to left
                if ((e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) && exceedsVelocityThreshold) {
                    scrollToColumn(activeColumnIndex + columns, false);
                    return true;
                }
                //left to right
                else if ((e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) && exceedsVelocityThreshold) {
                    scrollToColumn(activeColumnIndex - columns, false);
                    return true;
                }
            } catch (Exception e) {
                MyApp.LogDebug(LOG_TAG, "There was an error processing the Fling event:" + e.getMessage());
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
        if (columnWidth == 0) {
            max = 0;
        } else {
            max = (getChildAt(0).getMeasuredWidth() - columnWidth) / columnWidth;
        }
        if (col < 0) {
            col = 0;
        }
        if (col > max) {
            col = max;
        }
        final int scrollTo = col * columnWidth;
        MyApp.LogDebug(LOG_TAG, "scroll to col " + col + "/" + scrollTo + " " + getChildAt(0).getMeasuredWidth());
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
        activeColumnIndex = col;
    }

    public HorizontalSnapScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        maximumColumns = getResources().getInteger(R.integer.max_cols);
        columnWidth = 0;
        gestureDetector = new GestureDetector(new YScrollDetector());
        setOnTouchListener(new OnTouchListener());
    }

    private class OnTouchListener implements View.OnTouchListener {

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
                        "column width:" + columnWidth + " scrollX:" + scrollX + " distance:"
                                + distance + " active column index:" + activeColumnIndex);
                int columnIndex = activeColumnIndex;
                int columnDistance;
                if (maximumColumns > 1) {
                    columnDistance = Math.round(Math.abs((float) distance) / columnWidth);
                    MyApp.LogDebug(LOG_TAG, "column distance: " + columnDistance);
                    if (distance > 0) {
                        columnIndex = activeColumnIndex - columnDistance;
                    } else {
                        columnIndex = activeColumnIndex + columnDistance;
                    }
                } else {
                    if (Math.abs(distance) > columnWidth / 4) {
                        if (distance > 0) {
                            columnIndex = activeColumnIndex - 1;
                        } else {
                            columnIndex = activeColumnIndex + 1;
                        }
                    }
                }
                scrollToColumn(columnIndex, false);

                return true;
            } else {
                return false;
            }
        }
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

    // FIXME: Landscape patch to expand a column to full width if there is space available
    private static int calcMaxCols(Resources res, int availPixels, int columnsCount) {
        int max_cols = res.getInteger(R.integer.max_cols);
        // TODO: The next line is the relevant monkey patch
        max_cols = (columnsCount < max_cols) ? columnsCount : max_cols;
        // TODO: The previous line is the relevant monkey patch
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
        MyApp.LogDebug(LOG_TAG, "onSizeChanged " + oldw + ", " + oldh + ", " + w + ", " + h + " getMW:" + getMeasuredWidth());
        super.onSizeChanged(w, h, oldw, oldh);
        maximumColumns = calcMaxCols(getResources(), getMeasuredWidth(), MyApp.room_count);

        int newItemWidth = Math.round((float) getMeasuredWidth() / maximumColumns);
        float scale = getResources().getDisplayMetrics().density;

        MyApp.LogDebug(LOG_TAG, "item width: " + newItemWidth + " " + ((float) newItemWidth) / scale + "dp");
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
        return columnWidth;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        scrollToColumn(activeColumnIndex, true);
    }

    public void setColumnWidth(int pixels) {

        MyApp.LogDebug(LOG_TAG, "setColumnWidth " + pixels);
        columnWidth = pixels;
        if (pixels == 0) {
            return;
        }

        ViewGroup container = (ViewGroup) getChildAt(0);
        int childCount = container.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewGroup c = (ViewGroup) container.getChildAt(i);
            ViewGroup.LayoutParams p = c.getLayoutParams();
            p.width = columnWidth;
            c.setLayoutParams(p);
        }
        container.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        if (roomNames != null) {
            View v;
            int firstRoomChildCount = ((ViewGroup) roomNames.getChildAt(0)).getChildCount();

            for (int i = 0; i < firstRoomChildCount; i++) {
                v = ((ViewGroup) roomNames.getChildAt(0)).getChildAt(i);
                LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) v.getLayoutParams();
                p.width = columnWidth;
                v.setLayoutParams(p);
            }
        }
        scrollToColumn(activeColumnIndex, true);
    }
}
