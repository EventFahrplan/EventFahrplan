package nerd.tuxmobil.fahrplan.congress;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import nerd.tuxmobil.fahrplan.congress.FahrplanContract.AlarmsTable;
import nerd.tuxmobil.fahrplan.congress.FahrplanContract.AlarmsTable.Columns;

public class AlarmList extends ActionBarListActivity {

    private MyApp global;

    private SQLiteDatabase db;

    private CursorAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorActionBar)));

        global = (MyApp) getApplicationContext();

        setContentView(R.layout.alarms);

        AlarmsDBOpenHelper alarmsDB = new AlarmsDBOpenHelper(this);

        db = alarmsDB.getReadableDatabase();
        Cursor cursor;

        try {
            cursor = db.query(AlarmsTable.NAME, AlarmsDBOpenHelper.allcolumns, null,
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
            case 0:
                delete_alarm(info.position);
                setResult(RESULT_OK);
                FahrplanFragment.loadAlarms(this);
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

        String lecture_id = cursor.getString(cursor.getColumnIndex(AlarmsTable.Columns.EVENT_ID));
        intent.putExtra(BundleKeys.ALARM_LECTURE_ID, lecture_id);
        int day = cursor.getInt(cursor.getColumnIndex(AlarmsTable.Columns.DAY));
        intent.putExtra(BundleKeys.ALARM_DAY, day);
        String title = cursor.getString(cursor.getColumnIndex(AlarmsTable.Columns.EVENT_TITLE));
        intent.putExtra(BundleKeys.ALARM_TITLE, title);
        long startTime = cursor.getLong(cursor.getColumnIndex(AlarmsTable.Columns.TIME));
        intent.putExtra(BundleKeys.ALARM_START_TIME, startTime);

        intent.setAction("de.machtnix.fahrplan.ALARM");
        intent.setData(Uri.parse("alarm://" + lecture_id));

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingintent = PendingIntent
                .getBroadcast(this, Integer.parseInt(lecture_id), intent, 0);
        alarmManager.cancel(pendingintent);

        String id = cursor.getString(cursor.getColumnIndex(AlarmsTable.Columns.ID));
        db.delete(AlarmsTable.NAME, Columns.ID + " = ?", new String[]{id});
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
                FahrplanFragment.loadAlarms(this);
                setResult(RESULT_OK);
                return true;
            case android.R.id.home:
                return ActivityHelper.navigateUp(this);
        }
        return super.onOptionsItemSelected(item);
    }

}
