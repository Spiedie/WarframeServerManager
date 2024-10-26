package spiedie.api.http.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import spiedie.api.http.api.ISpHttpHandler;
import spiedie.api.http.api.ISpHttpHeaders;
import spiedie.api.http.api.ISpHttpRequest;
import spiedie.utilities.concurrency.AbstractThread;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;

public class SpHttpRequestHandler extends AbstractThread implements Closeable{
	public static boolean LOG_REQUEST = true;
	
	private static final String NAME = SpHttpRequestHandler.class.getSimpleName();
	
	private SpHttpServer server;
	private Socket socket;
	private final long time, nanoTime;
	
	private static int id = 0;
	
	public SpHttpRequestHandler(SpHttpServer server, Socket socket, long time, long nanoTime) {
		this.server = server;
		this.socket = socket;
		this.time = time;
		this.nanoTime = nanoTime;
	}
	
	private static int getId() {
		return id++;
	}
	
	public void start() {
		super.start(String.format("%s-%d", NAME, getId()));
	}
	
	public void run() {
		try {
			SpHttpRequest request = new SpHttpRequest(socket, time, nanoTime, server.getRequestTimeout());
			server.getMonitor().add(request);
			ISpHttpHeaders headers = request.getRequestHeaders();
			HttpRequestInputStream in = new HttpRequestInputStream(socket.getInputStream());
			String line = in.readHttpLine(server.getHeaderSizeLimit());
			// parse request
			if(line == null || !request.setRequestHeader(line)) {
				server.getBadRequestHandler().handle(request);
				return;
			}
			// check request method
			if(!server.getAllowedMethods().stream().anyMatch(m -> request.isMethod(m))) {
				SpDefaultHandlers.handle(request, 501, null, this, "Unknown method "+request);
				return;
			}
			// parse headers
			while((line = in.readHttpLine(server.getHeaderSizeLimit())) != null) {
				if(line.isEmpty()) break;
				else {
					int separator = line.indexOf(':');
					if(separator >= 0) {
						String header = line.substring(0, separator);
						if(line.charAt(separator + 1) == ' ') {
							separator++;
						}
						String value = line.substring(separator + 1);
						headers.put(header, value);
					} else {
						
					}
				}
			}
			String q = request.getRequestURI().getPath();
			q = q.substring(1);
			ISpHttpHandler h = server.getHandler(q);
			if(h != null) {
				h.handle(request);
			} else {
				server.getUnknownRequestHandler().handle(request);
			}
			log(request, Time.millis() - request.getTime());
		} catch (IOException e) {
			if(e.getMessage().toLowerCase().contains("software caused connection abort: socket write error")) {
				Log.err(this, e);
			} else {
				Log.caught(this, e, true);
			}
			
		} finally {
			close();
		}
	}
	
	private void log(ISpHttpRequest e, long time) {
		if(LOG_REQUEST) {
			InetAddress addr = e.getRemoteAddress();
			String host = addr.getHostAddress();
			Log.write(this, "Handled from "+host+" in "+time+" ms: "+e.getRequestURI(), false, true, true);
		}
	}

	public void close() {
		Stream.close(socket);
	}
}
