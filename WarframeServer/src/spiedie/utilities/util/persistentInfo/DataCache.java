package spiedie.utilities.util.persistentInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import spiedie.utilities.files.FileUtils;
import spiedie.utilities.stream.InputStreamFactory;
import spiedie.utilities.stream.OutputStreamFactory;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.stream.StreamFactory;
import spiedie.utilities.util.ISettings;
import spiedie.utilities.util.log.Log;

public class DataCache {
	public static final String DATA_CACHE_ALT = "spiedie.DataCacheAlt";
	public static final String EXTENSION = ".dat";
	private static final String PREFIX = "info"+File.separator;
	
	static final String getPrefix(ISettings options){
		String res = PREFIX;
		if(options != null && options.isSet(DATA_CACHE_ALT)){
			res = options.getProperty(DATA_CACHE_ALT);
		}
		if(!res.endsWith(File.separator)) res += File.separator;
		return res;
	}
	
	public static boolean isSet(String key, InputStreamFactory factory, ISettings options) throws IOException{
		return getProperty(key, factory, options) != null;
	}
	
	public static byte[] getProperty(String key, InputStreamFactory factory, ISettings options) throws IOException{
		if(factory == null) factory = StreamFactory.getInputStreamFactory();
		File f = new File(getPrefix(options)+key+EXTENSION);
		if(f.exists()){
			InputStream in = factory.getInputStream(f);
			byte[] buf = Stream.read(in);
			in.close();
			return buf;
		}
		return null;
	}
	
	public static void setProperty(String key, byte[] buf, OutputStreamFactory factory, ISettings options) throws IOException{
		if(factory == null) factory = StreamFactory.getOutputStreamFactory();
		File f = new File(getPrefix(options)+key+EXTENSION);
		if(!FileUtils.ensurePathExists(f, true)) Log.err(DataCache.class, "Could not create directory structure for "+key+" in "+f.getAbsolutePath());
		OutputStream out = factory.getOutputStream(f);
		out.write(buf);
		out.close();
	}
	
}
