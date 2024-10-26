package spiedie.warframe.allocator;

import spiedie.data.json.data.IJsonObject;

public class WFServerResponse {
	public IJsonObject object;
	
	public WFServerResponse(IJsonObject o){
		this.object = o;
	}
}
