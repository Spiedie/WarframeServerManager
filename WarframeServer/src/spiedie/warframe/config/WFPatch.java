package spiedie.warframe.config;

import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;

import spiedie.utilities.jvm.Sys;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.util.WFUtils;

public class WFPatch {
	public static final String NAME_EXE = "launcher.exe";
	public static final String LAUNCHER_LOG = "launcher.log";
	public static final String NAME_DSCFG_DUMMY = "dscfgdummy.txt";
	public static final String DSCFG_DUMMY = "\"LotusDedicatedServerSettings\" : 0\r\n";
	
	/**
	 * 
	 * @throws IOException
	 */
	private static void writeDummyConfig() throws IOException{
		Stream.writeToFile(DSCFG_DUMMY, new File(NAME_DSCFG_DUMMY));
	}
	
	/**
	 * Run the patch process.
	 * @param external
	 * @param msg
	 */
	public static void patch(boolean external, JLabel msg){
		String path = new File(WFConfigFile.getCfgLocation()).getParent()+"\\Downloaded\\Public\\Tools\\"+NAME_EXE;
		try {
			writeDummyConfig();
			String cmd = "\""+path+"\" -headless -dedicated -dscfg:"+NAME_DSCFG_DUMMY;
			String res = Sys.execute(cmd, true, true);
			Log.err(res);
		} catch (IOException e) {
			Log.caught(WFServerConfigurator.class, e);
			if(msg != null) msg.setText("Failed to start launcher: "+path);
		}
	}
	
	/**
	 * 
	 */
	public static boolean waitPatchEnded(){
		boolean checking = true;
		while(checking){
			try {
				String tasklist = Sys.execute("tasklist");
				if(!tasklist.toLowerCase().contains(NAME_EXE)) checking = false;
				Time.sleep(5000);
			} catch (IOException e) {
				Log.caught(WFPatch.class, e);
			}
		}
		String launcherLog = new File(WFConfigFile.getCfgLocation()).getParent()+"\\"+LAUNCHER_LOG;
		Log.write("Launcher log: "+launcherLog);
		boolean success = true;
		try {
			String log = Stream.readToEOF(launcherLog);
			if(log.toLowerCase().contains("restarted shortly")) {
				success = false;
			}
		} catch(Exception e) {
			success = false;
			Log.caught(WFPatch.class, e);
		}
		return success;
	}
	
	/**
	 * 
	 * @param server
	 * @param exe
	 * @param includeNewlogs
	 * @param msg
	 * @throws IOException
	 */
	public static void patchAndPrep(String server, String exe, boolean includeNewlogs, JLabel msg) throws IOException{
		int instances = WFUtils.getMaxInstances(exe);
		msg("Saving logs...", msg);
		WFLogFile.saveLogs(server, includeNewlogs, msg);
		boolean isPatched = false;
		while(!isPatched) {
			msg("Updating...", msg);
			WFPatch.patch(false, msg);
			msg("Waiting for patch...", msg);
			boolean success = WFPatch.waitPatchEnded();
			if(success) {
				isPatched = true;
			} else {
				long waitTime = 30000;
				msg("Patch failed/stalled. Waiting "+(waitTime / 1000)+"s to retry.", msg);
				Sys.execute("taskkill /im \"Launcher.exe\"");
				Time.sleep(waitTime);
			}
		}
		msg("Finalizing patch...", msg);
		Time.sleep(3000);
		msg("Preparing "+instances+" instances...", msg);
		WFUtils.prepareMultiInstance(exe, instances, msg);
	}
	
	/**
	 * 
	 * @param text
	 * @param msg
	 */
	private static void msg(String text, JLabel msg){
		if(msg != null) msg.setText(text);
		else Log.write(WFPatch.class, text);
	}
}
