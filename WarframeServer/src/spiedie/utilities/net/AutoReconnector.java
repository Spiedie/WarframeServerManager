package spiedie.utilities.net;

import java.io.IOException;
import java.net.Socket;

import spiedie.utilities.concurrency.AbstractThread;
import spiedie.utilities.concurrency.IThread;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;

public abstract class AutoReconnector extends AbstractThread{
	private String ip;
	private int port;
	protected long initBackoff, maxbackoff;
	public AutoReconnector(String ip, int port){
		setIp(ip);
		setPort(port);
		initBackoff = 1000;
		maxbackoff = 16000;
	}
	
	/**
	 * 
	 * @param ip
	 */
	public void setIp(String ip){
		this.ip = ip.trim();
	}
	
	/**
	 * 
	 * @param port
	 */
	public void setPort(int port){
		this.port = port;
	}
	
	/**
	 * 
	 * @return
	 */
	
	public void start() {
		start(getClass().getSimpleName());
	}
	
	public void run(){
		if(ip == null || port == 0) throw new IllegalArgumentException("Ip/port not set.");
		runSocket();
	}
	
	protected String getAttemptConnectMessage(String ip, int port){
		return "Attempt connect to "+ip+":"+port;
	}
	
	/**
	 * 
	 */
	public void runSocket(){
		try {
			long backoff = initBackoff;
			while(isRunning()){
				IThread con = null;
				try{
					write(getAttemptConnectMessage(ip, port));
					Socket s = new Socket(ip, port);
					con = connect(s);
					Time.sleep(5000);
					while(con.isRunning()){
						Time.sleep(initBackoff);
					}
					backoff = initBackoff;
				} catch(IOException e){
					write("Connection failed with reason \""+e.getMessage()+"\"");
					backoff = Math.min(maxbackoff, backoff * 2);
				}
				if(isRunning()){
					write("Retry in "+backoff);
					Time.sleep(backoff);
				}
 			}
		} catch (Throwable t) {
			Log.caught(this, t);
			setFinished(true);
		} finally {
			setFinished(true);
		}
	}
	
	/**
	 * logs a message.
	 * @param msg the message.
	 */
	public void write(Object msg) {
		Log.write(this, msg);
	}
	
	/**
	 * 
	 * @param s the Socket
	 * @return an instance of IThread
	 * @throws IOException
	 */
	public abstract IThread connect(Socket s) throws IOException;

}
