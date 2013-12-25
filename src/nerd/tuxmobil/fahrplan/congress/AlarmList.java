package nerd.tuxmobil.fahrplan.congress;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;

public class AlarmList extends SherlockListActivity {
	private MyApp global;
	private SQLiteDatabase db;
	private SimpleCursorAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		global = (MyApp) getApplicationContext();

		setContentView(R.layout.alarms);

		AlarmsDBOpenHelper alarmsDB = new AlarmsDBOpenHelper(this);

		db = alarmsDB.getReadableDatabase();
		Cursor cursor;

		try {
			cursor = db.query("alarms", AlarmsDBOpenHelper.allcolumns, null,
					null, null, null, "time");
		} catch (SQLiteException e) {
			e.printStackTrace();
			db.close();
			return;
		}
		startManagingCursor(cursor);

		// the desired columns to be bound
		String[] columns = new String[] { "title", "timeText" };
		// the XML defined views which the data will be bound to
		int[] to = new int[] { R.id.title, R.id.time };

		// create the adapter using the cursor pointing to the desired data as
		// well as the layout information
		mAdapter = new SimpleCursorAdapter(this, R.layout.alarm_list_item,
				cursor, columns, to);

		// set this adapter as your ListActivity's adapter
		this.setListAdapter(mAdapter);

		registerForContextMenu(getListView());

		setResult(RESULT_CANCELED);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		db.close();
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		int menuItemIndex = item.getItemId();
		switch (menuItemIndex) {
		case 0:
			delete_alarm(info.position);
			setResult(RESULT_OK);
			break;
		}
		return true;
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, 0, 0, global.getString(R.string.delete));
	}

	public void delete_alarm(int position) {
		Cursor cursor = (Cursor) getListAdapter().getItem(position);

		Intent intent = new Intent(this, AlarmReceiver.class);
		String lecture_id = cursor.getString(4);
		intent.putExtra("lecture_id", lecture_id);
		int day = cursor.getInt(6);
		intent.putExtra("day", day);
		String title = cursor.getString(1);
		intent.putExtra("title", title);
		long startTime = cursor.getLong(5);
		intent.putExtra("startTime", startTime);

		intent.setAction("de.machtnix.fahrplan.ALARM");
		intent.setData(Uri.parse("alarm://"+lecture_id));

		AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		PendingIntent pendingintent = PendingIntent.getBroadcast(this, Integer.parseInt(lecture_id), intent, 0);
		alarmManager.cancel(pendingintent);

		String id = cursor.getString(0);
		db.delete("alarms","_id = ?", new String[]{id});
		cursor.requery();
		mAdapter.notifyDataSetChanged();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.alarmmenu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_clear_all_alarms:
			int count = getListAdapter().getCount();
			for (int i = 0; i < count; i++) {
				delete_alarm(0);
			}
			setResult(RESULT_OK);
			return true;
		case android.R.id.home:
			Intent upIntent = NavUtils.getParentActivityIntent(this);
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				// This activity is NOT part of this app's task, so create a new task
				// when navigating up, with a synthesized back stack.
				TaskStackBuilder.create(this)
						// Add all of this activity's parents to the back stack
						.addNextIntentWithParentStack(upIntent)
						// Navigate up to the closest parent
						.startActivities();
			} else {
				// This activity is part of this app's task, so simply
				// navigate up to the logical parent activity.
				NavUtils.navigateUpTo(this, upIntent);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
