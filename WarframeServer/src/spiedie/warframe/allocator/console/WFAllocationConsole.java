package spiedie.warframe.allocator.console;

import java.io.IOException;

import spiedie.terminal.cmdProcessor.CmdArgs;
import spiedie.terminal.cmdProcessor.ConsoleHandler;
import spiedie.terminal.cmdProcessor.DefaultSettingsFactory;
import spiedie.terminal.cmdProcessor.HeadlessOutput;
import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.ISettings;
import spiedie.utilities.util.log.Log;
import spiedie.utilities.util.persistentInfo.Info;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.allocator.command.WFConfigProcessor;
import spiedie.warframe.allocator.command.WFEncryptedCommandProcessor;
import spiedie.warframe.allocator.command.WFModeProcessor;
import spiedie.warframe.allocator.command.WFRemoteProcessor;
import spiedie.warframe.allocator.command.WFScriptProcessor;
import spiedie.warframe.allocator.command.WFServerLoginProcessor;
import spiedie.warframe.allocator.command.WFShutdownProcessor;
import spiedie.warframe.allocator.command.WFStateProcessor;
import spiedie.warframe.allocator.command.WFStatusProcessor;
import spiedie.warframe.allocator.command.WFSystemsProcessor;
import spiedie.warframe.allocator.command.WFTestProcessor;
import spiedie.warframe.allocator.command.WFTrollProcessor;
import spiedie.warframe.allocator.command.WFUpdateProcessor;
import spiedie.warframe.allocator.command.WFVarProcessor;

public class WFAllocationConsole extends WFConsoleBase{
	public static final String VERSION = "0.1.0";
	private static boolean exists;
	public WFAllocationConsole(WFAllocationManager manager, ITextOutput out) {
		super(manager, WFPermissionAllow.getInstance(), out);
		if(exists) throw new AssertionError();
		exists = true;
		add(new WFConfigProcessor(manager, permissions, out), "-config");
		add(new WFSystemsProcessor(manager, permissions, out), "-systems");
		add(new WFStatusProcessor(manager, permissions, out), "-status");
		add(new WFScriptProcessor(manager, permissions, out), "-script");
		add(new WFUpdateProcessor(manager, permissions, out), "-update");
		add(new WFModeProcessor(manager, permissions, out), "-mode");
		add(new WFServerLoginProcessor(manager, permissions, out), "-serverLogin");
		add(new WFVarProcessor(manager, permissions, out), "-var");
		add(new WFRemoteProcessor(manager, permissions, out, new WFRemoteOutput()), "-remote");
		add(new WFStateProcessor(manager, permissions, out), "-state");
		add(new WFTrollProcessor(manager, permissions, out), "-troll");
		add(new WFTestProcessor(manager, permissions, out), "-test");
		add(new WFEncryptedCommandProcessor(manager, permissions, out), "-enc");
		add(new WFShutdownProcessor(manager, permissions, out), "-shutdown");
	}
	
	public static void main(String[] args){
		Log.compactness = Log.RAW;
		if(System.console() == null){
			Log.err("No console");
			boolean exit = true;
			try {
				exit = !Info.isSet("ignoreConsoleMissing");
			} catch (IOException e) {}
			if(exit) System.exit(0);
		}
		HeadlessOutput out = new HeadlessOutput();
		WFAllocationConsole console = new WFAllocationConsole(null, out);
		console.println(console, "WF Alloc Console version "+VERSION);
		CmdArgs a = new CmdArgs().process(args);
		if(a.iterator().hasNext()) console.process(a.toCmd());
		console.process("help");
		new ConsoleHandler<ISettings>(console).setFactory(new DefaultSettingsFactory()).start();
	}
}
