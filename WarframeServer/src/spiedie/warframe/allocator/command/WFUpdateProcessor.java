package spiedie.warframe.allocator.command;

import spiedie.terminal.cmdProcessor.CmdArgs;
import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.ISettings;
import spiedie.utilities.util.Time;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.allocator.console.WFPermission;

public class WFUpdateProcessor extends WFDefaultCmdProcessor{

	public WFUpdateProcessor(WFAllocationManager manager, WFPermission permissions, ITextOutput out) {
		super(manager, permissions, out);
	}

	public String description(){
		return "Update the state and perform actions on the state. Example -update";
	}

	public void process(CmdArgs args, ISettings settings) {
		long delay = manager.getUpdateDelay();
		manager.setUpdateDelay(0);
		Time.sleep(WFAllocationManager.DELAY_INTERVAL * 3);
		manager.setUpdateDelay(delay);
	}
}
