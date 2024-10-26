package spiedie.utilities.util;

import java.util.HashMap;
import java.util.Map;

public class MemorySettings implements ISettings{
	private Map<String, String> map;
	
	public MemorySettings(){
		this(new HashMap<String, String>());
	}
	
	public MemorySettings(Map<String, String> map){
		this.map = map;
	}
	
	public boolean isSet(String key) {
		return getProperty(key) != null;
	}

	public void setProperty(String key, String value) {
		map.put(key, value);
	}

	public String getProperty(String key) {
		return map.get(key);
	}

	public Map<String, String> getProperties() {
		return map;
	}
	
	public int hashCode(){
		int h = 0;
		for(String s : map.keySet()){
			Object o = map.get(s);
			if(o != null) h += o.hashCode();
		}
		return h;
	}
	
	public boolean equals(Object o){
		if(o instanceof ISettings){
			return this.getProperties().equals(((ISettings) o).getProperties());
		}
		return false;
	}
	
	public String toString(){
		return map.toString();
	}
}
