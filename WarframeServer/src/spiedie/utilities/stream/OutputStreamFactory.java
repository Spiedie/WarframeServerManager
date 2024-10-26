package spiedie.utilities.stream;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public interface OutputStreamFactory {
	OutputStream getOutputStream(String filename) throws IOException;
	OutputStream getOutputStream(File file) throws IOException;
	OutputStream getOutputStream(OutputStream out) throws IOException;
}
