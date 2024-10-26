package spiedie.warframe.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import spiedie.utilities.concurrency.AbstractThread;
import spiedie.utilities.net.NetUtils;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.log.Log;

public abstract class WFGenericLogin extends AbstractThread{
	protected ServerSocket ss;
	protected int port;
	public WFGenericLogin(int port){
		this.port = port;
	}
	
	public void run() {
		try {
			Log.write(this, "Listening on "+NetUtils.localIp()+":"+port);
			ss = new ServerSocket(port);
			while(isRunning()){
				try{
					Socket s = ss.accept();
					handle(s);
				} catch(IOException e){
					Log.caught(this, e);
				}
			}
		} catch (IOException e) {
			Log.caught(this, e);
		} finally{
			setFinished(true);
		}
	}

	/**
	 * 
	 * @param s
	 * @throws IOException
	 */
	public abstract void handle(Socket s) throws IOException;
	
	/**
	 * 
	 */
	public void close(){
		Stream.close(ss);
	}
}
