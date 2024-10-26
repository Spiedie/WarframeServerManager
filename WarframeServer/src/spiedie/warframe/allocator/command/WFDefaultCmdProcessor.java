package spiedie.warframe.allocator.command;

import java.util.Map;

import spiedie.terminal.cmdProcessor.CmdArgs;
import spiedie.terminal.cmdProcessor.CommandProcessor;
import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.ISettings;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.allocator.console.WFPermission;

public abstract class WFDefaultCmdProcessor extends CommandProcessor<ISettings>{
	protected WFAllocationManager manager;
	protected WFPermission permissions;
	public WFDefaultCmdProcessor(WFAllocationManager manager, WFPermission permissions, ITextOutput out) {
		super(out);
		this.manager = manager;
		this.permissions = permissions;
	}
	
	public boolean process(String arg, ISettings settings) {
		if(permissions != null && permissions.hasPermission(getPermissionString(), settings)){
			process(getArgs(arg), settings);
		} else{
			println(this, "Access denied for "+arg+" by "+permissions);
		}
		return true;
	}
	
	public String getPermissionString(){
		return getClass().getName();
	}
	
	public abstract void process(CmdArgs args, ISettings settings);
	
	protected String mapMessage(String msg){
		if(msg == null) return "null";
		Map<String, String> map = manager.getMapping().getMapping(WFC.KEY_MAPPING_DISPLAY_NAME);
		if(map == null) {
			Log.err(this, "No mapping found.");
			return "null";
		}
		for(String key : map.keySet()){
			String repl = map.get(key);
			if(key != null && repl != null) {
				msg = msg.replace(key, repl);
			}
		}
		return msg;
	}
}
