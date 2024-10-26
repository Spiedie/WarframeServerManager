package spiedie.warframe.allocator;

import java.lang.reflect.Field;

import spiedie.utilities.util.log.Log;
import spiedie.warframe.util.WFGameMode;

public class WFAction implements Comparable<WFAction>{
	public static final int ACTION_KILL_EMPTY_SERVER = 1;
	public static final int ACTION_START_NEW = 2;
	public static final int ACTION_PATCH = 3;
	public WFGameMode gameMode;
	public WFConnection connection;
	public int action;
	public boolean completed;
	
	public WFAction(){
		completed = false;
	}
	
	public WFAction(WFGameMode mode, int action){
		this();
		this.gameMode = mode;
		this.action = action;
	}
	
	public WFAction(WFConnection connection, int action){
		this();
		this.connection = connection;
		this.action = action;
	}
	
	public String toString(){
		return gameMode+":"+getAction(action);
	}
	
	private static final String getAction(int action){
		for(Field f : WFAction.class.getDeclaredFields()){
			try {
				if(f.getInt(null) == action) return f.getName();
			} catch (IllegalArgumentException e) {
				Log.write(WFAction.class, e.toString(), true, false, true);
			} catch (IllegalAccessException e) {
				Log.write(WFAction.class, e.toString(), true, false, true);
			}
		}
		return String.valueOf(action);
	}

	public int compareTo(WFAction a) {
		return action - a.action;
	}
}
