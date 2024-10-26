package spiedie.utilities.util.persistentInfo;

import java.io.IOException;

import spiedie.utilities.stream.InputStreamFactory;
import spiedie.utilities.stream.OutputStreamFactory;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.ISettings;

public class Info {
	
	public static boolean isSet(String key) throws IOException{
		return isSet(key, null, null);
	}
	
	public static boolean isSet(String key, InputStreamFactory factory, ISettings options) throws IOException{
		return getProperty(key, factory, options) != null;
	}
	
	public static void setProperty(String key, String value) throws IOException{
		setProperty(key, value, null, null);
	}
	
	public static void setProperty(String key, String value, OutputStreamFactory factory, ISettings options) throws IOException{
		DataCache.setProperty(key, value.getBytes(Stream.charset), factory, options);
	}
	
	public static String getProperty(String key) throws IOException{
		return getProperty(key, null, null);
	}
	
	public static String getProperty(String key, InputStreamFactory factory, ISettings options) throws IOException{
		byte[] buf = DataCache.getProperty(key, factory, options);
		return buf == null ? null : new String(buf, Stream.charset);
	}
	
}
