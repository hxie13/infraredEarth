package cn.ac.sitp.infrared.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days); // minus number would decrement the days
        return cal.getTime();
    }

    public static Date addYears(Date date, int years) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR, years); // minus number would decrement the years
        return cal.getTime();
    }

    public static Date addMonths(Date date, int months) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, months); // minus number would decrement the months
        return cal.getTime();
    }
}
