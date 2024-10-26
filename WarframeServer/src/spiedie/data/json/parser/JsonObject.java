package spiedie.data.json.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import spiedie.data.json.Json;
import spiedie.data.json.JsonParseUtils;
import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonObject;
import spiedie.data.json.data.IJsonValue;
import spiedie.utilities.util.log.Log;

public class JsonObject extends Json implements IJsonObject{
	public static boolean TO_JSON_WHITESPACE = false;
	public static boolean MAP_ORDERED = false;
	private Map<String, IJson> map;

	public JsonObject() {
		if(MAP_ORDERED) {
			map = new LinkedHashMap<>();
		} else {
			map = new ConcurrentHashMap<>();
		}
	}
	
	public int getType() {
		return Json.OBJECT;
	}

	public boolean isSet(String key) {
		return map.containsKey(key);
	}

	public void setProperty(String key, String value) {
		Objects.requireNonNull(key);
		putJson(key, new JsonValue(value, false));
	}

	public String getProperty(String key) {
		IJson res = map.get(key);
		IJsonValue v = res == null ? null : res.toJsonValue();
		return v == null ? null : v.getValue();
	}

	public Map<String, String> getProperties() {
		Map<String, String> m = new HashMap<String, String>();
		for(String s : keySetJson()){
			m.put(s, getProperty(s));
		}
		return m;
	}

	public void putJson(String key, IJson val) {
		map.put(key, val);
	}

	public IJson getJson(String key) {
		return map.get(key);
	}

	public IJson removeJson(String key) {
		return map.remove(key);
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public void clear() {
		map.clear();
	}

	public Set<String> keySetJson() {
		return map.keySet();
	}

	public Collection<IJson> values() {
		return map.values();
	}

	public void putAll(IJsonObject o) {
		for(String key : keySetJson()) {
			putJson(key, o.getJson(key));
		}
	}
	
	public String toJson(){
		StringBuilder sb = new StringBuilder();
		sb.append(JsonParseUtils.CURLY_OPEN);
		if(TO_JSON_WHITESPACE) sb.append("\n");
		List<String> list = new ArrayList<String>(map.keySet());
		for(int i = 0; i < list.size();i++){
			String s = list.get(i);
			try {
				sb.append("\""+s+"\":"+map.get(s).toJson());
			} catch(NullPointerException e) {
				Log.err(this, e.getMessage()+" "+s+" "+this.toString());
			}
			if(i != list.size() - 1) sb.append(JsonParseUtils.COMMA);
		}
		if(TO_JSON_WHITESPACE) sb.append("\n");
		sb.append(JsonParseUtils.CURLY_CLOSE);
		return sb.toString();
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		List<String> keys = new ArrayList<>(map.keySet());
		for(int i = 0; i < keys.size();i++){
			sb.append("\t\""+(keys.get(i)+"\":"+map.get(keys.get(i))).replace("\n", "\n\t"));
			if(i != keys.size() - 1) sb.append(",");
			sb.append("\n");
		}
		sb.append('}');
		return sb.toString();
	}
}
