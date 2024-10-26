package spiedie.warframe.util;

public class WFGameMode {
	public String regionId;
	public String gameModeId;
	public String eloRating;
	public WFGameMode(String regionId, String gameModeId, String eloRating){
		this.regionId = regionId;
		this.gameModeId = gameModeId;
		this.eloRating = eloRating;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getGameModeName(){
		return regionId+"-"+gameModeId+"-"+eloRating;
	}
	
	public String toString(){
		return getGameModeName();
	}
}
