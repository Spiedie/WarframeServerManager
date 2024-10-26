package spiedie.warframe.server;

import java.io.IOException;
import java.net.Socket;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJsonObject;
import spiedie.utilities.data.StringUtils;

import spiedie.utilities.random.XorPerf;
import spiedie.utilities.util.log.Log;
import spiedie.utilities.util.persistentInfo.Info;
import spiedie.warframe.WFC;
import spiedie.warframe.util.WFAutoReconnector;
import spiedie.warframe.util.WFUtils;

public class WFServerLogin extends WFAutoReconnector {
	private String name;
	private IJsonObject initData;
	private String initExe;
	public WFServerLogin(){
		Log.write(this, "Initializing ServerLogin...");
		setPort(WFC.PORT_ALLOCATOR_LOGIN);
		initExe = WFUtils.getWarframeExeFromRegistry();
		initData = Json.object();
		Log.write(this, "Using exe "+initExe);
		try {
			if(getPort() == WFC.PORT_ALLOCATOR_LOGIN && Info.isSet(WFC.KEY_NET_PORT)) setPort(Integer.parseInt("0"+StringUtils.keep(Info.getProperty(WFC.KEY_NET_PORT), StringUtils.DIGITS)));
			if(getIp() == null && Info.isSet(WFC.KEY_NET_IP)) setIp(Info.getProperty(WFC.KEY_NET_IP));
			if(name == null && Info.isSet(WFC.KEY_NET_NAME)) setName(Info.getProperty(WFC.KEY_NET_NAME));
		} catch (NumberFormatException e) {
			Log.caught(this, e);
		} catch (IOException e) {
			Log.caught(this, e);
		}
	}
	
	/**
	 * 
	 */
	private final void init(){
		int threads = Runtime.getRuntime().availableProcessors();
		double single = XorPerf.getPerformance(1);
		Log.write(this, String.format("Single perf: %.2f", single));
		double multi = threads == 1 ? single : XorPerf.getPerformance(threads);
		Log.write(this, String.format("Multi perf: %.2f using %d threads", multi, threads));
		initData.setProperty(WFC.KEY_CMD, WFC.VAL_CMD_INIT);
		initData.setProperty(WFC.KEY_INIT_SINGLE, String.valueOf(single));
		initData.setProperty(WFC.KEY_INIT_MULTI, String.valueOf(multi));
		initData.setProperty(WFC.KEY_INIT_THREADS, String.valueOf(threads));
		initData.setProperty(WFC.KEY_INIT_VERSION, WFServer.VERSION);
	}
	
	public void run(){
		init();
		super.run();
	}
	
	/**
	 * 
	 * @param name
	 */
	public void setName(String name){
		if(name != null){
			name = name.trim();
			this.name = name;
			initData.setProperty(WFC.KEY_INIT_NAME, name);
		}
	}
	
	/**
	 * 
	 */
	public void write(Object msg){
		Log.write(this, msg);
	}
	
	public WFServer connect(Socket s) throws IOException{
		WFServer server = new WFServer(s.getInputStream(), s.getOutputStream());
		server.setInitData(initData);
		server.setExe(initExe);
		server.start();
		return server;
	}
	
}
