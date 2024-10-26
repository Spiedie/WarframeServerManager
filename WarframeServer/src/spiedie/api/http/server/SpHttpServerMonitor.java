package spiedie.api.http.server;

import java.io.IOException;
import java.util.function.Supplier;

import spiedie.api.http.api.ISpHttpHandler;
import spiedie.api.http.api.ISpHttpRequest;
import spiedie.api.http.api.ISpHttpServerMetrics;
import spiedie.utilities.concurrency.SimpleQueueProcessor;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;

public class SpHttpServerMonitor extends SimpleQueueProcessor<ISpHttpRequest>{
	private Supplier<ISpHttpHandler> timeoutHandler;
	private ISpHttpServerMetrics metrics;
	
	public SpHttpServerMonitor(Supplier<ISpHttpHandler> timeoutHandler) {
		this(timeoutHandler, null);
	}
	
	public SpHttpServerMonitor(Supplier<ISpHttpHandler> timeoutHandler, ISpHttpServerMetrics metrics) {
		this.delay = 1000;
		this.timeoutHandler = timeoutHandler;
		this.metrics = metrics;
		this.setDaemon(true);
	}
	
	public ISpHttpServerMetrics getMetrics() {
		return metrics;
	}
	
	public void add(ISpHttpRequest r) {
		super.add(r);
		if(getMetrics() != null) getMetrics().addRequestReceived(r);
	}
	
	protected void process(ISpHttpRequest r) {
//		Log.write(this, "Monitor check "+r);
		long time = Time.millis();
		if(left(r, time) > 0) {
			Time.sleep(left(r, time));
		}
		while(left(r, Time.millis()) > 0) {
			Time.sleep(1);
		}
		try {
			if(r.getResponseCode() == 0) {
				Log.write(this, "Timeout on "+r);
				timeoutHandler.get().handle(r);
			}
		} catch (IOException e) {
			if(e.getMessage().equals("Socket closed")
					|| e.getMessage().equals("Connection reset")
					|| e.getMessage().equals("Connection or inbound has closed")
					|| e.getMessage().equals("Connection or outbound has closed")) {
				Log.err(this, e.getMessage());
			} else {
				Log.err(this, e.getMessage());
				Log.caught(this, e, false);
			}
			
		} finally {
			Stream.close(r);
			if(getMetrics() != null) getMetrics().addRequestClosed(r);
		}
//		Log.write(this, "End of "+r);
	}
	
	private static long left(ISpHttpRequest t, long time) {
		return t.getTimeout() - (time - t.getTime());
	}
}
