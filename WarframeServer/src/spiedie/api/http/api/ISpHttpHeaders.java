package spiedie.api.http.api;

import java.util.Map;
import java.util.Set;

public interface ISpHttpHeaders {
	
	public void put(String key, String value);
	public String get(String key);
	
	public Set<String> keySet();
	
	public Map<String, String> toMap();
}
