package spiedie.warframe.util;

import java.io.IOException;

import spiedie.utilities.jvm.Sys;
import spiedie.utilities.util.log.Log;

public class WFInstance {

	public static final String TITLE_LOADING_MESSAGE = "Loading";
	public static boolean DX_10 = false;
	public static boolean DX_11 = true;
	public static boolean ENABLED = true;
	public String exe, settings;
	public int instance, numaNodeCount;
	public WFInstance(String exe, String settings, int instance, int numaNodeCount){
		if(numaNodeCount < 1) numaNodeCount = 1;
		this.exe = exe;
		this.settings = settings;
		this.instance = instance;
		this.numaNodeCount = numaNodeCount;
	}
	
	/**
	 * 
	 * @return the command to start this instance.
	 */
	public String getStartCommand(){
		int node = instance % numaNodeCount;
		int dx10 = DX_10 ? 1 : 0;
		int dx11 = DX_11 ? 1 : 0;
		String format = "start /node %d \"Windows x64 0 player(s) ID: %d %s\" \"%s\" -fullscreen:0 -dx10:%d -dx11:%d -threadedworker:1 -cluster:public -language:en -allowmultiple -log:DedicatedServer%d.log -applet:/Lotus/Types/Game/DedicatedServer /Lotus/Types/GameRules/DefaultDedicatedServerSettings -instance:%d -settings:%s";
		return String.format(format, node, instance, TITLE_LOADING_MESSAGE, exe, dx10, dx11, instance, instance, settings);
	}
	
	/**
	 * Start the instance.
	 * @throws IOException
	 */
	public void start() throws IOException{
		String cmd = getStartCommand();
		Log.write(this, cmd);
		if(ENABLED) Sys.execute(cmd);
	}
}
