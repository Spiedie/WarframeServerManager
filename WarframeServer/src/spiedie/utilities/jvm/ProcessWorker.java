package spiedie.utilities.jvm;

import java.io.IOException;
import java.io.InputStream;

import spiedie.utilities.concurrency.SimpleQueueProcessor;
import spiedie.utilities.process.ProcessUtils;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;

public class ProcessWorker extends SimpleQueueProcessor<ProcessTask>{
	public static long DELAY = 1;
	
	public void process(ProcessTask task){
		try {
			read(task);
		} catch (IOException e) {
			Log.caught(this, e);
		}
	}
	
	private void read(ProcessTask task) throws IOException{
		Process p = task.getProcess();
		InputStream in = p.getInputStream();
		InputStream err = p.getErrorStream();
		StringBuilder sbIn = new StringBuilder();
		StringBuilder sbErr = new StringBuilder();
		byte[] buf = new byte[Stream.DEFAULT_BUFFER_SIZE];
		while(ProcessUtils.isRunning(p)){
			while(in.available() > 0 || err.available() > 0){
				read(in, sbIn, buf, false);
				read(err, sbErr, buf, true);
			}
			Time.sleep(DELAY);
		}
		while(in.available() > 0){
			read(in, sbIn, buf, false);
		}
		while(err.available() > 0){
			read(err, sbErr, buf, true);
		}
		task.setResult(sbIn.toString(), sbErr.toString());
	}
	
	private void read(InputStream in, StringBuilder sb, byte[] buf, boolean error) throws IOException{
		if(in.available() > 0){
			int len = in.read(buf);
			String s = new String(buf, 0, len);
			if(error) processErr(s);
			else processOut(s);
			sb.append(s);
		}
	}

	protected void processOut(String s){

	}

	protected void processErr(String s){

	}
}
