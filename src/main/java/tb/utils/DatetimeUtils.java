package tb.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DatetimeUtils {

	public static Date localTimeToUtc(Date date) {
		return new Date(date.getTime()
				- TimeZone.getDefault().getOffset(date.getTime()));
	}

	public static boolean checkTimeout(Date utcDate, int timeoutMilisecond, Date checkDateLocalTimezone) {
		Calendar booking = getUtcCalendar(utcDate);
		booking.add(Calendar.MILLISECOND, timeoutMilisecond);
		return checkDateLocalTimezone.after(booking.getTime());
	}

	public static Calendar getUtcCalendar(Date utcDate) {
		// 1) utcDate without time zone
		Calendar localCalendar = Calendar.getInstance();
		localCalendar.setTime(utcDate);

		// 2) now apply UTC time zone
		Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		utcCalendar.set(localCalendar.get(Calendar.YEAR),
				localCalendar.get(Calendar.MONTH),
				localCalendar.get(Calendar.DAY_OF_MONTH),
				localCalendar.get(Calendar.HOUR_OF_DAY),
				localCalendar.get(Calendar.MINUTE));

		return utcCalendar;
	}

	public static Date offsetTimeZone(Date date, String fromTZ, String toTZ) {

		// Construct FROM and TO TimeZone instances
		TimeZone fromTimeZone = TimeZone.getTimeZone(fromTZ);
		TimeZone toTimeZone = TimeZone.getTimeZone(toTZ);

		// Get a Calendar instance using the default time zone and locale.
		Calendar calendar = Calendar.getInstance();

		// Set the calendar's time with the given date
		calendar.setTimeZone(fromTimeZone);
		calendar.setTime(date);

		System.out.println("Input: " + calendar.getTime() + " in " + fromTimeZone.getDisplayName());

		// FROM TimeZone to UTC
		calendar.add(Calendar.MILLISECOND, fromTimeZone.getRawOffset() * -1);

		if (fromTimeZone.inDaylightTime(calendar.getTime())) {
			calendar.add(Calendar.MILLISECOND, calendar.getTimeZone().getDSTSavings() * -1);
		}

		// UTC to TO TimeZone
		calendar.add(Calendar.MILLISECOND, toTimeZone.getRawOffset());

		if (toTimeZone.inDaylightTime(calendar.getTime())) {
			calendar.add(Calendar.MILLISECOND, toTimeZone.getDSTSavings());
		}

		return calendar.getTime();
	}
}
