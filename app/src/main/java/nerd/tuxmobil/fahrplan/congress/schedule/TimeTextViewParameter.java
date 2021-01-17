package nerd.tuxmobil.fahrplan.congress.schedule;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import info.metadude.android.eventfahrplan.commons.temporal.Moment;
import nerd.tuxmobil.fahrplan.congress.R;

import static nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.BOX_HEIGHT_MULTIPLIER;
import static nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.FIFTEEN_MINUTES;
import static nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.ONE_DAY;

/**
 * Parameters to be used to inflate and configure a time text view.
 */
class TimeTextViewParameter {

    @LayoutRes
    private final int layout;
    private final int height;
    @NonNull
    private final String titleText;

    private TimeTextViewParameter(@LayoutRes int layout, int height, @NonNull String titleText) {
        this.layout = layout;
        this.height = height;
        this.titleText = titleText;
    }

    @LayoutRes
    public int getLayout() {
        return layout;
    }

    public int getHeight() {
        return height;
    }

    @NonNull
    public String getTitleText() {
        return titleText;
    }

    @Override
    public String toString() {
        return "TimeTextViewParameter{" +
                "layout=" + layout +
                ", height=" + height +
                ", titleText='" + titleText + '\'' +
                '}';
    }

    /**
     * Returns a list of parameters to be used to inflate and configure a time text view.
     */
    public static List<TimeTextViewParameter> parametersOf(
            @NonNull Moment nowMoment,
            @NonNull Conference conference,
            int firstDayStartDay,
            int dayIndex,
            int normalizedBoxHeight
    ) {
        List<TimeTextViewParameter> parameters = new ArrayList<>();
        int time = conference.getFirstSessionStartsAt();
        int printTime = time;
        int timeTextViewHeight = BOX_HEIGHT_MULTIPLIER * normalizedBoxHeight;
        TimeSegment timeSegment;
        while (time < conference.getLastSessionEndsAt()) {
            timeSegment = TimeSegment.ofMinutesOfTheDay(printTime);
            int timeTextLayout;
            boolean isToday = nowMoment.getMonthDay() - firstDayStartDay == dayIndex - 1;
            if (isToday && timeSegment.isMatched(nowMoment, FIFTEEN_MINUTES)) {
                timeTextLayout = R.layout.time_layout_now;
            } else {
                timeTextLayout = R.layout.time_layout;
            }
            TimeTextViewParameter parameter = new TimeTextViewParameter(timeTextLayout, timeTextViewHeight, timeSegment.getFormattedText());
            parameters.add(parameter);
            time += FIFTEEN_MINUTES;
            printTime = time;
            if (printTime >= ONE_DAY) {
                printTime -= ONE_DAY;
            }
        }
        return parameters;
    }

}
