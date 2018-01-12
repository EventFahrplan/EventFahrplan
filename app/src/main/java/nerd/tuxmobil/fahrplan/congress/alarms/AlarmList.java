package nerd.tuxmobil.fahrplan.congress.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import org.ligi.tracedroid.logging.Log;

import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.base.ActionBarListActivity;
import nerd.tuxmobil.fahrplan.congress.persistence.AlarmsDBOpenHelper;
import nerd.tuxmobil.fahrplan.congress.persistence.FahrplanContract.AlarmsTable;
import nerd.tuxmobil.fahrplan.congress.persistence.FahrplanContract.AlarmsTable.Columns;
import nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment;

public class AlarmList extends ActionBarListActivity {

    private static final int CONTEXT_MENU_ITEM_ID_DELETE = 0;

    private MyApp global;

    private SQLiteDatabase db;

    private CursorAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int actionBarColor = ContextCompat.getColor(this, R.color.colorActionBar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionBarColor));

        global = (MyApp) getApplicationContext();

        setContentView(R.layout.alarms);

        AlarmsDBOpenHelper alarmsDB = new AlarmsDBOpenHelper(this);

        db = alarmsDB.getReadableDatabase();
        Cursor cursor;

        try {
            cursor = db.query(AlarmsTable.NAME, null, null,
                    null, null, null, Columns.TIME);
        } catch (SQLiteException e) {
            e.printStackTrace();
            db.close();
            return;
        }
        startManagingCursor(cursor);

        mAdapter = new AlarmCursorAdapter(this, cursor, 0);

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
            case CONTEXT_MENU_ITEM_ID_DELETE:
                delete_alarm(info.position);
                setResult(RESULT_OK);
                FahrplanFragment.loadAlarms(this);
                break;
        }
        return true;
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        menu.add(0, CONTEXT_MENU_ITEM_ID_DELETE, 0, global.getString(R.string.delete));
    }

    public void delete_alarm(int position) {
        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        String lecture_id = cursor.getString(cursor.getColumnIndex(AlarmsTable.Columns.EVENT_ID));
        int day = cursor.getInt(cursor.getColumnIndex(AlarmsTable.Columns.DAY));
        String title = cursor.getString(cursor.getColumnIndex(AlarmsTable.Columns.EVENT_TITLE));
        long startTime = cursor.getLong(cursor.getColumnIndex(AlarmsTable.Columns.TIME));
        Log.d(getClass().getName(), "delete_alarm: lecture: " + lecture_id);

        Intent deleteAlarmIntent = new AlarmReceiver.AlarmIntentBuilder()
                .setContext(this)
                .setLectureId(lecture_id)
                .setDay(day)
                .setTitle(title)
                .setStartTime(startTime)
                .setIsDeleteAlarm()
                .build();

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingintent = PendingIntent.getBroadcast(
                this, Integer.parseInt(lecture_id), deleteAlarmIntent, 0);
        alarmManager.cancel(pendingintent);

        String id = cursor.getString(cursor.getColumnIndex(AlarmsTable.Columns.ID));
        db.delete(AlarmsTable.NAME, Columns.ID + " = ?", new String[]{id});
        cursor.requery();
        mAdapter.notifyDataSetChanged();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.alarms_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.alarms_delete:
                int count = getListAdapter().getCount();
                for (int i = 0; i < count; i++) {
                    delete_alarm(0);
                }
                FahrplanFragment.loadAlarms(this);
                setResult(RESULT_OK);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
