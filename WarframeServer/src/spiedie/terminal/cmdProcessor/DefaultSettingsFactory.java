package spiedie.terminal.cmdProcessor;

import spiedie.utilities.util.ISettings;
import spiedie.utilities.util.MemorySettings;

public class DefaultSettingsFactory implements ISettingsFactory<ISettings>{

	public ISettings getEmpty() {
		return new MemorySettings();
	}

}
