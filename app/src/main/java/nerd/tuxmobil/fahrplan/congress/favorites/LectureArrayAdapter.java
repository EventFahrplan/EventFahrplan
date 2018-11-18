package nerd.tuxmobil.fahrplan.congress.favorites;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.text.format.Time;
import android.view.View;

import java.util.List;

import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.base.LecturesAdapter;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;
import nerd.tuxmobil.fahrplan.congress.utils.DateHelper;

public class LectureArrayAdapter extends LecturesAdapter {

    private final Time now;

    @ColorInt
    private int pastEventTextColor;

    LectureArrayAdapter(Context context, List<Lecture> list, int numDays) {
        super(context, R.layout.lecture_change_row, list, numDays);
        now = new Time();
        pastEventTextColor = ContextCompat.getColor(context, R.color.favorites_past_event_text);
    }

    @Override
    protected void initViewSetup() {
        now.setToNow();
    }

    @Override
    protected void setItemContent(int position, ViewHolder viewHolder) {
        resetItemStyles(viewHolder);

        Lecture l = getLecture(position);
        if (lectureTookPlace(l)) {
            viewHolder.title.setTextColor(pastEventTextColor);
            viewHolder.subtitle.setTextColor(pastEventTextColor);
            viewHolder.speakers.setTextColor(pastEventTextColor);
            viewHolder.lang.setTextColor(pastEventTextColor);
            viewHolder.day.setTextColor(pastEventTextColor);
            viewHolder.time.setTextColor(pastEventTextColor);
            viewHolder.room.setTextColor(pastEventTextColor);
            viewHolder.duration.setTextColor(pastEventTextColor);
        }

        viewHolder.title.setText(l.title);
        viewHolder.subtitle.setText(l.subtitle);
        viewHolder.speakers.setText(l.getFormattedSpeakers());
        viewHolder.lang.setText(l.lang);
        viewHolder.day.setVisibility(View.GONE);
        String timeText = DateHelper.getFormattedTime(l.dateUTC);
        viewHolder.time.setText(timeText);
        viewHolder.room.setText(l.room);
        String durationText = context.getString(R.string.event_duration, l.duration);
        viewHolder.duration.setText(durationText);
        viewHolder.video.setVisibility(View.GONE);
        viewHolder.noVideo.setVisibility(View.GONE);
    }

    private boolean lectureTookPlace(Lecture lecture) {
        return lecture.dateUTC + lecture.duration * 60000 < now.toMillis(true);
    }

}
