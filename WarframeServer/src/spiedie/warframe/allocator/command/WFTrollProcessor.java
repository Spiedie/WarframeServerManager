package spiedie.warframe.allocator.command;

import spiedie.terminal.cmdProcessor.CmdArgs;
import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.ISettings;
import spiedie.warframe.WFC;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.allocator.console.WFPermission;

public class WFTrollProcessor extends WFDefaultCmdProcessor{

	public WFTrollProcessor(WFAllocationManager manager, WFPermission permissions, ITextOutput out) {
		super(manager, permissions, out);
	}

	public String description(){
		return "system.console.internal.test";
	}

	public void process(CmdArgs args, ISettings settings) {
		settings.setProperty(WFC.KEY_PERMISSION, WFC.VAL_PERMISSION_GRANTED);
		if(args.isSet("terminate")){
			println(this, "No.");
		}
		if(args.isSet("execute")){
			println(this, "DID YOU REALLY BELIEVE, IT WOULD BE THIS EASY??");
		}
	}
}
