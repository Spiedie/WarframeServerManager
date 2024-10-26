package spiedie.warframe.allocator.command;

import spiedie.terminal.cmdProcessor.CmdArgs;
import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.ISettings;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.allocator.console.WFPermission;
import spiedie.warframe.server.WFServerLogin;

public class WFServerLoginProcessor extends WFDefaultCmdProcessor{

	public WFServerLoginProcessor(WFAllocationManager manager, WFPermission permissions, ITextOutput out) {
		super(manager, permissions, out);
	}

	public String description(){
		return "Start a ServerLogin instance";
	}

	public void process(CmdArgs args, ISettings settings) {
		WFServerLogin l = new WFServerLogin();
		l.start();
	}
}
