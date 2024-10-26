package spiedie.utilities.stream.raf;

import java.io.IOException;
import java.io.OutputStream;

public class RAFOutputStream extends OutputStream{
	private IRAFStream out;
	public RAFOutputStream(IRAFStream out){
		this.out = out;
	}
	
	public void write(int b) throws IOException {
		out.write(b);
	}
	
	public void write(byte[] buf, int off, int len) throws IOException{
		out.write(buf, off, len);
	}
	
	public void flush(){
		
	}
	
	public void close() throws IOException{
		out.close();
	}
}
