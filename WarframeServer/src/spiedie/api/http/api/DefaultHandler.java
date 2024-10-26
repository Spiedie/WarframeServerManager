package spiedie.api.http.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class DefaultHandler implements ISpHttpHandler{
	private Map<String, String> headers;
	public DefaultHandler() {
		headers = new HashMap<>();
	}
	
	public void handle(ISpHttpRequest r) throws IOException {
		addHeaders(r);
		handleExchange(r);
	}
	
	private void addHeaders(ISpHttpRequest r) {
		if(!headers.isEmpty()) {
			ISpHttpHeaders h = r.getResponseHeaders();
			for(String key : headers.keySet()) {
				h.put(key, headers.get(key));
			}
		}
	}

	public abstract void handleExchange(ISpHttpRequest e) throws IOException;
}
