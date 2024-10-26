package spiedie.warframe.util;

import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;

import spiedie.data.json.data.IJson;
import spiedie.utilities.concurrency.SimpleQueueProcessor;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;

public abstract class WFGenericWriter extends SimpleQueueProcessor<IJson>{
	protected OutputStream out;
	protected boolean stopping = false;
	protected byte[] symmetricKey;
	public WFGenericWriter(OutputStream out){
		this.out = out;
		this.delay = 10;
	}
	
	/**
	 * 
	 * @param key
	 */
	public void setKey(byte[] key){
		this.symmetricKey = key;
	}
	
	protected void process(IJson j) {
		try {
			write(j);
		} catch (SocketException e) {
			if(e.getMessage().toLowerCase().contains("connection reset")){
				Log.write(this, "Connection reset by peer", true, WFC.logPrintEnabled, true);
				setFinished(true);
			}
		} catch (IOException e) {
			if(!stopping){
				Log.caught(this, e);
			}
		}
	}
	
	/**
	 * 
	 * @param j
	 * @throws IOException
	 */
	public abstract void write(IJson j) throws IOException;
	
	/**
	 * 
	 */
	public void close(){
		setFinished(true);
		Stream.close(out);
	}
}
