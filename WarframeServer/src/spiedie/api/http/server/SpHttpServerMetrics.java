package spiedie.api.http.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;

import spiedie.api.http.api.ISpHttpRequest;
import spiedie.api.http.api.ISpHttpServerMetrics;
import spiedie.data.json.Json;

import spiedie.data.json.data.IJsonObject;
import spiedie.utilities.data.StringUtils;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.log.Log;

public class SpHttpServerMetrics implements ISpHttpServerMetrics{
	
//	private List<UserMetric> list;
	//TODO synchronized, periodically flush to disk
	private Object lock;
	private File file;
	
	static class UserMetric {
		public int responseCode;
		public long dataSize, responseHeaderSize, time;
		public String method, query, protocol, host, ip;
		
		public IJsonObject toJson() {
			IJsonObject o = Json.object();
			o.setProperty("code", ""+responseCode);
			o.setProperty("s", ""+dataSize);
			o.setProperty("hs", ""+responseHeaderSize);
			o.setProperty("t", ""+time);
			o.setProperty("m", ""+method);
			o.setProperty("q", ""+query);
			o.setProperty("o", ""+protocol);
			o.setProperty("h", ""+host);
			o.setProperty("ip", ""+ip);
			return o;
		}
		
	}
	
	public String getName() {
		return getClass().getSimpleName();
	}
	
	public void addRequestReceived(ISpHttpRequest r) {
		
	}
	
	public void addRequestClosed(ISpHttpRequest r) {
		UserMetric m = new UserMetric();
		m.method = r.getMethod();
		m.protocol = r.getProtocol();
		URI uri = r.getRequestURI();
		m.query = uri == null ? null : uri.toString();
		m.host = r.getResponseHeaders().get("Host");
		m.responseCode = r.getResponseCode();
		m.time = r.getTime();
		InetAddress addr = r.getRemoteAddress();
		if(addr != null) m.ip = addr.getHostAddress();
		m.dataSize = StringUtils.parseLong(r.getResponseHeaders().get("Content-Length"));
		for(String header : r.getResponseHeaders().keySet()) {
			if(header != null) {
				String value = r.getResponseHeaders().get(header);
				if(value != null) {
					m.responseHeaderSize += header.length() + value.length();	
				}
			}
		}
//		list.add(m);
		flush(m);
	}
	
	private void flush(UserMetric m) {
		IJsonObject o = m.toJson();
		synchronized (lock) {
			FileOutputStream fout = null;
			try {
				fout = new FileOutputStream(file, true);
				fout.write(o.toJson().getBytes(Stream.charset));
				fout.write("\n".getBytes(Stream.charset));
			} catch (IOException e) {
				Log.caught(this, e);
			} finally {
				Stream.close(fout);
			}
		}
	}
}
