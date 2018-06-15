package nerd.tuxmobil.fahrplan.congress.validation;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable;
import info.metadude.android.eventfahrplan.database.contract.FahrplanContract.LecturesTable.Columns;
import info.metadude.android.eventfahrplan.database.sqliteopenhelper.LecturesDBOpenHelper;
import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.utils.DateHelper;

public class DateFieldValidation {

    final protected SQLiteOpenHelper mLecturesDatabase;

    final protected List<ValidationError> mValidationErrors;

    public DateFieldValidation(Context context) {
        mLecturesDatabase = new LecturesDBOpenHelper(context);
        mValidationErrors = new ArrayList<>();
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
            lectureCursor = db.query(
                    LecturesTable.NAME, null, null, null, null, null, Columns.DATE);

            if (lectureCursor.getCount() == 0) return true;

            // Prepare time range (first day)
            lectureCursor.moveToFirst();
            int dateColumnIndex = lectureCursor.getColumnIndex(Columns.DATE);
            String firstDateString = lectureCursor.getString(dateColumnIndex);
            Date firstDate = DateHelper.getDate(firstDateString, "yyyy-MM-dd");
            String formattedFirstDate = DateHelper.getFormattedDate(firstDate);

            // Prepare time range (last day)
            lectureCursor.moveToLast();
            String lastDateString = lectureCursor.getString(dateColumnIndex);
            Date lastDate = DateHelper.getDate(lastDateString, "yyyy-MM-dd");
            // Increment date by one day since events also happen on the last day
            // and no time information is given - only the pure date.
            lastDate = DateHelper.shiftByDays(lastDate, 1);
            String formattedLastDate = DateHelper.getFormattedDate(lastDate);

            // Check if the time stamp in <date> is within the time range (first : last day)
            // defined by "date" attribute in the <day> nodes.
            lectureCursor.moveToFirst();
            while (!lectureCursor.isAfterLast()) {
                validateEvent(lectureCursor, firstDate, lastDate,
                        formattedFirstDate, formattedLastDate);
                lectureCursor.moveToNext();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (lectureCursor != null) {
                lectureCursor.close();
            }
            db.close();
        }

        // Evaluate validation
        MyApp.LogDebug(getClass().getName(),
                "Validation result for <date> field: " + mValidationErrors.size() + " errors.");
        return mValidationErrors.isEmpty();
    }

    private void validateEvent(Cursor lectureCursor, Date firstDate, Date lastDate,
                               String formattedFirstDate, String formattedLastDate) {
        long dateUtcInMilliseconds = lectureCursor.getLong(
                lectureCursor.getColumnIndex(LecturesTable.Columns.DATE_UTC));
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
    }

}
