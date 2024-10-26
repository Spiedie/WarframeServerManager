package spiedie.data.json;

import spiedie.data.json.data.IJson;

public interface IJsonParser {
	public IJson parse();
	public void setUseLog(boolean log);
}
