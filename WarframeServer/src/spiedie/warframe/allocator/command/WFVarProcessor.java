package spiedie.warframe.allocator.command;

import java.util.Map;

import spiedie.terminal.cmdProcessor.CmdArg;
import spiedie.terminal.cmdProcessor.CmdArgs;
import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.ISettings;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.allocator.console.WFPermission;

public class WFVarProcessor extends WFDefaultCmdProcessor{

	public WFVarProcessor(WFAllocationManager manager, WFPermission permissions, ITextOutput out) {
		super(manager, permissions, out);
	}

	public String description(){
		return "Set variables. Example: -var -set exe C:\\Program Files\\Warframe\\Downloaded\\Public\\Warframe.x64.exe";
	}

	public void process(CmdArgs args, ISettings settings) {
		CmdArg set = args.getArg("set");
		if(set == null){
			println(this, "Missing -set argument.");
		} else if(set.values.size() < 2){
			println(this, "Missing required arguments for -set.");
		} else{
			String key = set.values.get(0);
			String val = set.values.get(1);
			WFVars.getInstance().setProperty(key, val);
		}
		if(args.isSet("list")){
			Map<String, String> map = WFVars.getInstance().getProperties();
			for(String key : map.keySet()){
				println(this, key+" = "+map.get(key));	
			}
		}
	}
}
