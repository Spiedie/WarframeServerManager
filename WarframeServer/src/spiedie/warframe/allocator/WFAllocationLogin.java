package spiedie.warframe.allocator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.util.WFGenericLogin;

public class WFAllocationLogin extends WFGenericLogin{
	private WFAllocationManager manager;
	public WFAllocationLogin(WFAllocationManager manager, int port){
		super(port);
		this.manager = manager;
	}
	
	public void handle(Socket s) throws IOException{
		WFConnection con = new WFConnection();
		InputStream in = s.getInputStream();
		OutputStream out = s.getOutputStream();
		boolean added = false;
		if(!con.init(in, out)){
			Log.err(this, "Init failed.");
		} else{
			String name = con.getServerData().getProperty(WFC.KEY_INIT_NAME);
			if(name == null){
				Log.err(this, "Init failed to send name.");
			} else{
				name = name.toLowerCase();
				
				for(WFConnector ctr : manager.getConnectors()){
					if(!added){
						Log.write(this, "Check ctr "+ctr.getName());
						if(ctr.getName().toLowerCase().equals(name)){
							con.setConnector(ctr);
							manager.getConnections().add(con);
							added = true;
						}
					}
				}
				if(!added){
					Log.write(this, "No ctr for "+name);
				}
			}
		}
		if(!added){
			Stream.close(out);
			Stream.close(in);
			Stream.close(s);
		}
	}
}
