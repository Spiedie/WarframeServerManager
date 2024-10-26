package spiedie.utilities.util;

import java.util.Map;

public interface ISettings {
	boolean isSet(String key);
	void setProperty(String key, String value);
	String getProperty(String key);
	Map<String, String> getProperties();
}
