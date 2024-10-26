package spiedie.warframe.api;

import java.io.IOException;

import spiedie.api.http.data.MemoryDataHandler;
import spiedie.api.http.server.SpHttpServer;
import spiedie.terminal.cmdProcessor.CmdArgs;
import spiedie.utilities.util.log.Log;

public class WFHttpServer {
	/* 
	 * 0.1.0.0 initial version
	 * 0.1.0.1 close connection on error
	 * 0.1.0.2 add port argument option
	 */
	public static final String VERSION = "0.1.0.2";
	
	public static void main(String[] args) throws IOException {
		Log.write(WFHttpServer.class, "WFHttpServer v"+VERSION);
		CmdArgs a = new CmdArgs();
		a.process(args);
		int port = 80;
		if(a.isSet("port")) {
			port = Integer.parseInt(a.getValue("port"));
		}
		SpHttpServer server = new SpHttpServer(port);
		server.getLoaders().add(api -> api.getHandlers().put("foo", new MemoryDataHandler("bar".getBytes())));
		server.getLoaders().add(api -> api.getHandlers().put("conclave/status", new WFStatusHandler("localhost", WFApiC.PORT_REMOTE_CONSOLE, "-status")));
		server.getLoaders().add(api -> api.getHandlers().put("conclave/systems", new WFStatusHandler("localhost", WFApiC.PORT_REMOTE_CONSOLE, "-systems")));
		
		server.load();
		server.start();
	}
}
