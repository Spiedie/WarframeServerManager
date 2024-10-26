package spiedie.api.http.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class HttpRequestInputStream extends InputStream{
	public static final int CR = 13;
	public static final int NL = 10;
	
	private InputStream in;
	
	public HttpRequestInputStream(InputStream in) {
		this.in = Objects.requireNonNull(in);
	}
	
	public int read() throws IOException {
		return in.read();
	}
	
	public int read(byte[] buf) throws IOException {
		return read(buf, 0, buf.length);
	}
	
	public int read(byte[] buf, int off, int len) throws IOException {
		return in.read(buf, off, len);
	}
	
	public String readHttpLine(int sizeLimit) throws IOException {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		boolean cr = false;
		boolean crnl = false;
		while(!crnl && (sizeLimit == 0 || bout.size() < sizeLimit)) {
			int b = in.read();
			if(b == -1) {
				break;
			}
			if(b == NL) {
				crnl = true;
			} else {
				if(cr) {
					bout.write(CR);
					cr = false;
				}
				if(b == CR) {
					cr = true;
				} else {
					bout.write(b);
				}
			}
		}
		if(crnl) return new String(bout.toByteArray(), StandardCharsets.US_ASCII);
		else return null;
	}
	
	public int available() throws IOException {
		return in.available();
	}
	
	public void close() throws IOException {
		this.in.close();
	}
	
	public long skip(long n) throws IOException {
		return in.skip(n);
	}
}
