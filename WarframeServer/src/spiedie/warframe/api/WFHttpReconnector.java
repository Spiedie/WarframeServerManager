package spiedie.warframe.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import spiedie.utilities.concurrency.IThread;
import spiedie.utilities.net.AutoReconnector;

public class WFHttpReconnector extends AutoReconnector{
	public WFHttpReconnectorThread t;
	public WFHttpReconnector(String ip, int port) {
		super(ip, port);
	}

	public IThread connect(Socket s) throws IOException {
		if(t != null) t.setFinished(true);
		WFHttpReconnectorThread t = new WFHttpReconnectorThread(s.getInputStream(), s.getOutputStream());
		t.start();
		this.t = t;
		return t;
	}
	
	static class WFHttpReconnectorThread implements IThread {
		private boolean started, finished;
		public InputStream in;
		public OutputStream out;
		
		public WFHttpReconnectorThread(InputStream in, OutputStream out) {
			this.in = in;
			this.out = out;
		}
		
		public void start() {
			this.started = true;
		}

		public boolean isRunning() {
			return started && !finished;
		}

		public void setFinished(boolean finished) {
			this.finished = finished;
		}
	}
}
