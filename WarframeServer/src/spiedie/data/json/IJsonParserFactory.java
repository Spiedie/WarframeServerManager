package spiedie.data.json;

import spiedie.utilities.data.CharIterator;

public interface IJsonParserFactory {
	public IJsonParser create(String json);
	public IJsonParser create(CharIterator it);
}
