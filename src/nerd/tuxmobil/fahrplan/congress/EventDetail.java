package nerd.tuxmobil.fahrplan.congress;

import java.util.Locale;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class EventDetail extends SherlockActivity {

	private final String LOG_TAG = "Detail";
	private String event_id;
	private String title;
	private static String feedbackURL = "https://cccv.pentabarf.org/feedback/29C3/event/"; // + 4302.en.html
	private Locale locale;
	private Typeface boldCondensed;
	private Typeface black;
	private Typeface light;
	private Typeface regular;
	private Typeface bold;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

		boldCondensed = Typeface.createFromAsset(getAssets(), "Roboto-BoldCondensed.ttf");
		black = Typeface.createFromAsset(getAssets(), "Roboto-Black.ttf");
		light = Typeface.createFromAsset(getAssets(), "Roboto-Light.ttf");
		regular = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
		bold = Typeface.createFromAsset(getAssets(), "Roboto-Bold.ttf");
        Intent intent = getIntent();

        locale = getResources().getConfiguration().locale;

        event_id = intent.getStringExtra("eventid");

        TextView t = (TextView)findViewById(R.id.title);
        title = intent.getStringExtra("title");
        t.setTypeface(boldCondensed);
        t.setText(title);

        t = (TextView)findViewById(R.id.subtitle);
        t.setText(intent.getStringExtra("subtitle"));
        t.setTypeface(light);
        if (intent.getStringExtra("subtitle").length() == 0) t.setVisibility(View.GONE);

        t = (TextView)findViewById(R.id.speakers);
        t.setTypeface(black);
        t.setText(intent.getStringExtra("spkr"));

        t = (TextView)findViewById(R.id.abstractt);
        t.setTypeface(bold);
        String s = intent.getStringExtra("abstract");
        s = s.replaceAll("\\[(.*?)\\]\\(([^ \\)]+).*?\\)", "<a href=\"$2\">$1</a>");
        t.setText(Html.fromHtml(s), TextView.BufferType.SPANNABLE);
        t.setMovementMethod(new LinkMovementMethod());

        t = (TextView)findViewById(R.id.description);
        t.setTypeface(regular);
        s = intent.getStringExtra("descr");
        s = s.replaceAll("\\[(.*?)\\]\\(([^ \\)]+).*?\\)", "<a href=\"$2\">$1</a>");
        t.setText(Html.fromHtml(s), TextView.BufferType.SPANNABLE);
        t.setMovementMethod(new LinkMovementMethod());

        TextView l = (TextView)findViewById(R.id.linksSection);
        l.setTypeface(bold);
        String links = intent.getStringExtra("links");
        t = (TextView)findViewById(R.id.links);
        t.setTypeface(regular);

        if (links.length() > 0) {
        	Log.d(LOG_TAG, "show links");
        	l.setVisibility(View.VISIBLE);
        	t.setVisibility(View.VISIBLE);
        	links = links.replaceAll("\\),", ")<br>");
	        links = links.replaceAll("\\[(.*?)\\]\\(([^ \\)]+).*?\\)", "<a href=\"$2\">$1</a>");
	        t.setText(Html.fromHtml(links), TextView.BufferType.SPANNABLE);
	        t.setMovementMethod(new LinkMovementMethod());
        } else {
        	l.setVisibility(View.GONE);
        	t.setVisibility(View.GONE);
        }
    }

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater mi = new MenuInflater(getApplication());
		mi.inflate(R.menu.detailmenu, menu);
		if (Build.VERSION.SDK_INT < 14) {
			MenuItem item = menu.findItem(R.id.item_add_to_calendar);
			if (item != null) item.setVisible(false);
		}
		return true;
	}

	private Lecture eventid2Lecture(String event_id) {
		for (Lecture lecture : MyApp.lectureList) {
			if (lecture.lecture_id.equals(event_id)) {
				return lecture;
			}
		}
		return null;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Lecture l;
		switch (item.getItemId()) {
		case R.id.item_feedback:
			StringBuilder sb = new StringBuilder();
			sb.append(feedbackURL);
			sb.append(event_id).append(".");
			if (locale.getLanguage().equals("de")) {
				sb.append("de");
			} else {
				sb.append("en");
			}
			sb.append(".html");
			Uri uri = Uri.parse(sb.toString());
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			return true;
		case R.id.item_share:
			l = eventid2Lecture(event_id);
			if (l != null) Fahrplan.share(this, l);
			return true;
		case R.id.item_add_to_calendar:
			l = eventid2Lecture(event_id);
			if (l != null) Fahrplan.addToCalender(this, l);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
