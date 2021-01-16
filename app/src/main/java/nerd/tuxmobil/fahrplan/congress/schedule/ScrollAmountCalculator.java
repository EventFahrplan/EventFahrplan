package nerd.tuxmobil.fahrplan.congress.schedule;

import androidx.annotation.NonNull;

import java.util.List;

import info.metadude.android.eventfahrplan.commons.logging.Logging;
import info.metadude.android.eventfahrplan.commons.temporal.Moment;
import nerd.tuxmobil.fahrplan.congress.models.DateInfos;
import nerd.tuxmobil.fahrplan.congress.models.RoomData;
import nerd.tuxmobil.fahrplan.congress.models.ScheduleData;
import nerd.tuxmobil.fahrplan.congress.models.Session;

import static nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.BOX_HEIGHT_MULTIPLIER;
import static nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.FIFTEEN_MINUTES;
import static nerd.tuxmobil.fahrplan.congress.schedule.FahrplanFragment.ONE_DAY;

/**
 * Calculates the amount to be scrolled depending on the given schedule data,
 * device specifics and the current date/time.
 */
class ScrollAmountCalculator {

    @NonNull
    private static final String LOG_TAG = "ScrollAmountCalculator";

    @NonNull
    private final Logging logging;
    @NonNull
    private final DateInfos dateInfos;
    @NonNull
    private final ScheduleData scheduleData;
    @NonNull
    private final Conference conference;

    public ScrollAmountCalculator(
            @NonNull Logging logging,
            @NonNull DateInfos dateInfos,
            @NonNull ScheduleData scheduleData,
            @NonNull Conference conference
    ) {
        this.logging = logging;
        this.dateInfos = dateInfos;
        this.scheduleData = scheduleData;
        this.conference = conference;
    }

    /**
     * Returns the amount to be scrolled. Valid values are 0 and positive integers.
     */
    public int calculateScrollAmount(@NonNull Moment nowMoment, int currentDayIndex, int boxHeight, int columnIndex) {
        int time = conference.getFirstSessionStartsAt();
        int printTime = time;
        int scrollAmount = 0;

        if (!(nowMoment.getMinuteOfDay() < conference.getFirstSessionStartsAt()
                && dateInfos.sameDay(nowMoment, currentDayIndex))) {

            TimeSegment timeSegment;
            while (time < conference.getLastSessionEndsAt()) {
                timeSegment = TimeSegment.ofMinutesOfTheDay(printTime);
                if (timeSegment.isMatched(nowMoment, FIFTEEN_MINUTES)) {
                    break;
                } else {
                    scrollAmount += boxHeight * BOX_HEIGHT_MULTIPLIER;
                }
                time += FIFTEEN_MINUTES;
                printTime = time;
                if (printTime >= ONE_DAY) {
                    printTime -= ONE_DAY;
                }
            }

            List<RoomData> roomDataList = scheduleData.getRoomDataList();
            if (columnIndex >= 0 && columnIndex < roomDataList.size()) {
                RoomData roomData = roomDataList.get(columnIndex);
                for (Session session : roomData.getSessions()) {
                    if (session.startTime <= time && session.getEndsAtTime() > time) {
                        logging.d(LOG_TAG, session.title);
                        logging.d(LOG_TAG, time + " " + session.startTime + "/" + session.duration);
                        scrollAmount -= ((time - session.startTime) / TimeSegment.TIME_GRID_MINIMUM_SEGMENT_HEIGHT) * boxHeight;
                        time = session.startTime;
                    }
                }
            }
        }
        return scrollAmount;
    }

}
