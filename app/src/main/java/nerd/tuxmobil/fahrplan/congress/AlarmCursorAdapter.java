package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import nerd.tuxmobil.fahrplan.congress.FahrplanContract.AlarmsTable;
import nerd.tuxmobil.fahrplan.congress.FahrplanContract.AlarmsTable.Columns;

public class AlarmCursorAdapter extends CursorAdapter {

    protected LayoutInflater mInflater;

    public AlarmCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (holder == null) {
            holder = new ViewHolder();
            holder.alarmTimeInMin = (TextView) view.findViewById(R.id.alarm_list_icon);
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.time = (TextView) view.findViewById(R.id.time);
            view.setTag(holder);
        }
        int alarmTimeInMin = cursor.getInt(cursor.getColumnIndex(Columns.ALARM_TIME_IN_MIN));
        String title = cursor.getString(cursor.getColumnIndex(Columns.EVENT_TITLE));
        String timeText = cursor.getString(cursor.getColumnIndex(Columns.TIME_TEXT));
        if (alarmTimeInMin == AlarmsTable.Defaults.ALARM_TIME_IN_MIN_DEFAULT) {
            holder.alarmTimeInMin.setText("?");
        } else {
            holder.alarmTimeInMin.setText("" + alarmTimeInMin);
        }
        holder.title.setText(title);
        holder.time.setText(timeText);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.alarm_list_item, parent, false);
    }

    private static class ViewHolder {

        TextView alarmTimeInMin;

        TextView title;

        TextView time;
    }

}
