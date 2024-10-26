package spiedie.utilities.util;

public class Constants {
	public static final String KEY_TRUE = "true";
	public static final String KEY_FALSE = "false";
	
	private static ISettings settings = new MemorySettings();
	
	protected Constants(){}
	
	public static boolean isSet(String key){
		return getProperty(key) != null;
	}
	
	public static void setProperty(String key, String value) {
		settings.setProperty(key, value);
	}
	
	public static String getProperty(String key) {
		return settings.getProperty(key);
	}
	
	public static String getProperty(String key, String def) {
		return isSet(key) ? getProperty(key) : def;
	}
	
	public static boolean use(String key, String value, boolean def) {
		if(!isSet(key)) return def;
		return getProperty(key).equals(value);
	}

	public static ISettings getSettings(){
		return settings;
	}
	
}

