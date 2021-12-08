package hk.siggi.bungeecord.bungeechat.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil
{
	private static Pattern timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);

	public static String removeTime(String input)
	{
		return timePattern.matcher(input).replaceFirst("").trim();
	}

	public static long parseTime(String time) {
		return parseTime(time, System.currentTimeMillis(), true);
	}

	public static long parseTime(String time, boolean future) {
		return parseTime(time, System.currentTimeMillis(), future);
	}

	public static long parseTime(String time, long originTime, boolean future) {
		Matcher m = timePattern.matcher(time);
		int years = 0;
		int months = 0;
		int weeks = 0;
		int days = 0;
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		boolean found = false;
		while (m.find())
		{
			if (m.group() == null || m.group().isEmpty())
			{
				continue;
			}
			for (int i = 0; i < m.groupCount(); i++)
			{
				if (m.group(i) != null && !m.group(i).isEmpty())
				{
					found = true;
					break;
				}
			}
			if (found)
			{
				if (m.group(1) != null && !m.group(1).isEmpty())
				{
					years = Integer.parseInt(m.group(1));
				}
				if (m.group(2) != null && !m.group(2).isEmpty())
				{
					months = Integer.parseInt(m.group(2));
				}
				if (m.group(3) != null && !m.group(3).isEmpty())
				{
					weeks = Integer.parseInt(m.group(3));
				}
				if (m.group(4) != null && !m.group(4).isEmpty())
				{
					days = Integer.parseInt(m.group(4));
				}
				if (m.group(5) != null && !m.group(5).isEmpty())
				{
					hours = Integer.parseInt(m.group(5));
				}
				if (m.group(6) != null && !m.group(6).isEmpty())
				{
					minutes = Integer.parseInt(m.group(6));
				}
				if (m.group(7) != null && !m.group(7).isEmpty())
				{
					seconds = Integer.parseInt(m.group(7));
				}
				break;
			}
		}
		if (!found)
		{
			return 0L;
		}
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(originTime);
		if (years > 0)
		{
			c.add(Calendar.YEAR, years * (future ? 1 : -1));
		}
		if (months > 0)
		{
			c.add(Calendar.MONTH, months * (future ? 1 : -1));
		}
		if (weeks > 0)
		{
			c.add(Calendar.WEEK_OF_YEAR, weeks * (future ? 1 : -1));
		}
		if (days > 0)
		{
			c.add(Calendar.DAY_OF_MONTH, days * (future ? 1 : -1));
		}
		if (hours > 0)
		{
			c.add(Calendar.HOUR_OF_DAY, hours * (future ? 1 : -1));
		}
		if (minutes > 0)
		{
			c.add(Calendar.MINUTE, minutes * (future ? 1 : -1));
		}
		if (seconds > 0)
		{
			c.add(Calendar.SECOND, seconds * (future ? 1 : -1));
		}
		return Math.abs(c.getTimeInMillis() - originTime);
	}

	private static int timeDiff(int type, Calendar fromTime, Calendar toTime, boolean future)
	{
		int diff = 0;
		long t = fromTime.getTimeInMillis();
		while ((future && !fromTime.after(toTime)) || (!future && !fromTime.before(toTime)))
		{
			t = fromTime.getTimeInMillis();
			fromTime.add(type, future ? 1 : -1);
			diff++;
		}
		diff--;
		fromTime.setTimeInMillis(t);
		return diff;
	}

	public static String timeToString(long time)
	{
		return timeToString(time, 3);
	}

	public static String timeToString(long time, int limit)
	{
		return timeToString(time, limit, false);
	}

	public static String timeToString(long time, int limit, boolean shortForm)
	{
		Calendar now = new GregorianCalendar();
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(now.getTimeInMillis() + time);
		return timeDifference(now, c, limit, shortForm);
	}

	public static String timeDifference(Calendar fromTime, Calendar toTime) {
		return timeDifference(fromTime, toTime, 3);
	}

	public static String timeDifference(Calendar fromTime, Calendar toTime, int limit)
	{
		return timeDifference(fromTime, toTime, limit, false);
	}
	public static String timeDifference(Calendar fromTime, Calendar toTime, int limit, boolean shortForm) {
		boolean future = false;
		if (toTime.equals(fromTime))
		{
			return "0 seconds";
		}
		if (toTime.after(fromTime))
		{
			future = true;
		}
		StringBuilder sb = new StringBuilder();
		int[] types = new int[]
		{
			Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND
		};
		String[] names = 
		shortForm ?
		new String[] {
			"y", "y", "mo", "mo", "d", "d", "h", "h", "m", "m", "s", "s"
		}
		:
		new String[]
		{
			"year", "years", "month", "months", "day", "days", "hour", "hours", "minute", "minutes", "second", "seconds"
		};
		int accuracy = 0;
		for (int i = 0; i < types.length; i++)
		{
			if (accuracy >= limit)
			{
				break;
			}
			int diff = timeDiff(types[i], fromTime, toTime, future);
			if (diff > 0)
			{
				accuracy++;
				sb.append(" ").append(diff).append(" ").append(names[i * 2 + (diff > 1 ? 1 : 0)]);
			}
		}
		if (sb.length() == 0)
		{
			return "0 seconds";
		}
		return sb.toString().trim();
	}
}
