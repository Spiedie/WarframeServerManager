package spiedie.data.json.parser;

import spiedie.data.json.IJsonFactory;
import spiedie.data.json.JsonArray;
import spiedie.data.json.data.IJsonArray;
import spiedie.data.json.data.IJsonObject;
import spiedie.data.json.data.IJsonValue;

public class JsonFactory implements IJsonFactory{

	public IJsonObject object() {
		return new JsonObject();
	}

	public IJsonArray array() {
		return new JsonArray();
	}

	public IJsonValue value(String value) {
		return new JsonValue(value);
	}

}
