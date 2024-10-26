package spiedie.warframe.allocator.console;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.NoSuchElementException;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonObject;
import spiedie.utilities.concurrency.AbstractThread;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;

public class WFRemoteConsoleManager extends AbstractThread{
	private WFRemoteConsole console;
	private WFRemoteOutput tout;
	private InputStream in;
	private OutputStream out;
	private boolean stopping;
	public WFRemoteConsoleManager(WFRemoteConsole console, WFRemoteOutput tout, InputStream in, OutputStream out){
		this.console = console;
		this.in = in;
		this.out = out;
		this.tout = tout;
	}
	
	public void run() {
		Log.write(this, "Start Reading...", false, WFC.logPrintEnabled, true);
		while(!stopping && isRunning()){
			try {
				String json = Stream.readString(in);
				IJson j = Json.parse(json);
				if(j != null) addNewMessage(j.toJsonObject());
				Log.write(this, "Send response  "+j.toJson(), false, WFC.logPrintEnabled, true);
				Json.write(out, j, true);
				Log.write(this, "Response sent.", false, WFC.logPrintEnabled, true);
			} catch (IOException e) {
				if(!stopping){
					if(e.getMessage().toLowerCase().contains("connection reset")){
						Log.write(this, "Connection reset.", true, true, true);
						setFinished(true);
					} else if(e.getMessage().toLowerCase().contains("socket closed")){
						Log.write(this, "Socket closed.", true, true, true);
						setFinished(true);
					} else if(e.getMessage().toLowerCase().contains("could not read required data")){
						Log.write(this, "Data connection closed.", false, false, true);
						setFinished(true);
					} else{
						Log.caught(this, e);
					}
				}
			} catch(NoSuchElementException e){
				Log.caught(this, e);
			}
		}
		setFinished(true);
		Log.write(this, "End Reading.", false, WFC.logPrintEnabled, true);
	}
	
	public void addNewMessage(IJsonObject o) throws IOException{
		if(o != null && o.isSet(WFC.KEY_CMD_ARGS)){
			String args = o.getProperty(WFC.KEY_CMD_ARGS);
			Log.write(this, "Process "+args, false, WFC.logPrintEnabled, true);
			tout.setText("");
			try{
				console.process(args, o);
			} catch(Exception e){
				Log.caught(this, e);
				tout.setText("500");
			}
			o.setProperty(WFC.KEY_CMD_DATA, tout.getText());
			if(args.trim().equals("help")) o.setProperty(WFC.KEY_PERMISSION, WFC.VAL_PERMISSION_GRANTED);
			if(!o.isSet(WFC.KEY_PERMISSION)){
				Log.err(this, "No permission set: "+o.toJson());
			}
		}
	}

}
