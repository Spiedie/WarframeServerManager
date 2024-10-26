package spiedie.warframe.allocator.command;

import java.util.List;

import spiedie.terminal.cmdProcessor.CmdArgs;
import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.Constants;
import spiedie.utilities.util.ISettings;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.allocator.WFTrackedGameMode;
import spiedie.warframe.allocator.console.WFPermission;

public class WFModeProcessor extends WFDefaultCmdProcessor{

	public WFModeProcessor(WFAllocationManager manager, WFPermission permissions, ITextOutput out) {
		super(manager, permissions, out);
	}

	public String description(){
		return "Enable or disable modes. Example -mode -enable 7-406012-0";
	}

	public void process(CmdArgs args, ISettings settings) {
		boolean permission = permissions.hasPermission("mode", settings);
		if(!permission){
			settings.setProperty(WFC.KEY_PERMISSION, WFC.VAL_PERMISSION_DENIED);
			println(this, "Permission denied.");
		} else{
			settings.setProperty(WFC.KEY_PERMISSION, WFC.VAL_PERMISSION_GRANTED);
			boolean enable = true;
			String key = null;
			if(args.isSet("enable")){
				key = "enable";
				enable = true;
			} else if(args.isSet("disable")){
				key = "disable";
				enable = false;
			}
			if(key != null && args.isSet(key)){
				List<String> values = args.getArg(key).values;
				for(WFTrackedGameMode mode : manager.getGameModes()){
					int matches = 0;
					for(String value : values){
						if(mode.regionId.equals(value)){
							matches++;
						}
						if(mode.gameModeId.equals(value)){
							matches++;
						}
						if(mode.eloRating.equals(value)){
							matches++;
						}
					}
					if(matches == values.size()){
						mode.setEnabled(enable);
						if(args.isSet("sendStats")){
							mode.setSendStats(args.getValue("sendStats").equals(Constants.KEY_TRUE));
						}
						if(args.isSet("trigger")){
							mode.setTrigger(manager.getWorldState(), args.getValue("trigger"));
						}
						Log.write(this, "Set mode "+mode+" to "+(enable ? "Enabled" : "Disabled")+(args.isSet("trigger") ? " using trigger "+args.getValue("trigger") : ""));
					}
				}
			}
		}
	}
}
