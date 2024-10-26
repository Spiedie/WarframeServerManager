package spiedie.utilities.util.log;

import java.util.Arrays;

import spiedie.utilities.util.Time;

public class LogEntry {
	public long time;
	public Thread thread;
	private Object caller;
	public Object log;
	private String logCache;
	public boolean isError, print, logToFile;
	
	private String formatCache, fullFormatCache;
	
	public LogEntry(Object caller, Object log, boolean error, boolean print, boolean logToFile){
		this(Time.millis(), Thread.currentThread(), caller, log, error, print, logToFile);
	}
	
	private LogEntry(long time, Thread thread, Object caller, Object log, boolean isError, boolean print, boolean logToFile){
		this.time = time;
		this.thread = thread;
		this.caller = caller;
		this.log = log;
		this.isError = isError;
		this.print = print;
		this.logToFile = logToFile;
	}
	
	public String log() {
		if(logCache == null) {
			String logtext = null;
			if(log == null || log.toString() == null) logtext = "null";
			else if(log instanceof Object[]){
				logtext = Arrays.toString((Object[])log);
			} else{
				logtext = log.toString();
			}
			logCache = logtext;
		}
		return logCache;
	}
	
	public String format(boolean full){
		if(full){
			if(fullFormatCache == null){
				fullFormatCache = getFormatted(full);
			}
			return fullFormatCache;
		} else{
			if(formatCache == null){
				formatCache = getFormatted(full);
			}
			return formatCache;
		}
	}
	
	private String getFormatted(boolean full){
		int compactness = full ? Log.FULL : Log.compactness;
		String logtext = "null";
		try{
			logtext = log();
		} catch(NullPointerException e){
			System.err.println(e);
		} catch(IndexOutOfBoundsException e){
			System.err.println(e);
		} catch(Exception e){
			System.err.println(e);
		}
		if(logtext.contains("\n")){
			logtext = "\n"+logtext;
			logtext = logtext.replace("\r\n", "\n");
			logtext = logtext.replace("\n", "\n\t");
		}
		String timeText = null;
		String threadText = thread.getName();
		String callerText = null;
		if(caller != null){
			if(caller instanceof String) callerText = caller.toString();
			else if(caller instanceof Class) callerText = ((Class<?>) caller).getName();
			else callerText = caller.getClass().getName();
		}
		String logPrefix = "[]";
		if(compactness == Log.COMPACT){
			threadText = shorten(thread.getName());
		} else{
			timeText = Time.toDateString("yyyy-MM-dd HH:mm:ss", time);
			if(compactness == Log.SHORT){
				threadText = shorten(thread.getName());
				if(callerText != null) callerText = shorten(callerText);
			} else{
				threadText = thread.getName();
			}
		}
		switch(compactness){
		case Log.COMPACT: logPrefix = String.format("[%s]", threadText); break;
		default: logPrefix = String.format("[%s :: %s :: %s]", timeText, threadText, callerText); break;
		}
		if(compactness != Log.RAW) logtext = logPrefix + " " + logtext;
		return logtext;
	}
	
	private static String shorten(String s){
		if(s == null) return null;
		if(s.contains(".")){
			int dot = s.lastIndexOf('.');
			if(dot >= 0) s = s.substring(dot + 1);
		}
		return s;
	}
}
