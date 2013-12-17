package nerd.tuxmobil.fahrplan.congress;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class EventDetail extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

        setContentView(R.layout.detail_frame);
        Intent intent = getIntent();
        if (intent == null) finish();

		if (findViewById(R.id.detail) != null) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction fragmentTransaction = fm.beginTransaction();
			EventDetailFragment ev = new EventDetailFragment();
			Bundle args = new Bundle();
			args.putString("title", intent.getStringExtra("title"));
			args.putString("subtitle", intent.getStringExtra("subtitle"));
			args.putString("abstract", intent.getStringExtra("abstract"));
			args.putString("descr", intent.getStringExtra("descr"));
			args.putString("spkr", intent.getStringExtra("skpr"));
			args.putString("links", intent.getStringExtra("links"));
			args.putString("eventid", intent.getStringExtra("eventid"));
			args.putInt("time", intent.getIntExtra("time", 0));
			args.putInt("day", intent.getIntExtra("day", 0));
			ev.setArguments(args);
			fragmentTransaction.replace(R.id.detail, ev, "detail");
			fragmentTransaction.commit();
		}
	}
}
