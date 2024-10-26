package spiedie.utilities.stream.raf;

import java.io.IOException;
import java.io.InputStream;

public class RAFInputStream extends InputStream{
	private IRAFStream in;
	public RAFInputStream(IRAFStream in){
		this.in = in;
	}
	
	public int read() throws IOException {
		return in.read();
	}
	
	public int read(byte[] buf) throws IOException{
		return read(buf, 0, buf.length);
	}
	
	public int read(byte[] buf, int off, int len) throws IOException{
		return in.read(buf, off, len);
	}
	
	public int available() throws IOException{
		return in.available();
	}
	
	public void close() throws IOException{
		in.close();
	}
	
	public long skip(long n) throws IOException{
		return in.skip(n);
	}
	
}
