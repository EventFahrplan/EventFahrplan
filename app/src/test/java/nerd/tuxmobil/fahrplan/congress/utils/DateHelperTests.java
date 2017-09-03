package nerd.tuxmobil.fahrplan.congress.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class DateHelperTests {

    protected Calendar calendar;

    @Before
    public void setup() {
        calendar = Calendar.getInstance();
    }

    @Test
    public void shiftByDaysWithMonthChange() {
        calendar.set(2016, 1, 29, 0, 0, 0); // 29.2.2016
        Date date = calendar.getTime();
        calendar.set(2016, 2, 1, 0, 0, 0); // 1.3.2016
        Date shiftedDate = calendar.getTime();
        assertThat(DateHelper.shiftByDays(date, 1)).isEqualTo(shiftedDate);
    }

}
