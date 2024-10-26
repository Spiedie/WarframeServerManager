package spiedie.warframe.util;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonObject;
import spiedie.utilities.data.StringUtils;

public class WFRunningInstance {
	public static final String KEY_SETTINGS_REGION_ID = "regionId";
	public static final String KEY_SETTINGS_GAME_MODE_ID = "gameModeId";
	public static final String KEY_SETTINGS_ELO_RATING = "eloRating";
	public static final String KEY_SETTINGS_MAX_PLAYERS = "maxPlayers";
	
	public IJsonObject settings;
	public int id, players, pid;
	public boolean loading;
	
	/**
	 * 
	 * @return
	 */
	public String getRegionId(){
		return settings == null ? null : settings.getProperty(KEY_SETTINGS_REGION_ID);
	}
	/**
	 * 
	 * @return
	 */
	public String getGameModeId(){
		return settings == null ? null : settings.getProperty(KEY_SETTINGS_GAME_MODE_ID);
	}
	/**
	 * 
	 * @return
	 */
	public String getEloRating(){
		return settings == null ? null : settings.getProperty(KEY_SETTINGS_ELO_RATING);
	}
	/**
	 * 
	 * @return
	 */
	public WFGameMode getGameMode(){
		return new WFGameMode(getRegionId(), getGameModeId(), getEloRating());
	}
	
	public int getMaxPlayers(){
		if(settings == null) return 0;
		String max = settings.getProperty(KEY_SETTINGS_MAX_PLAYERS);
		return max == null ? 0 : Integer.parseInt("0"+StringUtils.keep(max, StringUtils.DIGITS));
	}
	
	public String toString(){
		return "["+getGameMode()+", "+String.valueOf(id)+", "+String.valueOf(players)+"/"+String.valueOf(getMaxPlayers())+", "+String.valueOf(pid)+"]";
	}
	
	/**
	 * Serialize
	 * @return
	 */
	public IJson toJson(){
		IJsonObject o = Json.object();
		o.putJson("settings", settings == null ? Json.object() : settings);
		o.setProperty("id", String.valueOf(id));
		o.setProperty("players", String.valueOf(players));
		return o;
	}
	
	/**
	 * Deserialize
	 * @param j
	 * @return
	 */
	public static WFRunningInstance parse(IJson j){
		IJsonObject o = j.toJsonObject();
		WFRunningInstance instance = new WFRunningInstance();
		instance.settings = o.getJson("settings").toJsonObject();
		instance.id = Integer.parseInt(o.getProperty("id"));
		instance.players = Integer.parseInt(o.getProperty("players"));
		return instance;
	}
}
