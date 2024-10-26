package spiedie.api.http.server;

import java.io.IOException;

import spiedie.api.http.api.ISpHttpHandler;
import spiedie.api.http.api.ISpHttpRequest;
import spiedie.utilities.util.log.Log;

public class SpDefaultHandlers {
	public static class DefaultBadRequestHandler implements ISpHttpHandler{
		public void handle(ISpHttpRequest r) throws IOException {
			r.sendResponseHeaders(400, 0);
			r.close();
		}
	}
	
	public static class DefaultTimeoutHandler implements ISpHttpHandler{
		public void handle(ISpHttpRequest r) throws IOException {
			r.sendResponseHeaders(408, 0);
			r.close();
		}
	}
	
	public static class DefaultUnknownRequestHandler implements ISpHttpHandler{
		public void handle(ISpHttpRequest r) throws IOException {
			Log.err(this, "Unknown handler for "+r);
			r.sendResponseHeaders(404, 0);
			r.close();
		}
	}
	
	public static void handle(ISpHttpRequest r, int code, String response, Object src, String log) throws IOException {
		if(log != null) Log.write(src, log);
		if(log == null) r.sendResponseHeaders(code, 0);
		else {
			if(response == null) response = "";
			byte[] buf = response.getBytes("UTF-8");
			r.sendResponseHeaders(code, buf.length);
			r.getResponseBody().write(buf);
			r.getResponseBody().flush();
		}
		r.close();
	}
}
