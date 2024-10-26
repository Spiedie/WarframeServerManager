package spiedie.warframe.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJson;
import spiedie.utilities.concurrency.AbstractThread;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;

public abstract class WFGenericReader<T> extends AbstractThread{
	protected InputStream in;
	protected Queue<T> q;
	protected boolean stopping = false;
	protected byte[] symmetricKey;
	public WFGenericReader(InputStream in){
		this.in = in;
		this.q = new ConcurrentLinkedDeque<>();
	}

	/**
	 * 
	 * @param q
	 */
	public void setQueue(Queue<T> q){
		this.q = q;
	}

	/**
	 * 
	 * @param key
	 */
	public void setKey(byte[] key){
		this.symmetricKey = key;
	}

	public void run() {
		Log.write(this, "Start Reading...", false, WFC.logPrintEnabled, true);
		while(!stopping && isRunning()){
			try {
				String json = Stream.readString(in, 1024 * 1024, Time.MINUTE);//1MB limit, 60 second timeout
				IJson j = Json.parse(json);
				if(j != null) handleMessage(j);
			} catch (IOException e) {
				if(!stopping){
					if(e.getMessage().toLowerCase().contains("connection reset")){
						Log.write(this, "Connection reset.", true, WFC.logPrintEnabled, true);
					} else if(e.getMessage().toLowerCase().contains("socket closed")){
						Log.write(this, "Socket closed.", true, WFC.logPrintEnabled, true);
					} else if(e.getMessage().toLowerCase().contains("could not read required data")){
						Log.write(this, "Data connection closed.", false, WFC.logPrintEnabled, true);
					} else{
						Log.caught(this, e);
					}
					setFinished(true);
				}
			} catch(Exception e){
				Log.caught(this, e);
				setFinished(true);
			}
		}
		setFinished(true);
		Log.write(this, "End Reading.", false, WFC.logPrintEnabled, true);
	}
	/**
	 * 
	 */
	public void close(){
		stopping = true;
		Stream.close(in);
		setFinished(true);
	}
	
	/**
	 * Handle the given message.
	 * @param j
	 */
	public abstract void handleMessage(IJson j);
}
