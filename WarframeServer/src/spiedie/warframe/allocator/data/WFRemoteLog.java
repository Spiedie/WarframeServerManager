package spiedie.warframe.allocator.data;

import java.io.IOException;
import java.io.OutputStream;

import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.log.Log;

public class WFRemoteLog{
	private OutputStream out;
	public WFRemoteLog(OutputStream out){
		this.out = out;
	}
	
	public void addNewMessage(String msg){
		try {
			Stream.writeString(out, msg);
			out.flush();
		} catch (IOException e) {
			if(e.getMessage().toLowerCase().contains("connection reset")){
				Log.write(this, "Connection reset.", true, true, true);
			} else if(e.getMessage().toLowerCase().contains("socket closed")){
				Log.write(this, "Socket closed.", true, true, true);
			} else if(e.getMessage().toLowerCase().contains("could not read required data")){
				Log.write(this, "Data connection closed.", false, false, true);
			} else{
				Log.caught(this, e);
			}
		}
	}
}
