package spiedie.api.http.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import spiedie.api.http.api.IDataLoader;
import spiedie.api.http.api.IHttpApi;
import spiedie.api.http.api.ISpHttpHandler;

import spiedie.utilities.net.AbstractServerSocketHandler;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;

public class SpHttpServer extends AbstractServerSocketHandler implements IHttpApi{
	public static final int MODE_TRHEAD = 1;
	public static final int MODE_POOL = 2;
	
	public static final long DEFAULT_TIMEOUT = 5000;
	public static final int DEFAULT_HEADER_SIZE_LIMIT = 1024 * 1024;
	private SpHttpServerMonitor monitor;
	private Map<String, ISpHttpHandler> handlers;
	private SpThreadPool threadPool;
	private ISpHttpHandler unknownRequestHandler, timeoutHandler, badRequestHandler;
	private List<IDataLoader> loaders;
	private List<String> allowedMethods;
	private int headerSizeLimit;
	private long requestHandlingTimeout;
	private boolean allowTrailingPath = false;
	private int handlerMode = MODE_TRHEAD;
	public SpHttpServer(int port) {
		super(port);
		this.allowedMethods = new ArrayList<>();
		this.handlers = new ConcurrentHashMap<>();
		this.loaders = new ArrayList<>();
		this.threadPool = new SpThreadPool();
		setRequestTimeout(DEFAULT_TIMEOUT);
		setHeaderSizeLimit(DEFAULT_HEADER_SIZE_LIMIT);
		setUnknownRequestHandler(new SpDefaultHandlers.DefaultUnknownRequestHandler());
		setTimeoutHandler(new SpDefaultHandlers.DefaultTimeoutHandler());
		setBadRequestHandler(new SpDefaultHandlers.DefaultBadRequestHandler());
		this.allowedMethods.add("GET");
		this.monitor = new SpHttpServerMonitor(() -> getTimeoutHandler());
	}
	
	public List<String> getAllowedMethods(){
		return allowedMethods;
	}
	
	public void setRequestTimeout(long timeout) {
		this.requestHandlingTimeout = timeout;
	}
	
	public long getRequestTimeout() {
		return this.requestHandlingTimeout;
	}
	
	public void setHeaderSizeLimit(int headerLimit) {
		this.headerSizeLimit = headerLimit;
	}
	
	public int getHeaderSizeLimit() {
		return this.headerSizeLimit;
	}
	
	public String getIp() {
		return null;
	}
	
	public ISpHttpHandler getUnknownRequestHandler() {
		return unknownRequestHandler;
	}

	public ISpHttpHandler getTimeoutHandler() {
		return timeoutHandler;
	}

	public ISpHttpHandler getBadRequestHandler() {
		return badRequestHandler;
	}
	
	public void setUnknownRequestHandler(ISpHttpHandler h) {
		unknownRequestHandler = h;
	}

	public void setTimeoutHandler(ISpHttpHandler h) {
		timeoutHandler = h;
	}

	public void setBadRequestHandler(ISpHttpHandler h) {
		badRequestHandler = h;
	}

	public Map<String, ISpHttpHandler> getHandlers(){
		return handlers;
	}
	
	public List<IDataLoader> getLoaders() {
		return loaders;
	}
	
	public SpHttpServerMonitor getMonitor() {
		return monitor;
	}
	
	public void start(String name) {
		super.start(name);
		this.monitor.start();
	}
	
	public void run() {
		super.run();
		this.monitor.setFinished(true);
	}
	
	public void handle(Socket s) throws IOException {
		Log.write(this, "Create new handler for "+s, false, false, true);
		SpHttpRequestHandler thread = new SpHttpRequestHandler(this, s, Time.millis(), Time.nanos());
		switch(handlerMode) {
		case MODE_TRHEAD: handleThread(thread); return;
		case MODE_POOL: handlePool(thread); return;
		default: throw new IllegalStateException("Unknown mode "+handlerMode);
		}
	}
	
	private void handleThread(SpHttpRequestHandler thread) {
		thread.start();
	}
	
	private void handlePool(SpHttpRequestHandler thread) {
		threadPool.add(thread);
	}
	
	public void load() throws IOException {
		getHandlers().clear();
		for(IDataLoader loader : getLoaders()) {
			loader.load(this);
		}
		List<String> paths = new ArrayList<>(getHandlers().keySet());
		Collections.sort(paths);
		for(String h : paths) {
			Log.write(this, "Handling "+h);
		}
	}
	
	public ISpHttpHandler getHandler(String query) {
		ISpHttpHandler h = handlers.get(query);
		if(h == null && allowTrailingPath) {
			for(String handlerPath : handlers.keySet()) {
				if(query.startsWith(handlerPath)) {
					return handlers.get(handlerPath);
				}
			}
		}
		return h;
	}
	
}
