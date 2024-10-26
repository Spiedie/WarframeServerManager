package spiedie.warframe.allocator;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonObject;
import spiedie.warframe.util.WFRunningInstance;

public class WFRemoteInstance {
	public WFRunningInstance instance;
	public WFConnection connection;
	public boolean running, newBuild;
	
	public WFRemoteInstance(WFConnection con, int id){
		this.connection = con;
		this.instance = new WFRunningInstance();
		this.instance.id = id;
		this.running = false;
	}
	
	public WFRemoteInstance(WFConnection con, WFRunningInstance instance){
		this.connection = con;
		this.instance = instance;
		this.running = true;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		if(connection != null) sb.append(connection.getConnector().getName()+" ");
		sb.append(running ? instance : instance.id);
		return sb.toString();
	}
	
	public IJson toJson(){
		IJsonObject o = Json.object();
		o.putJson("connection", connection.toJson());
		o.putJson("instance", instance.toJson());
		o.setProperty("running", String.valueOf(running));
		o.setProperty("newBuild", String.valueOf(newBuild));
		return o;
	}
	
	public static WFRemoteInstance parse(IJson j){
		IJsonObject o = j.toJsonObject();
		WFConnection con = WFConnection.parse(o.getJson("connection").toJsonObject());
		WFRunningInstance instance = WFRunningInstance.parse(o.getJson("instance").toJsonObject());
		WFRemoteInstance remoteInstance = new WFRemoteInstance(con, instance);
		remoteInstance.running = Boolean.parseBoolean(o.getProperty("running"));
		remoteInstance.newBuild = Boolean.parseBoolean(o.getProperty("newBuild"));
		return remoteInstance;
	}
}
