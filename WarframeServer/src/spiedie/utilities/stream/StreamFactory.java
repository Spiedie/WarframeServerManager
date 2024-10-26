package spiedie.utilities.stream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class StreamFactory {
	private StreamFactory(){}
	
	public static InputStreamFactory getInputStreamFactory(){
		return new InputStreamFactory() {
			public InputStream getInputStream(InputStream in) {
				return Stream.getInputStream(in);
			}
			public InputStream getInputStream(File file) throws FileNotFoundException {
				return Stream.getInputStream(file);
			}
			public InputStream getInputStream(String filename) throws IOException {
				return Stream.getInputStream(filename);
			}
		};
	}
	
	public static OutputStreamFactory getOutputStreamFactory(){
		return new OutputStreamFactory() {
			public OutputStream getOutputStream(OutputStream out) {
				return Stream.getOutputStream(out);
			}
			public OutputStream getOutputStream(File file) throws FileNotFoundException {
				return Stream.getOutputStream(file);
			}
			public OutputStream getOutputStream(String filename) throws FileNotFoundException {
				return Stream.getOutputStream(filename);
			}
		};
	}
}
