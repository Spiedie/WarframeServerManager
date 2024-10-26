package spiedie.warframe.allocator.command;

import spiedie.terminal.cmdProcessor.CmdArgs;
import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.ISettings;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.allocator.WFConnection;
import spiedie.warframe.allocator.console.WFPermission;

public class WFTestProcessor extends WFDefaultCmdProcessor{

	public WFTestProcessor(WFAllocationManager manager, WFPermission permissions, ITextOutput out) {
		super(manager, permissions, out);
	}

	public String description(){
		return "Tests";
	}

	public void process(CmdArgs args, ISettings settings) {
		if(args.isSet("ping")){
			for(WFConnection con : manager.getConnections()){
				long ping = con.getPing();
				println(this, con.getConnector().getName()+": "+ping+" ms");
			}
		}
		if(args.isSet("echo")){
			for(String arg : args.getArg("echo").values){
				println(this, "ECHO: "+arg);
			}
		}
	}
}
