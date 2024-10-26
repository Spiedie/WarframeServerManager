package spiedie.warframe.allocator.command;

import java.io.IOException;
import java.util.List;

import spiedie.terminal.cmdProcessor.CmdArgs;
import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.jvm.Sys;
import spiedie.utilities.util.ISettings;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.allocator.console.WFPermission;
import spiedie.warframe.util.WFRunningInstance;
import spiedie.warframe.util.WFUtils;

public class WFShutdownProcessor extends WFDefaultCmdProcessor{

	public WFShutdownProcessor(WFAllocationManager manager, WFPermission permissions, ITextOutput out) {
		super(manager, permissions, out);
	}

	public String description(){
		return "Shutdown system and servers gracefully.";
	}

	public void process(CmdArgs args, ISettings settings) {
		boolean waiting = true;
		long initDelay = 0;
		long waitLimit = -1;
		boolean dummy = args.isSet("dummy");
		if(args.isSet("delay")) {
			initDelay = Time.getTime(args.getArg("delay").values);
			println(this, "Wait for "+Time.toTimeString(initDelay));
		}
		if(args.isSet("wait")) {
			waitLimit = Time.getTime(args.getArg("wait").values);
			println(this, "Wait until "+Time.toTimeString(waitLimit)+" passed to terminate.");
		}
		Time.sleep(initDelay);
		manager.setFinished(true);
		long start = Time.millis();
		while(waiting){
			try {
				List<WFRunningInstance> instances = WFUtils.getRunningInstances();
				StringBuilder sb = new StringBuilder("Waiting for: ");
				for(int i = 0; i < instances.size();i++){
					WFRunningInstance instance = instances.get(i);
					if(instance.players == 0 || (waitLimit >= 0 && Time.millis() - start > waitLimit)){
						if(!dummy) Sys.execute("taskkill /pid "+instance.pid);
						instances.remove(i--);
					} else {
						sb.append(instance.id+", ");
					}
				}
				sb.setLength(sb.length() - 2);
				if(waitLimit >= 0) {
					sb.append(", terminating in "+Time.toTimeString(waitLimit - (Time.millis() - start))+".");
				}
				waiting = !instances.isEmpty();
				if(waiting) println(this, sb.toString());
			} catch(Exception e){
				Log.err(this, e);
			}
			Time.sleep(10000);
		}
		if(!dummy && args.isSet("exit")){
			try {
				Sys.execute("shutdown -s -t 0");
			} catch (IOException e) {
				Log.caught(this, e);
			}
		} else {
			System.exit(0);
		}
	}
}
