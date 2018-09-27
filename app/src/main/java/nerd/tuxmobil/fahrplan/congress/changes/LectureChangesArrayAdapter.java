package nerd.tuxmobil.fahrplan.congress.changes;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.base.LecturesAdapter;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;
import nerd.tuxmobil.fahrplan.congress.utils.DateHelper;

public class LectureChangesArrayAdapter extends LecturesAdapter {

    @ColorInt
    private int scheduleChangeTextColor;

    @ColorInt
    private int scheduleChangeNewTextColor;

    @ColorInt
    private int scheduleChangeCanceledTextColor;

    LectureChangesArrayAdapter(Context context, List<Lecture> list, int numDays) {
        super(context, R.layout.lecture_change_row, list, numDays);
        scheduleChangeTextColor = ContextCompat.getColor(context, R.color.schedule_change);
        scheduleChangeNewTextColor = ContextCompat.getColor(context, R.color.schedule_change_new);
        scheduleChangeCanceledTextColor = ContextCompat.getColor(context, R.color.schedule_change_canceled);
    }

    @Override
    protected void resetTextStyle(TextView textView, int style) {
        super.resetTextStyle(textView, style);
        textView.setPaintFlags(textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
    }

    private void setTextStyleChanged(TextView textView) {
        textView.setTextColor(scheduleChangeTextColor);
    }

    private void setTextStyleNew(TextView textView) {
        textView.setTextColor(scheduleChangeNewTextColor);
    }

    private void setTextStyleCanceled(TextView textView) {
        textView.setTextColor(scheduleChangeCanceledTextColor);
        textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    @Override
    protected void initViewSetup() {
        // Nothing to do here
    }

    @Override
    protected void setItemContent(int position, ViewHolder viewHolder) {
        resetItemStyles(viewHolder);

        Lecture l = getLecture(position);
        viewHolder.title.setText(l.title);
        viewHolder.subtitle.setText(l.subtitle);
        viewHolder.speakers.setText(l.getFormattedSpeakers());
        viewHolder.lang.setText(l.lang);
        String dayText = DateHelper.getFormattedDate(l.dateUTC);
        viewHolder.day.setText(dayText);
        String timeText = DateHelper.getFormattedTime(l.dateUTC);
        viewHolder.time.setText(timeText);
        viewHolder.room.setText(l.room);
        String durationText = context.getString(R.string.event_duration, l.duration);
        viewHolder.duration.setText(durationText);
        viewHolder.video.setVisibility(View.GONE);
        viewHolder.noVideo.setVisibility(View.GONE);

        if (l.changedIsNew) {
            setTextStyleNew(viewHolder.title);
            setTextStyleNew(viewHolder.subtitle);
            setTextStyleNew(viewHolder.speakers);
            setTextStyleNew(viewHolder.lang);
            setTextStyleNew(viewHolder.day);
            setTextStyleNew(viewHolder.time);
            setTextStyleNew(viewHolder.room);
            setTextStyleNew(viewHolder.duration);
        } else if (l.changedIsCanceled) {
            setTextStyleCanceled(viewHolder.title);
            setTextStyleCanceled(viewHolder.subtitle);
            setTextStyleCanceled(viewHolder.speakers);
            setTextStyleCanceled(viewHolder.lang);
            setTextStyleCanceled(viewHolder.day);
            setTextStyleCanceled(viewHolder.time);
            setTextStyleCanceled(viewHolder.room);
            setTextStyleCanceled(viewHolder.duration);
        } else {
            if (l.changedTitle) {
                setTextStyleChanged(viewHolder.title);
                if (l.title.length() == 0) {
                    viewHolder.title.setText(context.getText(R.string.dash));
                }
            }
            if (l.changedSubtitle) {
                setTextStyleChanged(viewHolder.subtitle);
                if (l.subtitle.length() == 0) {
                    viewHolder.subtitle.setText(context.getText(R.string.dash));
                }
            }
            if (l.changedSpeakers) {
                setTextStyleChanged(viewHolder.speakers);
                if (l.speakers.length() == 0) {
                    viewHolder.speakers.setText(context.getText(R.string.dash));
                }
            }
            if (l.changedLanguage) {
                setTextStyleChanged(viewHolder.lang);
                if (l.lang.length() == 0) {
                    viewHolder.lang.setText(context.getText(R.string.dash));
                }
            }
            if (l.changedDay) {
                setTextStyleChanged(viewHolder.day);
            }
            if (l.changedTime) {
                setTextStyleChanged(viewHolder.time);
            }
            if (l.changedRoom) {
                setTextStyleChanged(viewHolder.room);
            }
            if (l.changedDuration) {
                setTextStyleChanged(viewHolder.duration);
            }
            if (l.changedRecordingOptOut) {
                if (l.recordingOptOut) {
                    viewHolder.noVideo.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.video.setVisibility(View.VISIBLE);
                }
            }
        }
    }

}
