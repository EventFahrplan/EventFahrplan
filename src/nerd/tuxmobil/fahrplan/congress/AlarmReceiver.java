package nerd.tuxmobil.fahrplan.congress;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public final class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
    	String lecture_id = intent.getStringExtra("lecture_id");
    	int lid = Integer.parseInt(lecture_id);
    	int day = intent.getIntExtra("day", 1);
    	long when = intent.getLongExtra("startTime", System.currentTimeMillis());
    	String title = intent.getStringExtra("title");
        //Toast.makeText(context, "Alarm worked.", Toast.LENGTH_LONG).show();
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notify = new Notification();
        
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		notify.sound = Uri.parse(prefs.getString("reminder_tone", ""));
		boolean insistent = prefs.getBoolean("insistent", false);
		Log.d("alarm", "insistent is "+insistent);
        notify.flags = Notification.FLAG_AUTO_CANCEL;
        if (insistent) {
        	notify.flags |= Notification.FLAG_INSISTENT;
        }
		Log.d("alarm", "flags: "+notify.flags);
        notify.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE;
		Log.d("alarm", "url: "+prefs.getString("reminder_tone",""));
        notify.icon = R.drawable.ic_notification;
        notify.when = when;
        
        Intent notificationIntent = new Intent(context, Fahrplan.class);  
        notificationIntent.putExtra("lecture_id", lecture_id);
        notificationIntent.putExtra("day", day);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent contentIntent = PendingIntent.getActivity(context, lid, notificationIntent, PendingIntent.FLAG_ONE_SHOT);  
        notify.setLatestEventInfo(context, context.getString(R.string.reminder), title, contentIntent);
        
        nm.notify(1, notify);
        
        // Clear from alarmDB
        
		AlarmsDBOpenHelper lecturesDB = new AlarmsDBOpenHelper(context);

		SQLiteDatabase db = lecturesDB.getReadableDatabase();
		db.delete("alarms", "eventid=?", new String[] { lecture_id });
		
		db.close();
    }
}
