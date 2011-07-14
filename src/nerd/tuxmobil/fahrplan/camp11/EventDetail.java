package nerd.tuxmobil.fahrplan.camp11;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class EventDetail extends Activity {

	private final String LOG_TAG = "Detail";
	private String event_id;
	private String title;
	private int startTime;
	private static String feedbackURL = "https://cccv.pentabarf.org/feedback/27C3/event/"; // + 4302.en.html
	private Locale locale;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        
        Intent intent = getIntent();
        
        locale = getResources().getConfiguration().locale;
     
        event_id = intent.getStringExtra("eventid");
        startTime = intent.getIntExtra("time", 0);
        
        TextView t = (TextView)findViewById(R.id.title);
        title = intent.getStringExtra("title");
        t.setText(title);
        
        t = (TextView)findViewById(R.id.subtitle);
        t.setText(intent.getStringExtra("subtitle"));
        
        t = (TextView)findViewById(R.id.speakers);
        t.setText(intent.getStringExtra("spkr"));
        
        t = (TextView)findViewById(R.id.abstractt);
        String s = intent.getStringExtra("abstract");
        s = s.replaceAll("\\[(.*?)\\]\\(([^ \\)]+).*?\\)", "<a href=\"$2\">$1</a>");   
        t.setText(Html.fromHtml(s), TextView.BufferType.SPANNABLE);
        t.setMovementMethod(new LinkMovementMethod());
        
        t = (TextView)findViewById(R.id.description);
        s = intent.getStringExtra("descr");
        s = s.replaceAll("\\[(.*?)\\]\\(([^ \\)]+).*?\\)", "<a href=\"$2\">$1</a>");   
        t.setText(Html.fromHtml(s), TextView.BufferType.SPANNABLE);
        t.setMovementMethod(new LinkMovementMethod());
        
        TextView l = (TextView)findViewById(R.id.linksSection);
        String links = intent.getStringExtra("links");
        t = (TextView)findViewById(R.id.links);
        
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
		// disabled until feedback URL is published
//		MenuInflater mi = new MenuInflater(getApplication());
//		mi.inflate(R.menu.detailmenu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
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
		}
		return super.onOptionsItemSelected(item);
	}

}
