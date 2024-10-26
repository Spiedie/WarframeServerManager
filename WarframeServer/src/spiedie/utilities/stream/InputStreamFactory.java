package spiedie.utilities.stream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface InputStreamFactory {
	InputStream getInputStream(String filename) throws IOException;
	InputStream getInputStream(File file) throws IOException;
	InputStream getInputStream(InputStream in) throws IOException;
}
