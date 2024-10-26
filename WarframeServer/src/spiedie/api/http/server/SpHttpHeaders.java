package spiedie.api.http.server;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import spiedie.api.http.api.ISpHttpHeaders;

public class SpHttpHeaders implements ISpHttpHeaders{
	private Map<String, String> map;
	
	public SpHttpHeaders() {
		map = new ConcurrentHashMap<>();
	}
	
	public void put(String key, String value) {
		map.put(normalizeHeader(key), value);
	}

	public String get(String key) {
		return map.get(normalizeHeader(key));
	}

	public Map<String, String> toMap() {
		return map;
	}

	public Set<String> keySet() {
		return map.keySet();
	}
	
	public String toString() {
		return toMap().toString();
	}
	
	private static String normalizeHeader(String name) {
		boolean nextUpper = true;
		boolean copy = false;
		StringBuilder sb = null;
		for(int i = 0; i < name.length();i++) {
			char c = name.charAt(i);
			if((nextUpper && !Character.isUpperCase(c)) || (!nextUpper && Character.isUpperCase(c))) {
				copy = true;
			}
			if(copy && sb == null) {
				sb = new StringBuilder(name.substring(0, i));
			}
			if(copy) {
				if(nextUpper) c = Character.toUpperCase(c);
				else c = Character.toLowerCase(c);
				sb.append(c);
			}
			nextUpper = false;
			if(c == '-') nextUpper = true;
		}
		if(copy) return sb.toString();
		return name;
	}
}
