package spiedie.evolutionEngine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import spiedie.utilities.data.StringUtils;

public class EELogUtils {
	private static final String PATTERN_START_TIME_STRING = "time: (.*)";
	private static final String PATTERN_LOG_TIME_STRING = "(\\d*\\.\\d{3})";
	private static final Pattern PATTERN_START_TIME = Pattern.compile(PATTERN_START_TIME_STRING);
	private static final Pattern PATTERN_LOG_TIME = Pattern.compile(PATTERN_LOG_TIME_STRING);
	public static long getStartTimeFromEntry(String line){
		String time = StringUtils.getFromPattern(line, PATTERN_START_TIME);
		if(time == null) return 0;
		return getStartTime(time);
	}
	
	public static long getStartTime(String time){
		SimpleDateFormat f = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
		try {
			Date d = f.parse(time);
			return d.getTime();
		} catch (ParseException e) {
			
		}
		return 0;
	}
	
	public static String getLogTime(String line) {
		return StringUtils.getFromPattern(line, PATTERN_LOG_TIME);
	}
	
	public static long getRelativeTimeFromTimeString(String logTime) {
		if(logTime != null){
			long time = (long) (Double.parseDouble(logTime) * 1000);
			return time;
		}
		return 0;
	}
	
	public static long getRelativeTime(String line) {
		return getRelativeTimeFromTimeString(getLogTime(line));
	}
	
}
