package spiedie.api.http.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import spiedie.api.http.api.ISpHttpHeaders;
import spiedie.api.http.api.ISpHttpRequest;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.log.Log;

public class SpHttpRequest implements ISpHttpRequest {
	public final Socket socket;
	public final long time, nanoTime, timeout;
	private int responseCode;
	private ISpHttpHeaders requestHeaders, responseHeaders;
	private InputStream in;
	private OutputStream out;
	private String method, path, protocol;
	public SpHttpRequest(Socket s, long time, long nanoTime, long timeout) throws IOException {
		this.socket = s;
		this.timeout = timeout;
		this.time = time;
		this.nanoTime = nanoTime;
		this.requestHeaders = new SpHttpHeaders();
		this.responseHeaders = new SpHttpHeaders();
		this.in = s.getInputStream();
		this.out = s.getOutputStream();
		getResponseHeaders().put("Connection", "close");
	}
	
	public boolean setRequestHeader(String header) {
		if(header == null) return false;
		String[] parts = header.split(" ");
		if(parts.length < 3) return false;
		setMethod(parts[0]);
		path = parts[1];
		protocol = parts[2];
		return true;
	}
	
	public void close() {
		Stream.close(socket);
	}

	public long getTimeout() {
		return timeout;
	}

	public long getTime() {
		return time;
	}
	
	public String getProtocol() {
		return protocol;
	}

	public int getResponseCode() {
		return responseCode;
	}
	
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public InputStream getRequestBody() {
		return in;
	}
	
	public OutputStream getResponseBody() {
		return out;
	}
	
	public void sendResponseHeaders(int code, long contentLength) throws IOException {
		OutputStream out = getResponseBody();
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("HTTP/1.1 %d %s\r\n", code, getStatusText(code)));
		ISpHttpHeaders headers = getResponseHeaders();
		headers.put("Content-Length", ""+contentLength);
		for(String header : headers.keySet()) {
			String value = headers.get(header);
			sb.append(String.format("%s: %s\r\n", header, value));
		}
		sb.append("\r\n");
		setResponseCode(code);
		out.write(sb.toString().getBytes(StandardCharsets.US_ASCII));
		out.flush();
	}
	
	public String getMethod() {
		return method;
	}
	
	public void setMethod(String method) {
		this.method = method;
	}
	
	public boolean isMethod(String method) {
		if(getMethod() == null) return method == null;
		return getMethod().equals(method);
	}

	public URI getRequestURI() {
		if(path == null) return null;
		try {
			return new URI(path);
		} catch (URISyntaxException e) {
			Log.caught(this, e);
		}
		return null;
	}
	
	public InetAddress getRemoteAddress() {
		return socket.getInetAddress();
	}

	public ISpHttpHeaders getResponseHeaders() {
		return this.responseHeaders;
	}

	public ISpHttpHeaders getRequestHeaders() {
		return this.requestHeaders;
	}
	
	public String toString() {
		InetAddress addr = getRemoteAddress();
		return String.format("%s %s %s %s", addr == null ? "null" : addr.getHostName(), getMethod(), path, getProtocol());
	}
	
	private static String getStatusText(int code) {
		switch(code) {
		case 100: return "Continue";
		case 200: return "OK";
		case 400: return "Bad Request";
		case 403: return "Forbidden";
		case 404: return "Not Found";
		case 408: return "Request Timeout";
		case 500: return "Internal Server Error";
		case 501: return "Not Implemented";
		}
		code = code/100;
		switch(code) {
		case 2: return "OK";
		case 4: return "Bad Request";
		case 5: return "Internal Server Error";
		}
		return "OK";
	}
}
