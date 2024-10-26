package spiedie.api.http.api;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;

public interface ISpHttpRequest extends Closeable{
	public long getTimeout();
	public long getTime();
	
	public String getMethod();
	public String getProtocol();
	public URI getRequestURI();
	public InetAddress getRemoteAddress();
	
	public ISpHttpHeaders getResponseHeaders();
	public ISpHttpHeaders getRequestHeaders();
	public InputStream getRequestBody();
	public OutputStream getResponseBody();
	
	public int getResponseCode();
	public void sendResponseHeaders(int code, long contentLength) throws IOException;
	
}
