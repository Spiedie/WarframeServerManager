package spiedie.warframe.allocator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonArray;
import spiedie.data.json.data.IJsonObject;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.util.WFGameMode;

public class WFServerPoolState {
	private List<WFRemoteInstance> instances;
	public WFServerPoolState(List<WFRemoteInstance> instances){
		this.instances = instances;
	}
	
	public List<WFRemoteInstance> getInstances(){
		return instances;
	}
	
	public static class GameModeProperties{
		public int slots, players, capacity, servers, emptyServers;
		public List<WFRemoteInstance> instances = new ArrayList<>();
		
		public String toString(){
			return "slots = "+slots+", players = "+players+", cap = "+capacity+", servers = "+servers+", empty = "+emptyServers;
		}
	}
	
	public GameModeProperties getProperties(WFGameMode mode){
		GameModeProperties s = new GameModeProperties();
		for(WFRemoteInstance remote : getInstances()){
			if(remote.running && mode.getGameModeName().equals(remote.instance.getGameMode().getGameModeName())){
				s.instances.add(remote);
				s.players += remote.instance.players;
				s.capacity += remote.instance.getMaxPlayers();
				s.slots += remote.instance.getMaxPlayers() - remote.instance.players;
				if(remote.instance.players == 0) s.emptyServers++;
				s.servers++;
			}
		}
		return s;
	}
	
	public int getSlots(WFGameMode mode){
		return getProperties(mode).slots;
	}
	
	public int getEmptyServers(WFGameMode mode){
		return getProperties(mode).emptyServers;
	}
	
	public WFRemoteInstance find(WFGameMode mode, int players){
		for(int i = getInstances().size() - 1; i >= 0;i--){
			WFRemoteInstance remote = getInstances().get(i);
			if(remote.running && mode.getGameModeName().equals(remote.instance.getGameMode().getGameModeName())){
				if(remote.instance.players == players) return remote;
			}
		}
		return null;
	}
	
	public WFRemoteInstance getUnusedInstance(){
		List<WFRemoteInstance> instances = new ArrayList<>(getInstances());
		Collections.sort(instances, new Comparator<WFRemoteInstance>() {
			public int compare(WFRemoteInstance a, WFRemoteInstance b) {
				return a.instance.id - b.instance.id;
			}
		});
		for(WFRemoteInstance remote : instances){
			if(!remote.running) return remote;
		}
		return null;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(WFRemoteInstance remote : getInstances()){
			sb.append(remote.toString()+"\n");
		}
		return sb.toString();
	}
	
	public IJson toJson(List<WFTrackedGameMode> modes){
		IJsonArray a = Json.array();
		for(WFRemoteInstance remote : getInstances()){
			WFTrackedGameMode mode = null;
			for(WFTrackedGameMode gameMode : modes){
				try{
					if(gameMode.getGameModeName().equals(remote.instance.getGameMode().getGameModeName())){
						mode = gameMode;
					}
				} catch(Exception e){
					Log.err(this, "Error comparing "+mode+" to "+gameMode);
					Log.caught(this, e);
				}
			}
			if(mode == null || mode.isSendStatsEnabled()){
				a.add(remote.toJson());
			} else{
				// remove all data to be sent if user decides to not send data for the instance
				IJsonObject settings = remote.instance.settings;
				boolean running = remote.running;
				remote.instance.settings = null;
				remote.running = false;
				a.add(remote.toJson());
				remote.running = running;
				remote.instance.settings = settings;
			}
		}
		return a;
	}
	
	public static WFServerPoolState parse(IJson j){
		IJsonArray a = (IJsonArray)j;
		List<WFRemoteInstance> instances = new ArrayList<>();
		for(IJson instance : a){
			instances.add(WFRemoteInstance.parse(instance));
		}
		WFServerPoolState state = new WFServerPoolState(instances);
		return state;
	}
}
