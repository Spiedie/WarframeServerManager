package spiedie.utilities.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;

import spiedie.utilities.concurrency.AbstractThread;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.log.Log;

public abstract class AbstractServerSocketHandler extends AbstractThread implements Closeable{
	protected ServerSocket ss;
	protected int port;
	protected boolean closing;
	protected ServerSocketFactory factory;
	public AbstractServerSocketHandler(int port){
		this.port = port;
	}
	
	public int getPort() {
		return port;
	}
	
	public boolean isRunning() {
		return super.isRunning() && !closing;
	}
	
	public void run() {
		try {
			String localInfo = NetUtils.localIp()+":"+port;
			Log.write(this, "Listening on "+localInfo);
			ss = factory == null ? new ServerSocket(port) : factory.createServerSocket(port);
			while(isRunning()){
				try{
					Socket s = ss.accept();
					handle(s);
				} catch(IOException e){
					if(!closing) {
						Log.caught(this, e);
					} else {
						Log.err(this, "Closed login "+localInfo);
					}
				}
			}
		} catch (IOException e) {
			Log.caught(this, e);
		} finally{
			setFinished(true);
			Stream.close(this);
		}
	}
	
	public void setFinished(boolean finished) {
		super.setFinished(finished);
	}

	/**
	 * 
	 * @param s the new Socket
	 * @throws IOException
	 */
	public abstract void handle(Socket s) throws IOException;
	
	/**
	 * 
	 */
	public void close() throws IOException{
		closing = true;
		setFinished(true);
		Stream.close(ss);
	}
}
