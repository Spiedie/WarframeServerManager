package spiedie.warframe.allocator.command;

import java.util.ArrayList;
import java.util.List;

import spiedie.terminal.cmdProcessor.CmdArg;
import spiedie.terminal.cmdProcessor.CmdArgs;
import spiedie.utilities.data.StringUtils;
import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.Constants;
import spiedie.utilities.util.ISettings;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.allocator.WFAllocationLogin;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.allocator.WFConnector;
import spiedie.warframe.allocator.WFTrackedGameMode;
import spiedie.warframe.allocator.console.WFPermission;
import spiedie.warframe.allocator.console.WFRemoteConsoleLogin;
import spiedie.warframe.allocator.global.WFRemoteFromLocalAllocatorLogin;
import spiedie.warframe.util.WFInstance;

public class WFConfigProcessor extends WFDefaultCmdProcessor{

	public WFConfigProcessor(WFAllocationManager manager, WFPermission permissions, ITextOutput out) {
		super(manager, permissions, out);
	}

	public String description(){
		List<String> list = new ArrayList<>();
		list.add("-config -allocName MyAllocator");
		list.add("-config -timer -updateDelay 120000 (check state and start/stop instances every 2 minutes)");
		list.add("-config -log -enable (-enable or -disable)");
		list.add("-config -log -verbose true");
		list.add("-config -dx10 1");
		list.add("-config -dx11 1");
		list.add("-config -set gameModeName 7-406014-0 SpDMAltRC");
		list.add("-config -mode 7 406014 0");
		list.add("-config -mode -remove 406011");
		list.add("-config -connect -name MyServer -instances 24");
		list.add("-config -connect -remove -name OldServer");
		list.add("-config -listen -port "+WFC.PORT_ALLOCATOR_LOGIN);
		list.add("-config -listenLocal -port "+WFC.PORT_LOCAL_ALLOCATOR_LOGIN);
		list.add("-config -listenConsole -port "+WFC.PORT_REMOTE_CONSOLE);
		StringBuilder sb = new StringBuilder("Manage settings. Examples:\n");
		for(String help : list){
			sb.append("\t"+help+"\n");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	public void process(CmdArgs args, ISettings settings) {
		if(args.isSet("list")){
			println(this, manager.getMapping().toString());
		}
		if(args.isSet("allocName")){
			manager.setName(args.getValue("allocName"));
		}
		if(args.isSet("timer")){
			processTimer(args);
		}
		if(args.isSet("set")){
			processSet(args);
		}
		if(args.isSet("mode")){
			processMode(args);
		}
		if(args.isSet("connect")){
			processConnect(args);
		}
		if(args.isSet("listen")){
			processListen(args);
		}
		if(args.isSet("listenLocal")){
			processListenLocal(args);
		}
		if(args.isSet("listenConsole")){
			processListenConsole(args);
		}
		if(args.isSet("log")){
			processLog(args);
		}
		if(args.isSet("dx10")){
			processDx10(args);
		}
		if(args.isSet("dx11")){
			processDx11(args);
		}
	}
	
	public void processTimer(CmdArgs args) {
		if(args.isSet("updateDelay")){
			long delay = Long.parseLong("0"+StringUtils.keep(args.getValue("updateDelay"), StringUtils.DIGITS));
			manager.setUpdateDelay(delay);
		}
	}

	public void processSet(CmdArgs args) {
		CmdArg arg = args.getArg("set");
		if(arg.values.size() < 3){
			println(this, "Not enough arguments: "+arg);
		} else{
			String setting = arg.values.get(0);
			String key = arg.values.get(1);
			String value = arg.values.get(2);
			manager.getMapping().put(setting, key, value);
		}
	}

	public void processMode(CmdArgs args) {
		CmdArg arg = args.getArg("mode");
		if(args.isSet("remove")){
			for(int i = 0; i < manager.getGameModes().size();i++){
				if(manager.getGameModes().get(i).gameModeId.equals(args.getValue("remove"))){
					manager.getGameModes().remove(i--);
				}
			}
		} else{
			if(arg.values.size() < 3){
				println(this, "Not enough arguments.");
			} else{
				String regionId = arg.values.get(0);
				String gameModeId = arg.values.get(1);
				String eloRating = arg.values.get(2);
				WFTrackedGameMode mode = new WFTrackedGameMode(regionId, gameModeId, eloRating);
				manager.getGameModes().add(mode);
			}
		}
	}
	
	public void processConnect(CmdArgs args) {
		boolean remove = args.isSet("remove");
		if(remove){
			String ip = args.getValue("name");
			if(ip == null) println(this, "Cannot remove connector without providing the name.");
			else{
				List<WFConnector> ctrs = new ArrayList<>();
				for(WFConnector ctr : manager.getConnectors()){
					if(ctr.getName().equals(ip)){
						println(this, "Remove "+ctr);
						ctrs.add(ctr);
					}
				}
				for(WFConnector ctr : ctrs) manager.getConnectors().remove(ctr);
			}
		} else{
			String name = WFVars.eval(args.getValue("name"));
			int instances = 1;
			if(args.isSet("instances")) instances = Integer.parseInt("0"+StringUtils.keep(args.getValue("instances"), StringUtils.DIGITS));
			if(name == null) println(this, "No name defined.");
			if(name != null){
				WFConnector ctr = new WFConnector(name, instances);
				println(this, "Added connector "+ctr);
				manager.getConnectors().add(ctr);
			}
		}
	}

	public void processListen(CmdArgs args) {
		int port = WFC.PORT_ALLOCATOR_LOGIN;
		if(args.isSet("port")) port = StringUtils.parseInt(args.getValue("port"));
		WFAllocationLogin login = new WFAllocationLogin(manager, port);
		login.start();
		manager.setLogin(login);
	}
	
	public void processListenLocal(CmdArgs args) {
		int port = WFC.PORT_LOCAL_ALLOCATOR_LOGIN;
		if(args.isSet("port")) port = StringUtils.parseInt(args.getValue("port"));
		WFRemoteFromLocalAllocatorLogin login = new WFRemoteFromLocalAllocatorLogin(manager, port);
		login.start();
		manager.setLocalLogin(login);
	}
	
	public void processListenConsole(CmdArgs args) {
		int port = WFC.PORT_REMOTE_CONSOLE;
		if(args.isSet("port")) port = StringUtils.parseInt(args.getValue("port"));
		WFRemoteConsoleLogin login = new WFRemoteConsoleLogin(manager, port);
		login.start();
		manager.setConsoleLogin(login);
	}

	public void processLog(CmdArgs args) {
		if(args.isSet("enable")) WFC.logPrintEnabled = true;
		if(args.isSet("disable")) WFC.logPrintEnabled = false;
		if(args.isSet("compactness")){
			String comp = args.getValue("compactness");
			int c = Integer.parseInt("0"+StringUtils.keep(comp, StringUtils.DIGITS));
			Log.compactness = c;
		}
		if(args.isSet("verbose")){
			Log.write(this, "Enabling verbose "+args.getValue("verbose"));
			WFC.logVerbose = args.getValue("verbose").equals(Constants.KEY_TRUE);
		}
	}
	
	public void processDx10(CmdArgs args) {
		String value = args.getValue("dx10");
		if(value != null) {
			boolean val = false;
			if(value.equals("1") || value.equalsIgnoreCase("true")) val = true;
			WFInstance.DX_10 = val;
		}
	}
	
	public void processDx11(CmdArgs args) {
		String value = args.getValue("dx11");
		if(value != null) {
			boolean val = false;
			if(value.equals("1") || value.equalsIgnoreCase("true")) val = true;
			WFInstance.DX_11 = val;
		}
	}
}
