package nerd.tuxmobil.fahrplan.congress;

import java.io.StringReader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

public class FahrplanParser {
	private parser task;
	private Activity activity;
	private Context context;

	public FahrplanParser(Context context) {
		task = null;
		MyApp.parser = this;
		this.context = context;
	}

	public void parse(String fahrplan, String eTag) {
		task = new parser(activity, context);
		task.execute(fahrplan, eTag);
	}

	public void cancel()
	{
		if (task != null) task.cancel(false);
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
		if (task != null) {
			task.setActivity(activity);
		}
	}
}

class parser extends AsyncTask<String, Void, Boolean> {
	private String LOG_TAG = "ParseFahrplan";
	private ArrayList<Lecture> lectures;
	private MetaInfo meta;
	private MetaDBOpenHelper metaDB;
	private SQLiteDatabase db;
	private Activity activity;
	private boolean completed;
	private boolean result;
	private Context context;

	public parser(Activity activity, Context context) {
		this.activity = activity;
		this.completed = false;
		this.db = null;
		this.context = context;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;

		if (completed && (activity != null)) {
			notifyActivity();
		}
	}

	protected Boolean doInBackground(String... args) {

		return parseFahrplan(args[0], args[1]);

	}

	protected void onCancelled() {
		Log.d(LOG_TAG, "parse cancelled");
		if (db != null) db.close();
	}

	private void notifyActivity() {
		((Fahrplan)activity).onParseDone(result, meta.version);
		completed = false;
	}

	protected void onPostExecute(Boolean result) {
		completed = true;
		this.result = result;

		if (activity != null) {
			notifyActivity();
		}
	}

	public void storeMeta(Context context, MetaInfo meta) {
		Log.d(LOG_TAG, "storeMeta");
		metaDB = new MetaDBOpenHelper(context);

		db = metaDB.getWritableDatabase();
		ContentValues values = new ContentValues();

		try {
			db.beginTransaction();
			db.delete("meta", null, null);
			values.put("numdays", meta.numdays);
			values.put("version", meta.version);
			values.put("title", meta.title);
			values.put("subtitle", meta.subtitle);
			values.put("day_change_hour", meta.dayChangeHour);
			values.put("day_change_minute", meta.dayChangeMinute);
			values.put("etag", meta.eTag);

			db.insert("meta", null, values);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	public void storeLectureList(Context context, ArrayList<Lecture> lectures) {
		Log.d(LOG_TAG, "storeLectureList");
		LecturesDBOpenHelper lecturesDB = new LecturesDBOpenHelper(context);

		db = lecturesDB.getWritableDatabase();
		try {
			db.beginTransaction();
			db.delete("lectures", null, null);
			for (Lecture lecture : lectures) {
				if (isCancelled()) break;
				ContentValues values = new ContentValues();
				values.put("event_id", lecture.lecture_id);
				values.put("title", lecture.title);
				values.put("subtitle", lecture.subtitle);
				values.put("day", lecture.day);
				values.put("room", lecture.room);
				values.put("start", lecture.startTime);
				values.put("duration", lecture.duration);
				values.put("speakers", lecture.speakers);
				values.put("track", lecture.track);
				values.put("type", lecture.type);
				values.put("lang", lecture.lang);
				values.put("abstract", lecture.abstractt);
				values.put("descr", lecture.description);
				values.put("links", lecture.links);
				values.put("relStart", lecture.relStartTime);
				values.put("date", lecture.date);
				db.insert("lectures", null, values);
			}
			db.setTransactionSuccessful();
		} catch (SQLException e) {
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	private Boolean parseFahrplan(String fahrplan, String eTag) {
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(new StringReader(fahrplan));
			int eventType = parser.getEventType();
			boolean done = false;
			int numdays = 0;
			String room = null;
			int day = 0;
			int dayChangeTime = 600; // hardcoded as not provided
			String date = "";
			while (eventType != XmlPullParser.END_DOCUMENT && !done && !isCancelled()) {
				String name = null;
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					lectures = new ArrayList<Lecture>();
					meta = new MetaInfo();
					break;
				case XmlPullParser.START_TAG:
					name = parser.getName();
					if (name.equals("day")) {
						day = Integer.parseInt(parser.getAttributeValue(null,
								"index"));
						date = parser.getAttributeValue(null, "date");
						if (day > numdays) { numdays = day; }
					}
					if (name.equals("room")) {
						room = new String(parser.getAttributeValue(null, "name"));
					}
					if (name.equalsIgnoreCase("event")) {
						String id = parser.getAttributeValue(null, "id");
						Lecture lecture = new Lecture(id);
						lecture.day = day;
						lecture.room = room;
						lecture.date = date;
						eventType = parser.next();
						boolean lecture_done = false;
						while (eventType != XmlPullParser.END_DOCUMENT
								&& !lecture_done && !isCancelled()) {
							switch (eventType) {
							case XmlPullParser.END_TAG:
								name = parser.getName();
								if (name.equals("event")) {
									lectures.add(lecture);
									lecture_done = true;
								}
								break;
							case XmlPullParser.START_TAG:
								name = parser.getName();
								if (name.equals("title")) {
									parser.next();
									if (parser.getText() != null) lecture.title = parser.getText();
								} else if (name.equals("subtitle")) {
									parser.next();
									if (parser.getText() != null) lecture.subtitle = parser.getText();
								} else if (name.equals("track")) {
									parser.next();
									if (parser.getText() != null) lecture.track = parser.getText();
								} else if (name.equals("type")) {
									parser.next();
									if (parser.getText() != null) lecture.type = parser.getText();
								} else if (name.equals("language")) {
									parser.next();
									if (parser.getText() != null) lecture.lang = parser.getText();
								} else if (name.equals("abstract")) {
									parser.next();
									if (parser.getText() != null) lecture.abstractt = parser.getText();
								} else if (name.equals("description")) {
									parser.next();
									if (parser.getText() != null) lecture.description = parser.getText();
								} else if (name.equals("person")) {
									parser.next();
									if (parser.getText() != null) {
										lecture.speakers = lecture.speakers + (lecture.speakers.length() > 0 ? ";":"") + parser.getText();
									}
								} else if (name.equals("link")) {
									String url = parser.getAttributeValue(null, "href");
									parser.next();
									String urlname = parser.getText();
									StringBuilder sb = new StringBuilder();
									if (lecture.links.length() > 0) {
										sb.append(lecture.links);
										sb.append(",");
									}
									sb.append("[").append(urlname).append("]").append("(").append(url).append(")");
									lecture.links = sb.toString();
								} else if (name.equals("start")) {
									parser.next();
									if (parser.getText() != null) lecture.startTime = Lecture.parseStartTime(parser.getText());
									lecture.relStartTime = lecture.startTime;
									if (lecture.relStartTime < dayChangeTime) lecture.relStartTime += (24*60);
								} else if (name.equals("duration")) {
									parser.next();
									if (parser.getText() != null) lecture.duration = Lecture.parseDuration(parser.getText());
								}
								break;
							}
							if (lecture_done) break;
							eventType = parser.next();
						}
					} else if (name.equalsIgnoreCase("conference")) {
						boolean conf_done = false;
						eventType = parser.next();
						while (eventType != XmlPullParser.END_DOCUMENT
								&& !conf_done) {
							switch (eventType) {
							case XmlPullParser.END_TAG:
								name = parser.getName();
								if (name.equals("conference")) {
									conf_done = true;
								}
								break;
							case XmlPullParser.START_TAG:
								name = parser.getName();
								if (name.equals("subtitle")) {
									parser.next();
									if (parser.getText() != null) {
										meta.subtitle = new String(parser.getText());
									} else {
										meta.subtitle = "";
									}
								}
								if (name.equals("title")) {
									parser.next();
									meta.title = new String(parser.getText());
								}
								if (name.equals("release")) {
									parser.next();
									meta.version = new String(parser.getText());
								}
								if (name.equals("day_change")) {
									parser.next();
									if (parser.getText() != null) dayChangeTime = Lecture.parseStartTime(parser.getText());
								}
								break;
							}
							if (conf_done) break;
							eventType = parser.next();
						}
					}
					break;
				}
				eventType = parser.next();
			}
			meta.numdays = numdays;
			if (isCancelled()) return false;
			storeLectureList(context, lectures);
			if (isCancelled()) return false;
			meta.eTag = eTag;
			storeMeta(context, meta);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}