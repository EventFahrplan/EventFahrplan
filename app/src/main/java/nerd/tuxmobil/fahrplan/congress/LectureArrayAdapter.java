package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.graphics.Paint;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
        this.context = new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light);
        this.list = list;
    }

    private void resetTextStyle(TextView textView, int style) {
        textView.setTextAppearance(context, style);
        textView.setPaintFlags(textView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView;
        ViewHolder viewHolder;

        if (convertView == null) {

            // clone the inflater using the ContextThemeWrapper
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LayoutInflater localInflater = inflater.cloneInContext(context);

            rowView = localInflater.inflate(R.layout.lecture_change_row, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.title = (TextView) rowView.findViewById(R.id.title);
            viewHolder.subtitle = (TextView) rowView.findViewById(R.id.subtitle);
            viewHolder.speakers = (TextView) rowView.findViewById(R.id.speakers);
            viewHolder.lang = (TextView) rowView.findViewById(R.id.lang);
            viewHolder.day = (TextView) rowView.findViewById(R.id.day);
            viewHolder.time = (TextView) rowView.findViewById(R.id.time);
            viewHolder.room = (TextView) rowView.findViewById(R.id.room);
            viewHolder.duration = (TextView) rowView.findViewById(R.id.duration);
            viewHolder.video = (ImageView) rowView.findViewById(R.id.video);
            viewHolder.novideo = (ImageView) rowView.findViewById(R.id.no_video);
            rowView.setTag(viewHolder);
        } else {
            rowView = convertView;
            viewHolder = (ViewHolder) rowView.getTag();
        }

        DateFormat df = SimpleDateFormat
                .getDateInstance(SimpleDateFormat.SHORT);
        DateFormat tf = SimpleDateFormat
                .getTimeInstance(SimpleDateFormat.SHORT);

        resetTextStyle(viewHolder.title, R.style.ScheduleListPrimary);
        resetTextStyle(viewHolder.subtitle, R.style.ScheduleListSecondary);
        resetTextStyle(viewHolder.speakers, R.style.ScheduleListSecondary);
        resetTextStyle(viewHolder.lang, R.style.ScheduleListSecondary);
        resetTextStyle(viewHolder.day, R.style.ScheduleListSecondary);
        resetTextStyle(viewHolder.time, R.style.ScheduleListSecondary);
        resetTextStyle(viewHolder.room, R.style.ScheduleListSecondary);
        resetTextStyle(viewHolder.duration, R.style.ScheduleListSecondary);

        Lecture l = list.get(position);
        viewHolder.title.setText(l.title);
        viewHolder.subtitle.setText(l.subtitle);
        viewHolder.speakers.setText(l.speakers);
        viewHolder.lang.setText(l.lang);
        viewHolder.day.setText(df.format(new Date(l.dateUTC)));
        viewHolder.time.setText(tf.format(new Date(l.dateUTC)));
        viewHolder.room.setText(l.room);
        viewHolder.duration.setText(String.valueOf(l.duration) + " min.");
        viewHolder.video.setVisibility(View.GONE);
        viewHolder.novideo.setVisibility(View.GONE);

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
        ImageView novideo;
        ImageView video;
    }
}
