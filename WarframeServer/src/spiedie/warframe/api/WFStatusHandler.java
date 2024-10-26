package spiedie.warframe.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import spiedie.api.http.api.DefaultHandler;
import spiedie.api.http.api.ISpHttpRequest;
import spiedie.data.json.Json;
import spiedie.data.json.data.IJsonObject;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.api.WFHttpReconnector.WFHttpReconnectorThread;

public class WFStatusHandler extends DefaultHandler{
	private String args;
	private WFHttpReconnector re;
	public WFStatusHandler(String ip, int port, String args) {
		this.args = args;
		this.re = new WFHttpReconnector(ip, port);
		re.start();
	}
	
	public void handleExchange(ISpHttpRequest e) throws IOException {
		IJsonObject response = null;
		IJsonObject o = Json.object();
		try {
			response = getResponse(args);
		} catch(Throwable t) {
			Log.caught(this, t);
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			PrintWriter w = new PrintWriter(bout);
			t.printStackTrace(w);
			w.flush();
			byte[] buf = bout.toByteArray();
			o.setProperty("error", new String(buf, 0, buf.length, Stream.charset));
			re.t.setFinished(true);
		}
		if(response != null) {
			o.putJson("response", response);
		}
		byte[] buf = o.toJson().getBytes(Stream.charset);
		e.sendResponseHeaders(200, buf.length);
		e.getResponseBody().write(buf);
		e.close();
	}
	
	public IJsonObject getResponse(String message) throws IOException{
		IJsonObject o = Json.object();
		o.setProperty(WFApiC.KEY_CMD_ARGS, message);
		o.setProperty(WFApiC.KEY_CMD_NAME, "api");
		Log.write(this, "Write request...");
		WFHttpReconnectorThread t = re.t;
		Json.write(t.out, o, true);
		t.out.flush();
		Log.write(this, "Read response...");
		IJsonObject res = Json.readObject(t.in);
		return res;
	}
}
