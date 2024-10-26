package spiedie.data.json.parser;

import spiedie.data.json.IJsonParser;
import spiedie.data.json.IJsonParserFactory;
import spiedie.utilities.data.CharIterator;

public class JsonParserFactory implements IJsonParserFactory{

	public IJsonParser create(String json) {
		return create(new CharIterator(json));
	}

	public IJsonParser create(CharIterator it) {
		return new JsonParser(it);
	}
	
}
