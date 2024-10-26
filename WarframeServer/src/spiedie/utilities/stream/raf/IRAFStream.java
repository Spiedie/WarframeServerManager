package spiedie.utilities.stream.raf;

import java.io.IOException;

public interface IRAFStream {
	public RAFInputStream getInputStream();
	public RAFOutputStream getOutputStream();
	public int read() throws IOException;
	public int read(byte[] buf) throws IOException;
	public int read(byte[] buf, int off, int len) throws IOException;
	public int available() throws IOException;
	public long length() throws IOException;
	public long skip(long n) throws IOException;
	public void write(int b) throws IOException;
	public void write(byte[] buf) throws IOException;
	public void write(byte[] buf, int off, int len) throws IOException;
	public void seek(long n) throws IOException;
	public void flush() throws IOException;
	public void close() throws IOException;
}
