package spiedie.warframe.allocator;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;

public class WFMappings {
	private Map<String, Map<String, String>> map;
	public WFMappings(){
		map = new HashMap<String, Map<String,String>>();
	}
	
	public String get(String setting, String key){
		if(map.containsKey(setting)){
			return map.get(setting).get(key);
		}
		return null;
	}
	
	public void put(String setting, String key, String value){
		if(!map.containsKey(setting)){
			map.put(setting, new HashMap<String, String>());
		}
		key = parseKeyConstant(key);
		map.get(setting).put(key, value);
	}
	
	private String parseKeyConstant(String key){
		try{
			Object res = key;
			for(Field f : WFC.class.getDeclaredFields()){
				if(f.getName().equals(key)){
					res = f.get(null);
				}
			}
			if(res != null) return res.toString();
		} catch(Throwable t){
			Log.err(this, t);
		}
		return key;
	}
	
	public Map<String, String> getMapping(String setting){
		if(map.containsKey(setting)){
			return map.get(setting);
		}
		return null;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(String setting : map.keySet()){
			sb.append(setting+":\n");
			for(String key : map.get(setting).keySet()){
				sb.append("\t"+key+"="+get(setting, key)+"\n");
			}
		}
		return sb.toString();
	}
	
	public String map(String msg, String mapping){
		Map<String, String> map = getMapping(mapping);
		for(String key : map.keySet()){
			String repl = map.get(key);
			msg = msg.replace(key, repl);
		}
		return msg;
	}
	
	public String format(String key, String defaultFormat, Object... args){
		String format = get(WFC.KEY_MAPPING_STRINGS, key);
		if(format == null) format = defaultFormat;
		return String.format(format, args);
	}
}
