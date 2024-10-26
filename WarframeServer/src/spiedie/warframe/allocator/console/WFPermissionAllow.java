package spiedie.warframe.allocator.console;

import spiedie.utilities.util.ISettings;

public class WFPermissionAllow implements WFPermission{
	private static WFPermissionAllow instance;
	public boolean hasPermission(String key, ISettings settings) {
		return true;
	}
	
	public static synchronized WFPermission getInstance(){
		if(instance == null) instance = new WFPermissionAllow();
		return instance;
	}
}
