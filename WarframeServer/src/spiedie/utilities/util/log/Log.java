package spiedie.utilities.util.log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import spiedie.utilities.files.FileUtils;
import spiedie.utilities.util.Constants;
import spiedie.utilities.util.Time;

public class Log {
	public static final String LOGFILE_PATH = "LogFilePath";
	public static final int FULL = 0;
	public static final int SHORT = 1;
	public static final int COMPACT = 2;
	public static final int RAW = 3;
	
	private Log(){}
	public static int compactness = SHORT;
	public static Stack<Long> times = new Stack<Long>();
	public static boolean autoFlush = false;
	private static boolean logActive = true;
	private static String logFile;
	public static Set<LogCapture> captures = Collections.newSetFromMap(new ConcurrentHashMap<LogCapture, Boolean>());
	
	private static LogWriteThread writer;
	
	public static synchronized LogWriteThread writer(){
		if(writer == null){
			writer = new LogWriteThread(true); 
		}
		return writer;
	}
	
	public static synchronized String logFile() {
		if(logFile == null) {
			logFile = getLogFile();
		}
		return logFile;
	}
	
	private static String getLogFile(){
		String base = Constants.getProperty(LOGFILE_PATH, "logs");
		if(!base.isEmpty() && !base.endsWith(File.separator)) base += File.separator;
		FileUtils.ensurePathExists(new File(base), false);
		String cur = base+"log";
		int i = 0;
		while(new File(cur+(++i)+".log").exists());
		cur += i+".log";
		return new File(cur).getAbsolutePath();
	}
	
	public static boolean active(){
		return logActive;
	}
	
	public static boolean caught(Object caller, Throwable e){
		return caught(caller, e, true);
	}
	
	public static boolean caught(Object caller, Throwable e, boolean print){
		boolean res = err(caller, e);
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintStream printer = new PrintStream(bout);
		e.printStackTrace(printer);
		if(print) e.printStackTrace();
		printer.close();
		write(caller, new String(bout.toByteArray()), true, false, true);
		return res;
	}
	// log capture
	
	// logging
	
	public static boolean write(Object text){
		return write(Log.class, text);
	}
	
	public static boolean write(Object caller, Object text){
		return write(caller, text, false, true, true);
	}
	
	public static boolean err(Object text){
		return err(Log.class, text);
	}
	
	public static boolean err(Object caller, Object text){
		return write(caller, text, true, true, true);
	}
	
	// file and/or print only logging
	
	// base log method
	
	public static boolean write(Object caller, Object text, boolean error, boolean print, boolean logToFile){
		if(active()){
			return forceWrite(caller, text, error, print, logToFile);
		}
		return false;
	}
	
	public static boolean forceWrite(Object caller, Object text, boolean error, boolean print, boolean logToFile){
		LogEntry e = new LogEntry(caller, text, error, print, logToFile);
		if(!autoFlush && writer().isRunning()){
			return sendToWriter(e);
		} else{
			return writeDirect(e);
		}
	}
	
	public static boolean sendToWriter(LogEntry e){
		writer.append(e);
		return true;
	}
	
	public static boolean writeDirect(LogEntry e){
		writer().writeLog(e);
		return true;
	}
	
	public static void flush(){
		writer().flush();
	}
	
	public static void start(){
		times.push(Time.nanos());
	}
	
	public static long end(String text){
		return end(text, "");
	}
	
	public static synchronized long end(String text, String postfix){
		long endTime = Time.nanos();
		if(times.isEmpty()) return 0;
		long elapsedTime = endTime(endTime);
		elapsedTime /= 1000000;
		write(Log.class, text+Time.toTimeString(elapsedTime)+postfix);
		return elapsedTime;
	}
	
	private static synchronized long endTime(long end){
		if(!times.isEmpty()){
			long time = end - times.pop();
			return time;
		}
		return 0;
	}
}
