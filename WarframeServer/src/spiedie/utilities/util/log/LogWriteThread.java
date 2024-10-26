package spiedie.utilities.util.log;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.Constants;
import spiedie.utilities.util.Time;

public class LogWriteThread implements Runnable,Closeable{
	public static final String LOG_TO_FILE = "LogToFile";
	
	private Thread t;
	private final Queue<LogEntry> q;
	private OutputStream out = null;
	private boolean running = false;
	private boolean inProgress = false;
	private boolean closed = false;
	
	public LogWriteThread(){
		q = new ConcurrentLinkedQueue<LogEntry>();
		try {
			this.out = Stream.getOutputStream(Log.logFile());
//			this.out = Stream.DRAIN;
		} catch (FileNotFoundException e) {
			System.err.println(e.getMessage());
			Constants.setProperty(LOG_TO_FILE, Constants.KEY_FALSE);
		}
		// keep the daemon thread running until the queue is empty. (flush the queue)
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){
			public void run(){
				try {
					flush();
					System.out.flush();
					close();
					if(new File(Log.logFile()).length() == 0){
						new File(Log.logFile()).delete();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		},"ShutdownHookLogWriteThread"));
		t = new Thread(this,"LogWriteThread");
	}
	
	public LogWriteThread(boolean start){
		this();
		if(start){
			start();
		}
	}
	
	public synchronized void append(LogEntry entry){
		if(!closed){
			q.add(entry);
			notifyAll();
		}
	}
	
	public void start(){
		t.setDaemon(true);
		t.start();
		running = true;
	}
	
	public void run(){
		runImpl();
	}
	
	public synchronized void runImpl(){
		try {
			while(!closed && isRunning()){
				while(!q.isEmpty()){
					inProgress = true;
					LogEntry e = q.poll();
					if(e != null) writeLog(e);
				}
				inProgress = false;
				wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void writeLog(LogEntry e) {
		String format = e.format(true);
		for(LogCapture cap : Log.captures){
			cap.capture(format);
		}
		if(e.print){
			String print = e.format(false);
			PrintStream out = e.isError ? System.err : System.out;
			out.println(print);
		}
		if(!closed && e.logToFile && Constants.use(LOG_TO_FILE, Constants.KEY_TRUE, true)){
			try {
				out.write((format+"\n").getBytes(Stream.charset));
				out.flush();
			} catch (IOException ex) {
				
			}
		}
	}
	
	public boolean isRunning(){
		return inProgress || (running && !closed);
	}
	
	public void flush(){
		while(!q.isEmpty()){
			Time.sleep(1);
		}
		while(inProgress){
			Time.sleep(1);
		}
	}
	
	public void close() throws IOException {
		if(!closed && out != null){
			closed = true;
			out.close();
		}
	}
}
