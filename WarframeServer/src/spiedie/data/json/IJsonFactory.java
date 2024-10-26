package spiedie.data.json;

import spiedie.data.json.data.IJsonArray;
import spiedie.data.json.data.IJsonObject;
import spiedie.data.json.data.IJsonValue;

public interface IJsonFactory {
	public IJsonObject object();
	public IJsonArray array();
	public IJsonValue value(String value);
}
