package nerd.tuxmobil.fahrplan.congress;

import nerd.tuxmobil.fahrplan.congress.FahrplanContract.AlarmsTable.Columns;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
            holder.title = (TextView) view.findViewById(R.id.title);
            holder.time = (TextView) view.findViewById(R.id.time);
            view.setTag(holder);
        }
        String title = cursor.getString(cursor.getColumnIndex(Columns.EVENT_TITLE));
        String timeText = cursor.getString(cursor.getColumnIndex(Columns.TIME_TEXT));
        holder.title.setText(title);
        holder.time.setText(timeText);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return mInflater.inflate(R.layout.alarm_list_item, parent, false);
	}

    private static class ViewHolder {
    	TextView title;
    	TextView time;
    }

}
