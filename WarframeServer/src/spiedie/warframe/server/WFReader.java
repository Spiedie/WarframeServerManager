package spiedie.warframe.server;

import java.io.InputStream;

import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonObject;
import spiedie.warframe.util.WFGenericReader;

public class WFReader extends WFGenericReader<IJsonObject>{
	protected boolean stopping = false;
	public WFReader(InputStream in){
		super(in);
	}
	
	public void handleMessage(IJson j){
		if(q != null && j != null){
			IJsonObject o = j.toJsonObject();
			if(o != null) q.add(o);
		}
	}
}
