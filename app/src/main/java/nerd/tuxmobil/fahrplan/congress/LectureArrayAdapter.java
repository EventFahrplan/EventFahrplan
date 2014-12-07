package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LectureArrayAdapter extends ArrayAdapter<Lecture> {

    private final Context context;
    private final List<Lecture> list;

    public LectureArrayAdapter(Context context, List<Lecture> list) {
        super(context, R.layout.lecture_change_row, list);
        this.context = context;
        this.list = list;
    }

    private void resetTextStyle(TextView textView) {
        textView.setTextAppearance(context, android.R.style.TextAppearance_Medium);
        textView.setPaintFlags(textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
    }

    private void setTextStyleChanged(TextView textView) {
        textView.setTextColor(context.getResources().getColor(R.color.schedule_change));
    }

    private void setTextStyleNew(TextView textView) {
        textView.setTextColor(context.getResources().getColor(R.color.schedule_change_new));
    }

    private void setTextStyleCanceled(TextView textView) {
        textView.setTextColor(context.getResources().getColor(R.color.schedule_change_canceled));
        textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.lecture_change_row, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.title = (TextView) rowView.findViewById(R.id.title);
            viewHolder.subtitle = (TextView) rowView.findViewById(R.id.subtitle);
            viewHolder.speakers = (TextView) rowView.findViewById(R.id.speakers);
            viewHolder.lang = (TextView) rowView.findViewById(R.id.lang);
            viewHolder.day = (TextView) rowView.findViewById(R.id.day);
            viewHolder.time = (TextView) rowView.findViewById(R.id.time);
            viewHolder.room = (TextView) rowView.findViewById(R.id.room);
            viewHolder.duration = (TextView) rowView.findViewById(R.id.duration);
            rowView.setTag(viewHolder);
        } else {
            rowView = convertView;
            viewHolder = (ViewHolder) rowView.getTag();
        }

        DateFormat df = SimpleDateFormat
                .getDateInstance(SimpleDateFormat.SHORT);
        DateFormat tf = SimpleDateFormat
                .getTimeInstance(SimpleDateFormat.SHORT);

        resetTextStyle(viewHolder.title);
        resetTextStyle(viewHolder.subtitle);
        resetTextStyle(viewHolder.speakers);
        resetTextStyle(viewHolder.lang);
        resetTextStyle(viewHolder.day);
        resetTextStyle(viewHolder.time);
        resetTextStyle(viewHolder.room);
        resetTextStyle(viewHolder.duration);

        Lecture l = list.get(position);
        viewHolder.title.setText(l.title);
        viewHolder.subtitle.setText(l.subtitle);
        viewHolder.speakers.setText(l.speakers);
        viewHolder.lang.setText(l.lang);
        viewHolder.day.setText(df.format(new Date(l.dateUTC)));
        viewHolder.time.setText(tf.format(new Date(l.dateUTC)));
        viewHolder.room.setText(l.room);
        viewHolder.duration.setText(String.valueOf(l.duration) + " min.");
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
                if (l.title.length() == 0) viewHolder.title.setText(context.getText(R.string.dash));
            }
            if (l.changedSubtitle) {
                setTextStyleChanged(viewHolder.subtitle);
                if (l.subtitle.length() == 0) viewHolder.subtitle.setText(context.getText(R.string.dash));
            }
            if (l.changedSpeakers) {
                setTextStyleChanged(viewHolder.speakers);
                if (l.speakers.length() == 0) viewHolder.speakers.setText(context.getText(R.string.dash));
            }
            if (l.changedLanguage) {
                setTextStyleChanged(viewHolder.lang);
                if (l.lang.length() == 0) viewHolder.lang.setText(context.getText(R.string.dash));
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
        }

        return rowView;
    }

    static class ViewHolder {
        TextView title;
        TextView subtitle;
        TextView speakers;
        TextView lang;
        TextView day;
        TextView time;
        TextView room;
        TextView duration;
    }
}
