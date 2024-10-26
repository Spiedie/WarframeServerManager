package spiedie.warframe.allocator.command;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import spiedie.terminal.cmdProcessor.CmdArgs;
import spiedie.utilities.data.StringUtils;
import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.ISettings;
import spiedie.utilities.util.log.Log;
import spiedie.utilities.util.persistentInfo.Info;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.allocator.console.WFPermission;

public class WFScriptProcessor extends WFDefaultCmdProcessor{
	private Set<String> constants;
	public WFScriptProcessor(WFAllocationManager manager, WFPermission permissions, ITextOutput out) {
		super(manager, permissions, out);
		constants = new HashSet<>();
	}

	public String description(){
		return "Run scripts. Examples:\n"
				+ "\t-script -run path\\to\\scriptfile.txt\n"
				+ "\t-script -set option\n"
				+ "\t-script -unset option\n"
				+ "\t-script -if option -run script.txt";
	}

	public void process(CmdArgs args, ISettings settings) {
		if(args.isSet("set")){
			constants.add(args.getValue("set"));
		}
		if(args.isSet("unset")){
			constants.remove(args.getValue("unset"));
		}
		if(checkIf(args, settings) && args.isSet("run")){
			String arg = args.getValue("run");
			if(arg != null){
				File f = new File(arg);
				if(f.exists()){
					try {
						String script = Stream.readToEOF(f);
						println(this, "run script "+f);
						script = script.replace("\r", "\n");
						script = StringUtils.replace(script, "\n\n", "\n");
						for(String s : script.split("\n")){
							if(!s.isEmpty()) manager.getConsole().process(s, settings);
						}
					} catch (IOException e) {
						Log.caught(this, e);
					}
				} else {
					println(this, "File "+arg+" doesn't exists.");
				}
			}
		}
	}
	
	private boolean checkIf(CmdArgs args, ISettings settings){
		if(!args.isSet("if")) return true;
		String check = args.getValue("if");
		if(constants.contains(check)) return true;
		try {
			return Info.isSet(check);
		} catch (IOException e) {}
		return false;
	}
}
