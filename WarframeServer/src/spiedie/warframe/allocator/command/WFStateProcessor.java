package spiedie.warframe.allocator.command;

import spiedie.terminal.cmdProcessor.CmdArgs;
import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.ISettings;
import spiedie.warframe.WFC;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.allocator.WFServerPoolState;
import spiedie.warframe.allocator.console.WFPermission;

public class WFStateProcessor extends WFDefaultCmdProcessor{

	public WFStateProcessor(WFAllocationManager manager, WFPermission permissions, ITextOutput out) {
		super(manager, permissions, out);
	}

	public String description(){
		return "Get state of instances. Example -state";
	}

	public void process(CmdArgs args, ISettings settings) {
		String instances = null;
		boolean permission = permissions.hasPermission("state", settings);
		if(!permission){
			settings.setProperty(WFC.KEY_PERMISSION, WFC.VAL_PERMISSION_DENIED);
			instances = "Permission denied.";
		} else{
			settings.setProperty(WFC.KEY_PERMISSION, WFC.VAL_PERMISSION_GRANTED);
			WFServerPoolState state = manager.getLastState();
			if(state == null){
				instances = "There is no state. The world has ended.";
			} else{
				String stateDescription = state.toString();
				if(stateDescription == null || stateDescription.isEmpty()){
					instances = "Waiting for connections.";
				} else{
					instances = stateDescription;
				}
			}
		}
		println(this, instances);
	}
}
