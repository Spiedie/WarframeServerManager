package spiedie.api.http.server;

import java.io.Closeable;

import spiedie.utilities.concurrency.SimpleQueueProcessor;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;

public class SpHttpRequestHandlerThread extends SimpleQueueProcessor<SpHttpRequestHandler> implements Closeable{

	private long lastActivity;
	private long idleTimeout;
	public SpHttpRequestHandlerThread() {
		delay = 100;
		idleTimeout = 10000;
		setDaemon(true);
	}
	
	public void setIdleTimeout(long timeout) {
		this.idleTimeout = timeout;
	}
	
	public void run() {
		super.run();
		Log.write(this, "Stop HttpThread", false, false, true);
	}
	
	protected void process(SpHttpRequestHandler t) {
		try {
			t.run();
		} finally {
			lastActivity = Time.millis();
		}
	}

	protected void updateWaiting() {
		if(idleTimeout != 0 && Time.millis() - lastActivity > idleTimeout) {
			setFinished(true);
		}
	}
}
