package spiedie.warframe.allocator.console;

import spiedie.terminal.cmdProcessor.CommandProcessor;
import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.ISettings;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.allocator.WFAllocationManager;

public abstract class WFConsoleBase extends CommandProcessor<ISettings>{
	protected WFAllocationManager manager;
	protected WFPermission permissions;
	public WFConsoleBase(WFAllocationManager manager, WFPermission permissions, ITextOutput out) {
		super(out);
		this.manager = manager;
		this.permissions = permissions;
	}

	public boolean process(String arg, ISettings settings) {
		if(settings == null) throw new UnsupportedOperationException();
		if(permissions != null && permissions.hasPermission(WFC.KEY_PERMISSION, settings)){
			Log.write(this, "Process "+arg, false, false, true);
			if(!arg.trim().startsWith("//")){
				try{
					return processDefault(arg, settings);
				} catch(Exception e){
					Log.caught(this, e);
				}
			}
		} else{
			println(this, "Access denied for "+arg+" by "+permissions);
		}
		return false;
	}
}
