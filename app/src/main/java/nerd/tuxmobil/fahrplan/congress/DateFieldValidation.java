package nerd.tuxmobil.fahrplan.congress;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nerd.tuxmobil.fahrplan.congress.FahrplanContract.LecturesTable;
import nerd.tuxmobil.fahrplan.congress.FahrplanContract.LecturesTable.Columns;

public class DateFieldValidation {

    protected SQLiteOpenHelper mLecturesDatabase;

    protected List<ValidationError> mValidationErrors;

    public DateFieldValidation(Context context) {
        mLecturesDatabase = new LecturesDBOpenHelper(context);
        mValidationErrors = new ArrayList<ValidationError>();
    }

    /**
     * Returns a list of validation errors or null.
     */
    public List<ValidationError> getValidationErrors() {
        return mValidationErrors;
    }

    /**
     * Prints all validation errors.
     */
    public void printValidationErrors() {
        for (ValidationError validationError : mValidationErrors) {
            MyApp.LogDebug(getClass().getName(), validationError.toString());
        }
    }

    /**
     * Returns true if the time stamps in the "date" fields of
     * each "event" are within a valid time range.
     * The time range is defined by the "date" attribute of
     * the "day" fields. Otherwise false is returned.
     */
    public boolean validate() {
        SQLiteDatabase db = mLecturesDatabase.getReadableDatabase();
        Cursor lectureCursor = null;
        try {
            // Order by "date" column relying on that the date formatted
            // in reverse order: yyyy-MM-dd'T'HH:mm:ssZ
            lectureCursor = db
                    .query(LecturesTable.NAME, null, null, null, null, null, Columns.DATE);

            if (lectureCursor.getCount() == 0) return true;

            // Prepare time range (first day)
            lectureCursor.moveToFirst();
            String firstDateString = lectureCursor
                    .getString(lectureCursor.getColumnIndex(LecturesTable.Columns.DATE));
            Date firstDate = DateHelper.getDate(firstDateString, "yyyy-MM-dd");
            String formattedFirstDate = DateHelper.getFormattedDate(firstDate);

            // Prepare time range (last day)
            lectureCursor.moveToLast();
            String lastDateString = lectureCursor
                    .getString(lectureCursor.getColumnIndex(LecturesTable.Columns.DATE));
            Date lastDate = DateHelper.getDate(lastDateString, "yyyy-MM-dd");
            String formattedLastDate = DateHelper.getFormattedDate(lastDate);

            // Check if the time stamp in <date> is within the time range (first : last day)
            // defined by "date" attribute in the <day> nodes.
            lectureCursor.moveToFirst();
            while (!lectureCursor.isAfterLast()) {
                long dateUtcInMilliseconds = lectureCursor
                        .getLong(lectureCursor.getColumnIndex(LecturesTable.Columns.DATE_UTC));
                Date dateUtc = new Date();
                dateUtc.setTime(dateUtcInMilliseconds);

                Date[] dateRange = new Date[]{firstDate, lastDate};
                if (!DateHelper.dateIsWithinRange(dateUtc, dateRange)) {
                    String eventId = lectureCursor.getString(
                            lectureCursor.getColumnIndex(LecturesTable.Columns.EVENT_ID));
                    String formattedDateUtc = DateHelper.getFormattedDate(dateUtc);
                    String errorMessage = "Field <date> " + formattedDateUtc + " of event "
                            + eventId +
                            " exceeds range: [ " + formattedFirstDate + " : " + formattedLastDate
                            + " ]";
                    ValidationError error = new ValidationError(errorMessage);
                    mValidationErrors.add(error);
                }
                lectureCursor.moveToNext();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            lectureCursor.close();
            db.close();
        }

        // Evaluate validation
        MyApp.LogDebug(getClass().getName(),
                "Validation result for <date> field: " + mValidationErrors.size() + " errors.");
        if (mValidationErrors.isEmpty()) {
            mValidationErrors = null;
            return true;
        }
        return false;
    }

}
