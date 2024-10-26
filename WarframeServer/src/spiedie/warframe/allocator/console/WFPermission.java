package spiedie.warframe.allocator.console;

import spiedie.utilities.util.ISettings;

public interface WFPermission {
	public boolean hasPermission(String action, ISettings settings);
}
