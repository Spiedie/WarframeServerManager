package spiedie.utilities.stream.raf;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RAFStream implements IRAFStream{
	public final RAFInputStream in;
	public final RAFOutputStream out;
	public final RandomAccessFile raf;
	public RAFStream(RandomAccessFile file){
		this.raf = file;
		this.in = new RAFInputStream(this);
		this.out = new RAFOutputStream(this);
	}
	
	public RAFInputStream getInputStream() {
		return in;
	}

	public RAFOutputStream getOutputStream() {
		return out;
	}
	
	public long length() throws IOException {
		return raf.length();
	}
	
	public int read() throws IOException {
		return raf.read();
	}

	public int read(byte[] buf) throws IOException {
		return read(buf, 0, buf.length);
	}

	public int read(byte[] buf, int off, int len) throws IOException {
		return raf.read(buf, off, len);
	}
	
	public void write(int b) throws IOException {
		raf.write(b);
	}
	
	public void write(byte[] buf) throws IOException {
		write(buf, 0, buf.length);
	}

	public void write(byte[] buf, int off, int len) throws IOException {
		raf.write(buf, off, len);
	}

	public int available() throws IOException {
		long av = raf.length() - raf.getFilePointer();
		if(av > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return (int) av;
	}

	public long skip(long n) throws IOException {
		long newPointer = raf.getFilePointer() + n;
		this.seek(newPointer);
		return n;
	}
	
	public void seek(long n) throws IOException{
		raf.seek(n);
	}

	public void flush() throws IOException {
		
	}
	
	public void close() throws IOException {
		raf.close();
	}
}
