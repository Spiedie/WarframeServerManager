package spiedie.data.json.data;

import java.util.List;

public interface IJsonArray extends List<IJson>, IJson{
	public boolean add(String s);
	public List<String> toStringList();
	public List<IJsonObject> toObjectList();
}
