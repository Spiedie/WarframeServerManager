package spiedie.warframe.allocator.local;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.util.WFAutoReconnector;

public class WFLocalToRemoteAllocatorConnector extends WFAutoReconnector{
	private WFLocalToRemoteAllocatorHandler handler;
	private String name;
	private WFAllocationManager manager;
	private boolean enableRemoteControl;
	public WFLocalToRemoteAllocatorConnector(WFAllocationManager manager, String ip, int port, String name, boolean enableRemoteControl){
		this.manager = manager;
		setIp(ip);
		setPort(port);
		maxBackoff = 32000;
		this.name = name;
		this.enableRemoteControl = enableRemoteControl;
		Log.write(this, "WFLocalToRemoteAllocatorConnector created with remote="+enableRemoteControl);
	}
	
	public WFLocalToRemoteAllocatorHandler getHandler(){
		return handler;
	}
	
	protected String getAttemptConnectMessage(String ip, int port){
		return "Attempt connect to remote allocator at "+ip+":"+port;
	}
	
	public void write(Object msg) {
		Log.write(this, msg, false, WFC.logPrintEnabled, true);
	}

	public WFLocalToRemoteAllocatorHandler connect(Socket s) throws IOException {
		if(handler != null){
			Log.write(this, "Kill old remote allocator handler");
			handler.setFinished(true);
		}
		InputStream in = s.getInputStream();
		OutputStream out = s.getOutputStream();
		WFLocalToRemoteAllocatorHandler w = new WFLocalToRemoteAllocatorHandler(manager, name, in, out, enableRemoteControl);
		w.start();
		handler = w;
		return w;
	}
}
