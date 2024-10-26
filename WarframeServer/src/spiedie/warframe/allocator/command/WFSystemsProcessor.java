package spiedie.warframe.allocator.command;

import java.util.ArrayList;
import java.util.List;

import spiedie.data.json.data.IJsonObject;
import spiedie.terminal.cmdProcessor.CmdArgs;
import spiedie.utilities.data.StringUtils;
import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.Constants;
import spiedie.utilities.util.ISettings;
import spiedie.warframe.WFC;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.allocator.WFConnection;
import spiedie.warframe.allocator.WFRemoteInstance;
import spiedie.warframe.allocator.WFServerPoolState;
import spiedie.warframe.allocator.console.WFPermission;
import spiedie.warframe.allocator.global.WFRemoteFromLocalAllocatorHandler;

public class WFSystemsProcessor extends WFDefaultCmdProcessor{

	public WFSystemsProcessor(WFAllocationManager manager, WFPermission permissions, ITextOutput out) {
		super(manager, permissions, out);
	}

	public String description(){
		return "Check performance of servers. Example: -systems";
	}

	public void process(CmdArgs args, ISettings settings) {
		StringBuilder performance = new StringBuilder();
		boolean permission = permissions.hasPermission("systems", settings);
		if(!permission){
			settings.setProperty(WFC.KEY_PERMISSION, WFC.VAL_PERMISSION_DENIED);
			performance.append("Permission denied");
		} else{
			settings.setProperty(WFC.KEY_PERMISSION, WFC.VAL_PERMISSION_GRANTED);
			settings.setProperty(WFC.KEY_REMOTE_CODEBLOCK, Constants.KEY_TRUE);
			WFServerPoolState state = manager.getCombinedState();
			List<WFConnection> connections = new ArrayList<>();
			for(WFRemoteInstance instance : state.getInstances()){
				String name = instance.connection.getConnector().getName();
				boolean present = false;
				for(WFConnection con : connections){
					if(con.getConnector().getName().equals(name)){
						present = true;
					}
				}
				if(!present){
					connections.add(instance.connection);
				}
			}
			String[][] message = new String[connections.size()][4];
			for(int i = 0; i < connections.size();i++){
				WFConnection con = connections.get(i);
				IJsonObject data = con.getServerData();
				if(data != null){
					double single = data.isSet(WFC.KEY_INIT_SINGLE) ? Double.parseDouble(data.getProperty(WFC.KEY_INIT_SINGLE)) : 0;
					double multi = data.isSet(WFC.KEY_INIT_MULTI) ? Double.parseDouble(data.getProperty(WFC.KEY_INIT_MULTI)) : 0;
					int threads = data.isSet(WFC.KEY_INIT_THREADS) ? StringUtils.parseInt(data.getProperty(WFC.KEY_INIT_THREADS)) : 1;
					String version = data.isSet(WFC.KEY_INIT_VERSION) ? data.getProperty(WFC.KEY_INIT_VERSION) : "Unknown";
					String name = data.isSet(WFC.KEY_INIT_NAME) ? data.getProperty(WFC.KEY_INIT_NAME) : "Unknown";
					String allocName = data.isSet(WFC.KEY_INIT_ALLOC_NAME) ? data.getProperty(WFC.KEY_INIT_ALLOC_NAME) : "Unknown";
					String allocVersion = "Unknown";
					for(WFRemoteFromLocalAllocatorHandler h : manager.getStateHandlers()){
						if(h.getName() != null && h.getVersion() != null && h.getName().equals(allocName)) allocVersion = h.getVersion();
					}
					message[i][0] = allocName == null ? name : allocName+" "+name;
					message[i][1] = "Login "+version;
					message[i][2] = "Allocator "+allocVersion;
					message[i][3] = manager.getMapping().format(WFC.KEY_STRINGS_SYSTEMS_MAX_ESTIMATE, "Estimated max servers: %.2f, %.2f", single, multi, threads);
				}
			}
			String header = manager.getMapping().format(WFC.KEY_STRINGS_SYSTEMS_VERSION_HEADER, "Allocator version %s", WFAllocationManager.VERSION);
			performance.append(header+"\n"+StringUtils.format(message));
		}
		println(this, performance.toString());
	}
}
