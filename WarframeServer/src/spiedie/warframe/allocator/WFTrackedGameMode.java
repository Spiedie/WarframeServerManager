package spiedie.warframe.allocator;

import spiedie.warframe.util.WFGameMode;

public class WFTrackedGameMode extends WFGameMode implements Comparable<WFTrackedGameMode>{
	public long value, lastUpdateTime;
	private boolean enabled, sendStats;
	private WFWorldState worldState;
	private String trigger;
	public WFTrackedGameMode(String regionId, String gameModeId, String eloRating) {
		super(regionId, gameModeId, eloRating);
		sendStats = true;
	}
	
	public void setTrigger(WFWorldState worldState, String trigger){
		this.worldState = worldState;
		this.trigger = trigger;
	}
	
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}
	
	public boolean isEnabled(){
		if(!enabled) return false;
		if(worldState == null || trigger == null) return true;
		else{
			String event = worldState.getEventMode();
			return (event != null && event.startsWith(trigger));
		}
	}
	
	public void setSendStats(boolean enabled){
		this.sendStats = enabled;
	}
	
	public boolean isSendStatsEnabled(){
		return sendStats;
	}
	
	public String toString(){
		double val = value;
		val /= 1000000;
		return super.toString()+", "+String.format("%.6f", val)+(isEnabled() ? "" : " x");
	}

	public int compareTo(WFTrackedGameMode m) {
		if(value == m.value) return 0;
		return value < m.value ? 1 : -1;
	}
}
