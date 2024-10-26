package spiedie.warframe.allocator.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import spiedie.terminal.cmdProcessor.CmdArgs;
import spiedie.utilities.data.StringUtils;
import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.Constants;
import spiedie.utilities.util.ISettings;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.allocator.WFRemoteInstance;
import spiedie.warframe.allocator.WFServerPoolState;
import spiedie.warframe.allocator.WFServerPoolState.GameModeProperties;
import spiedie.warframe.allocator.console.WFPermission;
import spiedie.warframe.util.WFGameMode;
import spiedie.warframe.util.WFNetActivity;
import spiedie.warframe.util.WFNetActivity.IntroRequest;

public class WFStatusProcessor extends WFDefaultCmdProcessor{

	public WFStatusProcessor(WFAllocationManager manager, WFPermission permissions, ITextOutput out) {
		super(manager, permissions, out);
	}

	public String description(){
		return "Get status overview. Example -status";
	}

	public void process(CmdArgs args, ISettings settings) {
		String status = null;
		boolean permission = permissions.hasPermission("status", settings);
		if(!permission){
			settings.setProperty(WFC.KEY_PERMISSION, WFC.VAL_PERMISSION_DENIED);
			status = "Permission denied.";
		} else{
			settings.setProperty(WFC.KEY_PERMISSION, WFC.VAL_PERMISSION_GRANTED);
			WFServerPoolState state = manager.getCombinedState();
			if(state == null){
				status = "There is no state. The world has ended.";
			} else{
				WFNetActivity activity = manager.getCombinedActivity();
				activity.clean(WFC.VAL_ACTIVITY_TIME);
				String stateDescription = getStatus(args, state, activity);
				if(stateDescription == null || stateDescription.isEmpty()){
					status = "Waiting for connections.";
				} else{
					status = stateDescription;
					settings.setProperty(WFC.KEY_REMOTE_CODEBLOCK, Constants.KEY_TRUE);
				}
			}
		}
		println(this, status);
	}
	
	private String getStatus(CmdArgs args, WFServerPoolState state, WFNetActivity activity){
		StringBuilder sb = new StringBuilder();
		sb.append(getGameModeStatus(state, activity));
		sb.append("\n");
		sb.append(getServerCapacity(state));
		return sb.toString();
	}
	
	private String getGameModeStatus(WFServerPoolState state, WFNetActivity activity){
		StringBuilder sb = new StringBuilder();
		List<WFGameMode> gameModes = new ArrayList<WFGameMode>();
		for(WFRemoteInstance remote : state.getInstances()){
			if(remote.running && remote.instance.settings != null){
				WFGameMode mode = remote.instance.getGameMode();
				boolean exists = false;
				for(WFGameMode m : gameModes){
					if(m.getGameModeName().equals(mode.getGameModeName())) exists = true; 
				}
				if(!exists){
					Log.write(this, "Adding game mode "+mode, false, false, true);
					gameModes.add(mode);
				}
			}
		}
		Collections.sort(gameModes, new GameModeComparator());
		List<List<String>> message = new ArrayList<>();
		int players = 0;
		for(int i = 0; i < gameModes.size();i++){
			WFGameMode mode = gameModes.get(i);
			GameModeProperties p = state.getProperties(mode);
			if(p.players > 0){
				message.add(new ArrayList<String>());
				message.get(message.size() - 1).add(mapMessage(mode.regionId+"-"));
				message.get(message.size() - 1).add(mapMessage(mode.gameModeId));
				message.get(message.size() - 1).add(mapMessage("-"+mode.eloRating));
				message.get(message.size() - 1).add(p.players+"/"+p.capacity);
				players += p.players;
			}
		}
		sb.append("Game mode activity:\n"+StringUtils.format(message)+players+" players total.\n");
		List<IntroRequest> req = activity.get();
		if(!req.isEmpty()) {
			sb.append(String.format("\nActivity (Experimental): %d players in the past %s\n", req.size(), Time.toTimeString(WFC.VAL_ACTIVITY_TIME)));
		}
		return sb.toString();
	}
	
	private String getServerCapacity(WFServerPoolState state){
		List<String> serverNames = new ArrayList<>();
		for(WFRemoteInstance remote : state.getInstances()){
			String name = remote.connection.getConnector().getName();
			if(!serverNames.contains(name)) serverNames.add(name);
		}
		Collections.sort(serverNames);
		String[][] message = new String[serverNames.size()][2];
		int totalRunning = 0;
		for(int i = 0; i < serverNames.size();i++){
			String name = serverNames.get(i);
			int running = 0;
			int instances = 0;
			boolean patching = false;
			for(WFRemoteInstance remote : state.getInstances()){
				if(remote.connection.getConnector().getName().equals(name)){
					patching = patching || remote.newBuild;
					if(remote.running) running++;
					instances++;
				}
			}
			message[i][0] = mapMessage(name);
			message[i][1] = patching ? "Patching..." :  running+"/"+instances;
			totalRunning += running;
		}
		return "Servers running/available:\n"+StringUtils.format(message)+"\nTotal running: "+totalRunning+".";
	}
	
	static class GameModeComparator implements Comparator<WFGameMode>{
		public int compare(WFGameMode a, WFGameMode b) {
			int cmp = 0;
			if(a.regionId != null && b.regionId != null) cmp += a.regionId.compareTo(b.regionId);
			cmp *= 1000;
			if(a.eloRating != null && b.eloRating != null) cmp += a.eloRating.compareTo(b.eloRating);
			cmp *= 1000;
			if(a.gameModeId != null && b.gameModeId != null) cmp += a.gameModeId.compareTo(b.gameModeId);
			cmp *= 1000;
			return cmp;
		}
	}
}
