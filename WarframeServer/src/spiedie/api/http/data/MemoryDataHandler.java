package spiedie.api.http.data;

import java.io.IOException;

import spiedie.api.http.api.DefaultHandler;
import spiedie.api.http.api.ISpHttpRequest;

public class MemoryDataHandler extends DefaultHandler{
	private byte[] buf;
	
	public MemoryDataHandler(byte[] buf) {
		this.buf = buf;
	}
	
	public void handleExchange(ISpHttpRequest e) throws IOException {
		e.sendResponseHeaders(200, buf.length);
		e.getResponseBody().write(buf);
		e.close();
	}
}
