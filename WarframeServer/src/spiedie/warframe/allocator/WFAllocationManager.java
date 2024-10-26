package spiedie.warframe.allocator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonArray;
import spiedie.data.json.data.IJsonObject;
import spiedie.terminal.cmdProcessor.ConsoleHandler;
import spiedie.terminal.cmdProcessor.DefaultSettingsFactory;
import spiedie.terminal.cmdProcessor.HeadlessOutput;
import spiedie.utilities.concurrency.AbstractThread;
import spiedie.utilities.data.StringUtils;
import spiedie.utilities.util.ISettings;
import spiedie.utilities.util.MemorySettings;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.allocator.WFServerPoolState.GameModeProperties;
import spiedie.warframe.allocator.console.WFAllocationConsole;
import spiedie.warframe.allocator.console.WFRemoteConsoleLogin;
import spiedie.warframe.allocator.data.WFRemoteLog;
import spiedie.warframe.allocator.global.WFRemoteFromLocalAllocatorHandler;
import spiedie.warframe.allocator.global.WFRemoteFromLocalAllocatorLogin;
import spiedie.warframe.allocator.local.WFLocalToRemoteAllocatorConnector;
import spiedie.warframe.allocator.local.WFLocalToRemoteWriter;
import spiedie.warframe.util.WFGameMode;
import spiedie.warframe.util.WFNetActivity;

public class WFAllocationManager extends AbstractThread{
	/*
	 * 0.1.0.0 allocator
	 * 0.1.1.0 fixed start/stop/start/stop... of empty server
	 * 0.1.2.0 adapted to WFServer 0.2.4
	 * 0.1.3.0 encryption
	 * 0.1.4.0 remove exe sending
	 * 0.1.5.0 changed to accept incoming connections
	 * 0.1.5.1 disconnect if valid data but no connector
	 * 0.1.6.0 local and remote allocator
	 * 0.1.6.1 send remote allocator info with json
	 * 0.1.6.2 fix state readers not disconnecting
	 * 0.1.6.3 fix perf not including remote allocators
	 * 0.1.6.4 auto reconnect to remote allocator
	 * 0.1.7.0 two way communication between allocators
	 * 0.1.7.1 added allocator name + allocator version
	 * 0.1.7.2 fix null name in WFRemoteFromLocalAllocatorHandler
	 * 0.1.7.3 fix null name in WFSystemsProcessor
	 * 0.1.8.0 game mode priority
	 * 0.1.8.1 kill unused game mode instances
	 * 0.1.8.2 fix killing multiple unused empty servers per update
	 * 0.1.8.3 reset mode value on start if its the first
	 * 0.1.8.4 don't kill servers for low valued ones
	 * 0.1.8.5 adjust algorithm for modes with no servers and negative value
	 * 0.1.8.6 adjust algorithm to kill empty instance for other mode without slots
	 * 0.1.8.7 enable and disable modes
	 * 0.1.8.8 fix matching NA and RU together
	 * 0.1.9.0 detect instance stuck on cache lock
	 * 0.1.9.1 check for instance stuck on cache lock
	 * 0.1.9.2 remove console logging of json responses
	 * 0.1.9.3 encrypt local to remote communication
	 * 0.1.9.4 named encryption keys
	 * 0.1.9.5 format strings based on files
	 * 0.1.10.0 linux changes
	 * 0.1.10.1 timeout handlers
	 * 0.1.11.0 added triggers to game modes
	 * 0.1.12.0 added serverLogin option
	 * 0.1.12.1 remove excessive json logging
	 * 0.1.13.0 start all instances ready to be started
	 * 0.1.13.1 fix errors on processing instances without settings
	 * 0.1.13.2 add instanceId offset
	 * 0.1.13.3 remove instanceId offset
	 * 0.1.14.0 generalize login system
	 * 0.1.14.1 fix sort of game mode instances
	 * 0.1.14.2 fix multiple patch detections on the same connection
	 * 0.1.14.3 added custom script file
	 * 0.2.0.0 remote console encrypted command processor
	 * 0.2.0.1 remote to local patching
	 * 0.2.0.2 encrypted command keys
	 * 0.2.0.3 fix encrypted command execute message
	 * 0.2.0.4 try detect instances that failed to load
	 * 0.2.0.5 try detect instances that failed to load by game mode data
	 * 0.2.0.6 ignore instances that are not running
	 * 0.2.0.7 increase delay between instance starts
	 * 0.2.1.0 check if patching succeeds
	 * 0.2.1.1 fix errors in status processor when name isn't sent
	 * 0.2.1.2 fix killing empty instances when no other instance is in need of instances
	 * 0.2.2.0 limit impact of invalid data sent to the remote login
	 * 0.2.2.1 added opt-in for remote control
	 * 0.2.2.2 fix remote message processing
	 * 0.2.2.3 added local command execution from remote
	 * 0.2.2.4 added remote command execution
	 * 0.2.2.5 added option to disable some verbose logging
	 * 0.2.2.6 added delay and max wait time to shutdown command
	 * 0.2.2.7 fixed allocator stopping while delayed for shutdown
	 * 0.2.2.8 added max instance per engine option
	 * 0.2.2.9 fix instances per engine
	 * 0.2.2.10 add options for dx10 and dx11 settings
	 * 0.2.2.11 update server component to 0.2.7.11
	 * 0.2.2.12 added more shared cache files to symlink exceptions
	 * 0.2.3.0 send introduction request activity
	 * 0.2.3.1 hash ip
	 */
	public static final String VERSION = "0.2.3.1";
	public static final String SCRIPT_INIT = "scripts/WFAllocInit.txt";
	public static final long DELAY_MIN = Time.SECOND * 10;
	public static final long DELAY_INTERVAL = Time.SECOND;
	private long updateDelay = Time.MINUTE * 2;
	private List<WFConnector> connectors;
	private List<WFConnection> connections;
	private List<WFTrackedGameMode> gameModes;
	private List<WFRemoteFromLocalAllocatorHandler> statehandlers;
	private List<WFRemoteLog> logs;
	private WFWorldState worldState;
	private WFLocalToRemoteAllocatorConnector remoteStateConnector;
	private WFRemoteFromLocalAllocatorLogin allocLogin;
	private WFAllocationLogin login;
	private WFRemoteConsoleLogin consoleLogin;
	private WFAllocationConsole console;
	private ConsoleHandler<ISettings> consoleHandler;
	private WFMappings mapping;
	private WFServerPoolState lastState;
	private String name;
	private WFNetActivity activity;
	public WFAllocationManager(){
		name = StringUtils.random(10);
		connectors = Collections.synchronizedList(new ArrayList<WFConnector>());
		connections = Collections.synchronizedList(new ArrayList<WFConnection>());
		statehandlers = Collections.synchronizedList(new ArrayList<WFRemoteFromLocalAllocatorHandler>()); 
		logs = Collections.synchronizedList(new ArrayList<WFRemoteLog>()); 
		gameModes = new ArrayList<>();
		mapping = new WFMappings();
		console = new WFAllocationConsole(this, new HeadlessOutput());
		consoleHandler = new ConsoleHandler<ISettings>(console).setFactory(new DefaultSettingsFactory());
		worldState = new WFWorldState(WFWorldState.URL_WORLD_STATE, Time.MINUTE * 10, Time.MINUTE * 3);
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public List<WFConnector> getConnectors(){
		return connectors;
	}
	
	public List<WFRemoteFromLocalAllocatorHandler> getStateHandlers(){
		return statehandlers;
	}
	
	public WFWorldState getWorldState(){
		return worldState;
	}
	
	public void setRemoteStateConnector(WFLocalToRemoteAllocatorConnector connector){
		if(this.remoteStateConnector != null){
			this.remoteStateConnector.setFinished(true);
			if(this.remoteStateConnector.getHandler() != null){
				this.remoteStateConnector.getHandler().setFinished(true);
			}
		}
		this.remoteStateConnector = connector;
	}
	
	public ConsoleHandler<ISettings> getConsoleHandler(){
		return consoleHandler;
	}
	
	public WFAllocationConsole getConsole(){
		return console;
	}
	
	public void setLogin(WFAllocationLogin login){
		if(this.login != null) this.login.close();
		this.login = login;
 	}
	
	public void setLocalLogin(WFRemoteFromLocalAllocatorLogin login){
		if(this.allocLogin != null) this.allocLogin.close();
		this.allocLogin = login;
 	}
	
	public void setConsoleLogin(WFRemoteConsoleLogin login){
		if(this.consoleLogin != null) this.consoleLogin.close();
		this.consoleLogin = login;
 	}
	
	public List<WFConnection> getConnections(){
		return connections;
	}
	
	public List<WFRemoteLog> getLogs(){
		return logs;
	}
	
	public WFMappings getMapping(){
		return mapping;
	}
	
	public List<WFTrackedGameMode> getGameModes(){
		return gameModes;
	}
	
	public WFServerPoolState getLastState(){
		return lastState;
	}
	
	public long getUpdateDelay(){
		return updateDelay;
	}
	
	public void setUpdateDelay(long delay){
		if(delay < DELAY_MIN) delay = DELAY_MIN;
		this.updateDelay = delay;
	}
	
	public WFServerPoolState getCombinedState(){
		List<WFRemoteInstance> instances = new ArrayList<>();
		if(getLastState() != null) instances.addAll(getLastState().getInstances());
		for(WFRemoteFromLocalAllocatorHandler r : getStateHandlers()){
			Log.write(this, "Add instances from state reader "+r.getName()+", "+r.isRunning()+", "+r.isTimedOut());
			WFServerPoolState localState = r.getState();
			if(r.isRunning() && !r.isTimedOut() && localState != null){
				instances.addAll(localState.getInstances());
			}
		}
		WFServerPoolState state = new WFServerPoolState(instances);
		return state;
	}
	
	public WFNetActivity getCombinedActivity(){
		WFNetActivity state = new WFNetActivity();
		if(activity != null) state.get().addAll(activity.get());
		for(WFRemoteFromLocalAllocatorHandler r : getStateHandlers()){
			Log.write(this, "Add activity from state reader "+r.getName()+", "+r.isRunning()+", "+r.isTimedOut());
			WFNetActivity localState = r.getActivity();
			if(r.isRunning() && !r.isTimedOut() && localState != null){
				state.get().addAll(localState.get());
			}
		}
		return state;
	}
	
	public void remoteLog(String msg){
		Log.write(this, msg, false, true, true);
		msg = mapping.map(msg, WFC.KEY_MAPPING_DISPLAY_NAME);
		msg = "["+Time.toDateString()+"] "+msg;
		for(WFRemoteLog log : logs){
			log.addNewMessage(msg);
		}
	}
	
	private final void init(){
		getConsoleHandler().start();
		getWorldState().start();
		getConsole().process("-script -run "+SCRIPT_INIT, new MemorySettings());
	}
	
	public void run() {
		init();
		manage();
	}
	
	public void manage(){
		while(isRunning()){
			long time = Time.millis();
			try{
				update();
			} catch(ConcurrentModificationException e){
				Log.caught(this, e);
			}
			while(Time.millis() - time < updateDelay){
				Time.sleep(DELAY_INTERVAL);
			}
		}
	}
	
	public void update(){
		Log.write(this, "Update", false, WFC.logPrintEnabled, true);
		updateConnections();
		WFServerPoolState state = getRunningInstances();
		Log.write(this, "State:\n"+state, false, WFC.logPrintEnabled, true);
		updatePriorities(state);
		if(!getConnections().isEmpty()){
			List<WFAction> actions = determineActions(state);
			executeActions(state, actions);
		}
		lastState = state;
		sendStateToGlobal(state);
		for(WFRemoteInstance remote : state.getInstances()) {
			WFGameMode mode = remote.instance.getGameMode();
			if(remote.running && (remote.instance.loading || mode.regionId == null || mode.gameModeId == null || mode.eloRating == null)) {
				Log.err(this, "Instance "+remote.instance.id+" is still loading!");
			}
		}
	}
	
	private void updateConnections(){
		Log.write(this, "updateConnections", false, WFC.logPrintEnabled, true);
		removeDeadConnections();
		removeDeadStateWriters();
	}
	
	private void removeDeadConnections(){
		Log.write(this, "Check for dead connections, checking "+getConnections().size(), false, WFC.logPrintEnabled, true);
		for(int i = 0; i < getConnections().size();i++){
			WFConnection con = getConnections().get(i);
			if(!con.isConnected()){
				Log.write(this, "Remove dead connection "+con, false, WFC.logPrintEnabled, true);
				con.close();
				getConnections().remove(i--);
			}
		}
	}
	
	private void removeDeadStateWriters(){
		Log.write(this, "Check for dead writers, checking "+getStateHandlers(), false, WFC.logPrintEnabled, true);
		for(int i = 0; i < getStateHandlers().size();i++){
			WFRemoteFromLocalAllocatorHandler handler = getStateHandlers().get(i);
			if(!handler.isRunning() || handler.isTimedOut()){
				Log.write(this, "Remove dead handler "+handler, false, WFC.logPrintEnabled, true);
				handler.setFinished(true);
				getStateHandlers().remove(i--);
			}
		}
	}
	
	protected WFServerPoolState getRunningInstances(){
		List<WFRemoteInstance> instances = new ArrayList<>();
		Log.write(this, "Get state", false, WFC.logPrintEnabled, true);
		for(WFConnection con : connections){
			con.requestInstances();
		}
		for(WFConnection con : connections){
			instances.addAll(con.tryReadInstances(5000));
		}
		return new WFServerPoolState(instances);
	}
	
	protected void updatePriorities(WFServerPoolState state){
		for(WFTrackedGameMode mode : gameModes){
			GameModeProperties properties = state.getProperties(mode);
			if(mode.lastUpdateTime != 0){
				long time = Time.millis() - mode.lastUpdateTime;
				updateModePriorities(mode, properties, time);
			}
			mode.lastUpdateTime = Time.millis();
		}
	}
	
	public void updateModePriorities(WFTrackedGameMode mode, GameModeProperties properties, long time){
		// move value closer to 0
		long off = Math.abs(mode.value) / 10;
		off = off * time / (Time.MINUTE * 5);
		if(mode.value > 0) mode.value -= off;
		else mode.value += off;
		// increase for players in servers
		mode.value += time * properties.players;
		// decrease for empty server
		mode.value -= time * properties.emptyServers;
		if(properties.servers == 0 && mode.value < 0){
			// increase by time
			mode.value += Math.min(time / 2, Math.abs(mode.value));
		}
	}
	
	protected List<WFAction> determineActions(WFServerPoolState state){
		List<WFAction> actions = new ArrayList<>();
		Set<WFConnection> patch = new HashSet<>();
		for(WFRemoteInstance instance : state.getInstances()){
			if(instance.newBuild){
				patch.add(instance.connection);
			}
		}
		for(WFConnection connection : patch){
			Log.write(this, "Patch detected on "+connection, false, WFC.logPrintEnabled, true);
			actions.add(new WFAction(connection, WFAction.ACTION_PATCH));
		}
		if(actions.isEmpty()){
			Collections.sort(gameModes);
			for(WFTrackedGameMode mode : gameModes){
				GameModeProperties properties = state.getProperties(mode);
				if(mode.isEnabled()) Log.write(this, "Check "+mode+", slots = "+properties.slots+", servers = "+properties.servers+", empty servers = "+properties.emptyServers, false, WFC.logPrintEnabled, true);
				if(determineNew(state, mode)){
					Log.write(this, mode+": Slots 0 for "+mode+", add new", false, WFC.logPrintEnabled, true);
					actions.add(new WFAction(mode, WFAction.ACTION_START_NEW));
				}
				if(determineKillInstance(state, mode)){
					Log.write(this, mode+": Kill empty server", false, WFC.logPrintEnabled, true);
					actions.add(new WFAction(mode, WFAction.ACTION_KILL_EMPTY_SERVER));
				}
			}
			WFGameMode mode = determineKillLowValueInstance(state);
			if(mode != null){
				Log.write(this, mode+": Kill empty low value server", false, WFC.logPrintEnabled, true);
				actions.add(new WFAction(mode, WFAction.ACTION_KILL_EMPTY_SERVER));
			}
		}
		return actions;
	}
	
	private boolean determineNew(WFServerPoolState state, WFTrackedGameMode mode){
		return mode.isEnabled() && state.getSlots(mode) == 0;
	}
	
	private boolean determineKillInstance(WFServerPoolState state, WFTrackedGameMode mode){
		GameModeProperties properties = state.getProperties(mode);
		int empty = properties.emptyServers;
		if(empty > 1) return true;// kill if multiple empty servers
		if(empty == 0) return false;// no servers to kill
		if(properties.servers == 1) return false;// keep last server on
		// one empty server and one or more other servers
		GameModeProperties p = state.getProperties(mode);
		int slots = 0;
		int maxPerInstance = 0;
		for(WFRemoteInstance remote : p.instances){
			if(remote.instance.players > 0){
				int instanceLots =  remote.instance.getMaxPlayers() - remote.instance.players;
				slots += instanceLots;
				maxPerInstance = Math.max(maxPerInstance, instanceLots);
			}
		}
		// kill the empty server if there are 2 slots in a single nonempty instance, or 3 slots in nonempty instances total
		return slots >= 3 || maxPerInstance >= 2;
	}
	
	private WFGameMode determineKillLowValueInstance(WFServerPoolState state){
		boolean modeToStart = false;
		for(WFTrackedGameMode mode : getGameModes()){
			GameModeProperties properties = state.getProperties(mode);
			if(mode.isEnabled() && mode.value >= 0 && properties.slots == 0) modeToStart = true;
		}
		if(modeToStart){
			int instanceCapacity = 0;
			int instancesRunning = 0;
			for(WFConnection c : getConnections()) instanceCapacity += c.getConnector().getInstances();
			for(WFRemoteInstance instance : state.getInstances()) if(instance.running) instancesRunning++;
			for(int i = getGameModes().size() - 1; i >= 0; i--){
				WFTrackedGameMode mode = getGameModes().get(i);
				if(state.getEmptyServers(mode) == 1 && mode.value <= -1000000 && instancesRunning >= instanceCapacity){
					Log.write(this, "Kill last empty: "+mode+", "+mode.value+", "+instancesRunning+"/"+instanceCapacity, false, WFC.logPrintEnabled, true);
					return mode;// kill last empty server to allow other game modes to get players
				}
			}
		}
		return null;
	}
	
	protected void executeActions(WFServerPoolState state, List<WFAction> actions){
		Collections.sort(actions);
		for(WFAction a : actions){
			Log.write(this, "Execute "+a, false, WFC.logPrintEnabled, true);
			if(a.action == WFAction.ACTION_START_NEW){
				WFRemoteInstance remote = state.getUnusedInstance();
				if(remote != null){
					startInstance(remote.connection, a.gameMode, remote.instance.id);
					a.completed = true;
					remote.running = true;
					remote.instance.settings = Json.object();
				}
			} else if(a.action == WFAction.ACTION_KILL_EMPTY_SERVER){
				WFRemoteInstance remote = state.find(a.gameMode, 0);
				if(remote != null){
					Log.write(this, "Kill empty instance "+remote, false, WFC.logPrintEnabled, true);
					killInstance(remote);
					remote.running = false;
					remoteLog("Stop "+remote);
				}
			} else if(a.action == WFAction.ACTION_PATCH){
				IJsonObject o = Json.object();
				o.setProperty(WFC.KEY_CMD, WFC.VAL_CMD_PATCH);
				a.connection.getWriter().add(getSingleCommandAction(o));
			}
		}
	}
	
	protected void startInstance(WFConnection connection, WFGameMode mode, int instanceId){
		Log.write(this, "Starting "+mode+" on "+connection+" "+instanceId, false, WFC.logPrintEnabled, true);

		IJsonObject remoteAction = Json.object();
		remoteAction.setProperty(WFC.KEY_CMD, WFC.VAL_CMD_INSTANCE_START);
		remoteAction.setProperty(WFC.KEY_CMD_NAME, "start"+mode+" "+instanceId);
		remoteAction.setProperty(WFC.KEY_CMD_INSTANCE, String.valueOf(instanceId));
		remoteAction.setProperty(WFC.KEY_CMD_SETTINGS, String.valueOf(mapping.get(WFC.KEY_MAPPING_GAME_MODE_NAME, mode.getGameModeName())));
		
		remoteAction = getSingleCommandAction(remoteAction);
		connection.getWriter().add(remoteAction);
	}
	
	protected void killInstance(WFRemoteInstance remote){
		IJsonObject remoteAction = Json.object();
		remoteAction.setProperty(WFC.KEY_CMD, WFC.VAL_CMD_INSTANCE_KILL);
		remoteAction.setProperty(WFC.KEY_CMD_NAME, "kill"+remote.instance.id);
		remoteAction.setProperty(WFC.KEY_CMD_INSTANCE, String.valueOf(remote.instance.id));
		remoteAction = getSingleCommandAction(remoteAction);
		remote.connection.getWriter().add(remoteAction);
	}
	
	protected void sendStateToGlobal(WFServerPoolState state){
		try{
			if(remoteStateConnector != null && remoteStateConnector.getHandler() != null && remoteStateConnector.getHandler().isRunning()){
				WFLocalToRemoteWriter writer = remoteStateConnector.getHandler().getWriter();
				IJson j = state.toJson(getGameModes());
				if(activity != null) {
					activity.update();
					activity.clean(WFC.VAL_ACTIVITY_TIME);
				}
				IJson act = activity == null ? Json.array() : activity.toJson();// state.toJson(getGameModes());
				if(writer != null && j != null){
					IJsonObject o = Json.object();
					o.setProperty(WFC.KEY_CMD, WFC.VAL_CMD_TRANSFER_SERVER_STATE);
					o.putJson(WFC.KEY_CMD_SERVER_POOL_STATE, j);
					o.putJson(WFC.KEY_CMD_SERVER_POOL_ACTIVITY, act);
					writer.add(o);
				}
			}
		} catch(Throwable t){
			Log.caught(this, t);
		}
	}
	
	public static IJsonObject getSingleCommandAction(IJsonObject o){
		IJsonObject messageWrapper = Json.object();
		IJsonArray messageList = Json.array();
		messageWrapper.putJson(WFC.KEY_CMD_MESSAGES_LIST, messageList);
		messageList.add(o);
		return messageWrapper;
	}
	
	public static void main(String[] args) throws IOException{
//		TextOutput.redirectStdOutErr(0, 0, 800, 600).withExitOnclose();
		WFC.logPrintEnabled = true;
		WFAllocationManager m = new WFAllocationManager();
		m.start();
	}
}
