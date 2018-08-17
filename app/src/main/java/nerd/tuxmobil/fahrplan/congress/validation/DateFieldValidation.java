package nerd.tuxmobil.fahrplan.congress.validation;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import nerd.tuxmobil.fahrplan.congress.MyApp;
import nerd.tuxmobil.fahrplan.congress.models.Lecture;
import nerd.tuxmobil.fahrplan.congress.utils.DateHelper;

public class DateFieldValidation {

    private final List<ValidationError> mValidationErrors;

    public DateFieldValidation() {
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
    public boolean validate(@NonNull List<Lecture> lectures) {
        // Order lectures by "dateUtc" field (milliseconds)
        Collections.sort(lectures, (lecture1, lecture2) -> Long.compare(lecture1.getDateUTC(), lecture2.getDateUTC()));
        if (lectures.isEmpty()) {
            return true;
        }

        // Prepare time range (first day)
        Lecture firstLecture = lectures.get(0);
        String firstDateString = firstLecture.date;
        Date firstDate = DateHelper.getDate(firstDateString, "yyyy-MM-dd");
        String formattedFirstDate = DateHelper.getFormattedDate(firstDate);

        // Prepare time range (last day)
        Lecture lastLecture = lectures.get(lectures.size() - 1);
        String lastDateString = lastLecture.date;
        Date lastDate = DateHelper.getDate(lastDateString, "yyyy-MM-dd");
        // Increment date by one day since events also happen on the last day
        // and no time information is given - only the pure date.
        lastDate = DateHelper.shiftByDays(lastDate, 1);
        String formattedLastDate = DateHelper.getFormattedDate(lastDate);

        // Check if the time stamp in <date> is within the time range (first : last day)
        // defined by "date" attribute in the <day> nodes.
        for (Lecture lecture : lectures) {
            validateEvent(lecture, firstDate, lastDate, formattedFirstDate, formattedLastDate);
        }

        // Evaluate validation
        MyApp.LogDebug(getClass().getName(), "Validation result for <date> field: " + mValidationErrors.size() + " errors.");
        return mValidationErrors.isEmpty();
    }

    private void validateEvent(Lecture lecture, Date firstDate, Date lastDate,
                               String formattedFirstDate, String formattedLastDate) {
        long dateUtcInMilliseconds = lecture.dateUTC;
        Date dateUtc = new Date();
        dateUtc.setTime(dateUtcInMilliseconds);

        Date[] dateRange = new Date[]{firstDate, lastDate};
        if (!DateHelper.dateIsWithinRange(dateUtc, dateRange)) {
            String eventId = lecture.lecture_id;
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
