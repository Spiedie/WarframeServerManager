package spiedie.warframe.allocator.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.util.WFGenericLogin;

public class WFRemoteConsoleLogin extends WFGenericLogin{
	private WFAllocationManager manager;
	public WFRemoteConsoleLogin(WFAllocationManager manager, int port){
		super(port);
		this.manager = manager;
	}
	
	public void handle(Socket s) throws IOException{
		InputStream in = s.getInputStream();
		OutputStream out = s.getOutputStream();
		WFRemoteOutput rout = new WFRemoteOutput();
		WFRemoteConsoleManager c = new WFRemoteConsoleManager(new WFRemoteConsole(manager, rout), rout, in, out);
		c.start();
	}
}
