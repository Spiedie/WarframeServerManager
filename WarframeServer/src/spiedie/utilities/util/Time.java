package spiedie.utilities.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import spiedie.utilities.concurrency.ThreadUtils;
import spiedie.utilities.data.StringUtils;

public class Time {
	public static final String DEFAULT_FORMAT = "yyyy/MM/dd HH:mm:ss";
	public static final long SECOND = 1000;
	public static final long MINUTE = 60 * SECOND;
	public static final long HOUR = 60 * MINUTE;
	public static final long DAY = 24 * HOUR;
	private Time(){}
	public static void sleep(long delay){
		ThreadUtils.sleep(delay);
	}
	
	public static long millis(){
		return System.currentTimeMillis();
	}
	
	public static long nanos(){
		return System.nanoTime();
	}
	
	public static String toDateString(){
		return toDateString(new Date());
	}
	
	public static String toDateString(Date d){
		return toDateString(DEFAULT_FORMAT,d);
	}
	
	public static String toDateString(long time){
		return toDateString(new Date(time));
	}
	
	public static String toDateString(String format, Date d){
		return toDateString(format, d, TimeZone.getDefault());
	}
	
	public static String toDateString(String format, Date d, TimeZone timeZone){
		SimpleDateFormat df = new SimpleDateFormat(format, Locale.ENGLISH);
		df.setTimeZone(timeZone);
		return df.format(d);
	}
	
	public static String toDateString(String format, long time){
		return toDateString(format, new Date(time));
	}
	
	private static String fill(long value){
		return fill(value, 2);
	}
	
	private static String fill(long value, int digits){
		String s = String.valueOf(value);
		while(s.length() < digits) s = "0"+s;
		return s;
	}
	
	public static String toTimeString(long time){
		long days = time/DAY;
		long hours = (time % DAY) / HOUR;
		long minutes = (time % HOUR) / MINUTE;
		long seconds = (time % MINUTE) / SECOND;
		long ms = (time % SECOND);
		StringBuilder sb = new StringBuilder();
		if(days > 0) sb.append(fill(days)+"d ");
		if(hours > 0) sb.append(fill(hours)+"h ");
		if(minutes > 0) sb.append(fill(minutes)+"m ");
		if(ms > 0){ 
			sb.append(fill(seconds)+"."+fill(ms, 3)+"s");
		} else if(seconds > 0){
			sb.append(fill(seconds)+"s");
		}
		if(sb.length() == 0) sb.append("0s");
		return sb.toString().trim();
	}
	
	public static long getTime(List<String> values){
		long time = 0;
		for(String value : values){
			double d = Double.valueOf("0"+StringUtils.keep(value, StringUtils.DIGITS+",."));
			if(value.toLowerCase().endsWith("d")) d *= Time.DAY;
			if(value.toLowerCase().endsWith("h")) d *= Time.HOUR;
			if(value.toLowerCase().endsWith("m")) d *= Time.MINUTE;
			if(value.toLowerCase().endsWith("s")) d *= Time.SECOND;
			time += d;
		}
		return time;
	}
	
}
