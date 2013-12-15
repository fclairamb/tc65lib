package org.javacint.time;

import java.util.Calendar;
import java.util.Date;
import org.javacint.at.ATExecution;

/**
 * Date management. This class allows to get the right time by calculating an
 * offset between the chip time and exact time.<br /> This is necessary because
 * the TC65i doesn't apply the clock change instantly.
 */
public class DateManagement {

    /**
     * String date to timestamp.
     *
     * @param sDate Date in the string format DDMMYYhhmmss
     * @return Unix timestamp in seconds
     */
    public static long stringDateToTimestamp(String sDate) {
        // DD+MM+YY+hh+mm+ss;
        int DD = Integer.parseInt(sDate.substring(0, 2)),
                MM = Integer.parseInt(sDate.substring(2, 4)),
                YY = Integer.parseInt(sDate.substring(4, 6)),
                hh = Integer.parseInt(sDate.substring(6, 8)),
                mm = Integer.parseInt(sDate.substring(8, 10)),
                ss = Integer.parseInt(sDate.substring(10, 12));

        Calendar cal = Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_MONTH, DD);
        cal.set(java.util.Calendar.MONTH, MM - 1);
        cal.set(java.util.Calendar.YEAR, 2000 + YY);
        cal.set(java.util.Calendar.HOUR_OF_DAY, hh);
        cal.set(java.util.Calendar.MINUTE, mm);
        cal.set(java.util.Calendar.SECOND, ss);
        Date date = cal.getTime();
        long time = date.getTime();

        return time;
    }
    private static long timeOffset_;

    /**
     * Give an UNIX equivalent time
     *
     * @return The UNIX equivalent time
     */
    public static long time() {
        return chipTime() + timeOffset_;
    }

    public static void setCurrentTime(long time) {
        // This doesn't change the JVM's time, it's only useful for the next startup
        ATExecution.setRTClock(new Date(time * 1000));
        
        timeOffset_ = time - chipTime();
    }

    public static Date date() {
        return new Date(time() * 1000);
    }

    public static long chipTime() {
        return (new Date()).getTime() / 1000;
    }

    public static Date realDateToChipDate(Date date) {
        return new Date(date.getTime() - (timeOffset_ * 1000));
    }

    public static Date chipDateToRealDate(Date date) {
        return new Date(date.getTime() + (timeOffset_ * 1000));
    }

    public static Date nextDate(Date date, int hour, int minute, int second) {

        long currentTime = date.getTime();

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        if (hour != -1) {
            cal.set(Calendar.HOUR_OF_DAY, hour);
        }
        if (minute != -1) {
            cal.set(Calendar.MINUTE, minute);
        }
        if (second != -1) {
            cal.set(Calendar.SECOND, second);
        }

        long calTime = cal.getTime().getTime();

        while (calTime < currentTime) {
            if (hour != -1) { // if hour is specified
                // We add 1 day
                calTime += 24 * 3600 * 1000;
            } else if (minute != -1) {
                // If minute is specified, we add one hour
                calTime += 3600 * 1000;
            } else if (second != -1) {
                // if second is specified, we add one minute
                calTime += 60 * 1000;
            }
        }

        return new Date(calTime);

    }
    public static long THE_PAST = 1368745211;

    /**
     * Check if we have a correct time.
     *
     * This only check if we are not years in the past.
     *
     * @return true if we have a correct time
     */
    public static boolean synced() {
        return time() > THE_PAST;
    }
}
