package spiedie.data.json.data;

import java.util.Collection;
import java.util.Set;

import spiedie.utilities.util.ISettings;

public interface IJsonObject extends IJson, ISettings{
	public void putJson(String key, IJson val);
	public IJson getJson(String key);
	public IJson removeJson(String key);
	public int size();
	public boolean isEmpty();
	public void clear();
	public Set<String> keySetJson();
	public Collection<IJson> values();
	public void putAll(IJsonObject o);
}
