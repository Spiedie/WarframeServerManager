package spiedie.warframe.allocator.global;

import java.io.IOException;
import java.net.Socket;

import spiedie.utilities.util.log.Log;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.util.WFGenericLogin;

public class WFRemoteFromLocalAllocatorLogin extends WFGenericLogin{
	private WFAllocationManager manager;
	public WFRemoteFromLocalAllocatorLogin(WFAllocationManager manager, int port){
		super(port);
		this.manager = manager;
	}
	
	public void handle(Socket s) throws IOException {
		WFRemoteFromLocalAllocatorHandler h = new WFRemoteFromLocalAllocatorHandler(s.getInputStream(), s.getOutputStream());
		h.start();
		Log.write(this, "Added state reader for "+s.getRemoteSocketAddress());
		manager.getStateHandlers().add(h);
	}
}
