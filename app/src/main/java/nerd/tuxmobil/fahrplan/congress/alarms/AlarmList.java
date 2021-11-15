package nerd.tuxmobil.fahrplan.congress.alarms;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.CursorAdapter;

import org.ligi.tracedroid.logging.Log;

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable;
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.AlarmsTable.Columns;
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.AlarmsDBOpenHelper;
import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.R;
import nerd.tuxmobil.fahrplan.congress.base.ActionBarListActivity;
import nerd.tuxmobil.fahrplan.congress.models.SchedulableAlarm;
import nerd.tuxmobil.fahrplan.congress.repositories.AppRepository;

public class AlarmList extends ActionBarListActivity {

    private static final int CONTEXT_MENU_ITEM_ID_DELETE = 0;
    public static final int REQUEST_CODE = 1;

    private MyApp global;

    private SQLiteDatabase db;

    private CursorAdapter mAdapter;

    private AppRepository appRepository;

    public static void startForResult(@NonNull Activity activity) {
        Intent intent = new Intent(activity, AlarmList.class);
        activity.startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int actionBarColor = ContextCompat.getColor(this, R.color.colorActionBar);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(actionBarColor));

        global = (MyApp) getApplicationContext();

        setContentView(R.layout.alarms);

        appRepository = AppRepository.INSTANCE;

        AlarmsDBOpenHelper alarmsDB = new AlarmsDBOpenHelper(this);

        db = alarmsDB.getReadableDatabase();
        Cursor cursor;

        try {
            cursor = db.query(AlarmsTable.NAME, null, null,
                    null, null, null, Columns.TIME);
        } catch (SQLiteException e) {
            e.printStackTrace();
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
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        int menuItemIndex = item.getItemId();
        //noinspection SwitchStatementWithTooFewBranches
        switch (menuItemIndex) {
            case CONTEXT_MENU_ITEM_ID_DELETE:
                deleteAlarm(info.position);
                setResult(RESULT_OK);
                appRepository.notifyAlarmsChanged();
                break;
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        menu.add(0, CONTEXT_MENU_ITEM_ID_DELETE, 0, global.getString(R.string.menu_item_title_delete_favorite));
    }

    public void deleteAlarm(int position) {
        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        String sessionId = cursor.getString(cursor.getColumnIndex(AlarmsTable.Columns.SESSION_ID));
        int day = cursor.getInt(cursor.getColumnIndex(AlarmsTable.Columns.DAY));
        String title = cursor.getString(cursor.getColumnIndex(AlarmsTable.Columns.SESSION_TITLE));
        long startTime = cursor.getLong(cursor.getColumnIndex(AlarmsTable.Columns.TIME));
        Log.d(getClass().getSimpleName(), "deleteAlarm: session: " + sessionId);
        SchedulableAlarm alarm = new SchedulableAlarm(day, sessionId, title, startTime);
        AlarmServices.newInstance(this, appRepository).discardSessionAlarm(alarm);

        int alarmId = cursor.getInt(cursor.getColumnIndex(Columns.ID));
        db.delete(AlarmsTable.NAME, Columns.ID + " = ?", new String[]{String.valueOf(alarmId)});
        cursor.requery();
        mAdapter.notifyDataSetChanged();
    }

    public void deleteAllAlarms() {
        db.delete(AlarmsTable.NAME, null, null);
        setListAdapter(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.alarms_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (item.getItemId()) {
            case R.id.menu_item_delete_all_alarms:
                deleteAllAlarms();
                appRepository.notifyAlarmsChanged();
                setResult(RESULT_OK);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
