package spiedie.warframe.allocator;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonObject;

public class WFConnector {
	private String name;
	private int instances;
	public WFConnector(String name, int numInstances) {
		this.name = name;
		this.instances = numInstances;
	}
	
	public String getName(){
		return name;
	}
	
	public int getInstances(){
		return instances;
	}
	
	public String toString(){
		return name;
	}
	
	public IJson toJson(){
		IJsonObject o = Json.object();
		o.setProperty("name", name == null ? "null" : name);
		o.setProperty("instances", String.valueOf(instances));
		return o;
	}
	
	public static WFConnector parse(IJson j){
		IJsonObject o = j.toJsonObject();
		String name = o.getProperty("name");
		int numInstances = Integer.parseInt(o.getProperty("instances"));
		WFConnector ctr = new WFConnector(name, numInstances);
		return ctr;
	}
}
