package spiedie.warframe.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import spiedie.utilities.data.StringUtils;
import spiedie.utilities.files.FileUtils;
import spiedie.utilities.files.prep.FileCopyPrep;
import spiedie.utilities.jvm.Sys;
import spiedie.utilities.util.log.Log;
import spiedie.utilities.util.persistentInfo.Info;

public class WFUtils {
	public static String KEY_MAX_INSTANCE_PER_ENGINE = "maxInstancesPerEngine";
	
	private static int MAX_INSTANCE_PER_ENGINE_DEFAULT = 6;
	private static int numaNodeCountCache = 0;
	private static int maxInstancePerEngineCache = 0;
	
	public static int getMaxInstancesPerEngine() {
		if(maxInstancePerEngineCache == 0) {
			try {
				if(Info.isSet(KEY_MAX_INSTANCE_PER_ENGINE)) {
					maxInstancePerEngineCache = StringUtils.parseInt(Info.getProperty(KEY_MAX_INSTANCE_PER_ENGINE));
				}
			} catch(Exception e) {
				Log.caught(WFUtils.class, e);
			}
			if(maxInstancePerEngineCache == 0) {
				maxInstancePerEngineCache = MAX_INSTANCE_PER_ENGINE_DEFAULT;
			}
		}
		return Math.max(1, maxInstancePerEngineCache);
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public static int getNumaNodeCount() throws IOException{
		if(numaNodeCountCache == 0){
			int c = 1;
			while(getNodeExists(c)) c++;
			numaNodeCountCache = c;
		}
		return numaNodeCountCache;
	}
	
	/**
	 * 
	 * @param node
	 * @return true iff the given 0-based node exists.
	 */
	private static boolean getNodeExists(int node){
		try{
			String res = Sys.execute("start /node "+node+" exit", false, true);
			return res != null && res.isEmpty();
		} catch(IOException e){
			
		}
		return false;
	}
	
	/**
	 * Check the registry for the executable for the game. 
	 * @return the path to the warframe game executable.
	 */
	public static String getWarframeExeFromRegistry(){
		try {
			String reg = Sys.execute("reg query \"HKCU\\Software\\Digital Extremes\\Warframe\\Launcher\" /v DownloadDir", true, true);
			for(String line : reg.split("\n")){
				for(String token : line.split("  ")){
					if(token.contains(":\\")){
						for(File file : new File(token.trim()+"\\Public").listFiles()){
							if(file.getName().endsWith(".exe")) return file.getAbsolutePath();
						}
					}
				}
			}
		} catch (IOException e) {
			Log.caught(WFUtils.class, e);
		}
		return null;
	}
	
	/**
	 * 
	 * @return a list of ids used by running instances.
	 * @throws IOException
	 */
	public static List<Integer> getUsedIds() throws IOException{
		List<Integer> ids = new ArrayList<>();
		for(WFRunningInstance instance : getRunningInstances()){
			ids.add(instance.id);
		}
		return ids;
	}
	
	/**
	 * 
	 * @return a list of WFRunningInstance.
	 * @throws IOException
	 */
	public static List<WFRunningInstance> getRunningInstances() throws IOException{
		String tasklist = Sys.execute("tasklist /v");
		return getRunningInstances(tasklist);
	}
	
	/**
	 * Parse a list of running instances from the output of windows command "tasklist /v".
	 * @param tasklist
	 * @return
	 */
	public static List<WFRunningInstance> getRunningInstances(String tasklist){
		List<WFRunningInstance> instances = new ArrayList<>();
		tasklist = tasklist.toLowerCase();
		tasklist = tasklist.replace("\r", "");
		String[] lines = tasklist.split("\n");
		for(String line : lines){
			if(line.contains("warframe")){
				String id = StringUtils.getFromPattern(line, "warframe.*\\.exe.*id: (\\d*) ");
				String players = StringUtils.getFromPattern(line, "warframe.*\\.exe.* (\\d*) player.*");
				String pid = StringUtils.getFromPattern(line, "warframe.*\\.exe\\s*(\\d*)\\s");
				if(id != null && players != null){
					WFRunningInstance instance = new WFRunningInstance();
					instance.id = Integer.parseInt(id);
					instance.players = Integer.parseInt(players);
					instance.loading = line.contains(WFInstance.TITLE_LOADING_MESSAGE);
					if(pid != null) instance.pid = Integer.parseInt(pid);
					instances.add(instance);
				}
			}
		}
		return instances;
	}
	
	/**
	 * Checks install path. Verifies the Public download folder exists,
	 * and they contains the warframe executable and the windows cache files.
	 * @param path
	 * @return true iff the path is a valid warframe install path.
	 */
	public static boolean isValidInstallPath(String path){
		File f = new File(path);
		boolean valid = f.exists();
		File pub = new File(f.getAbsolutePath() + "\\Downloaded\\Public");
		valid = valid && pub.exists();
		String[] files = pub.list();
		valid = valid && files != null;
		boolean hasCaches = false;
		boolean hasExe = false;
		if(valid) for(String file : files){
			if(file.equalsIgnoreCase("cache.windows")) hasCaches = true;
			if(file.toLowerCase().matches("warframe.*\\.exe")) hasExe = true;
		}
		return valid && hasExe && hasCaches;
	}
	
	/**
	 * 
	 * @param exe
	 * @return true iff the path is a valid exe path.
	 */
	public static boolean isValidExePath(String exe){
		File f = new File(exe);
		boolean valid = f.exists();
		valid = valid && f.getName().toLowerCase().contains("warframe") && f.getName().toLowerCase().endsWith(".exe");
		valid = valid && f.getParentFile().getName().toLowerCase().contains("public");
		valid = valid && f.getParentFile().getParentFile().getName().toLowerCase().contains("downloaded");
		return valid;
	}
	
	/**
	 * 
	 * @param text
	 * @param msg
	 */
	private static final void sendMessage(String text, JLabel msg){
		if(msg != null) msg.setText(text);
		else Log.write(WFUtils.class, text);
	}
	
	/**
	 * 
	 * @param exe
	 * @return the install dir from the given exe.
	 */
	public static String getInstallPath(String exe){
		return new File(exe).getParentFile().getParentFile().getParent();
	}
	
	/**
	 * 
	 * @param exe
	 * @return a number indicating the number of instances the currently prepared instances can run.
	 */
	public static int getMaxInstances(String exe){
		String installPath = getInstallPath(exe);
		int engines = 1;
		while(isValidInstallPath(installPath+(engines + 1))) engines++;
		return engines * getMaxInstancesPerEngine();
	}
	
	/**
	 * Get a path to an exe file, that is part of the engine running the given instance.
	 * @param exe
	 * @param instance
	 * @return the engine exe path.
	 */
	public static String getExe(String exe, int instance){
		int engine = 1 + ((instance - 1) / getMaxInstancesPerEngine());
		String append = engine == 1 ? "" : String.valueOf(engine);
		return getInstallPath(exe)+append+"\\Downloaded\\Public\\"+new File(exe).getName();
	}
	
	/**
	 * Prepare a number of instances provided by the user.
	 * @param exe
	 * @param msg
	 * @throws IOException
	 */
	public static void prepareMultiInstance(String exe, JLabel msg) throws IOException{
		if(!isValidExePath(exe)){
			sendMessage("Invalid exe path "+exe, msg);
			return;
		}
		String input = JOptionPane.showInputDialog(null, "How many instances would you like to support? (Make sure warframe isn't running)\nPreparing as "+ (Sys.isAdmin() ? "" : "Non-")+"Admin");
		if(input == null){
			sendMessage("Prepare canceled.", msg);
			return;
		}
		int numInstances = Integer.parseInt("0"+StringUtils.keep(input, StringUtils.DIGITS));
		prepareMultiInstance(exe, numInstances, msg);
	}
	
	/**
	 * Prepare a given amount of instances. Each engine can support <tt>MAX_INSTANCE_PER_ENGINE</tt> instances.
	 * Each engine is a copy of the original.
	 * @param exe
	 * @param numInstances
	 * @param msg
	 * @throws IOException
	 */
	public static void prepareMultiInstance(String exe, int numInstances, JLabel msg) throws IOException{
		if(!isValidExePath(exe)){
			sendMessage("Invalid exe path "+exe, msg);
			return;
		}
		if(numInstances <= 0) numInstances = 1;
		int engines = 1 + ((numInstances - 1) / getMaxInstancesPerEngine());
		Log.write(WFUtils.class, numInstances+" instances,  Create "+engines+" Engines");
		if(engines > 1){
			if(!Sys.isAdmin()) createEngines(exe, engines, msg);
			else createEnginesSymbolic(exe, engines, msg);
		}
		sendMessage((engines * getMaxInstancesPerEngine())+" Instances prepared.", msg);
	}
	
	/**
	 * 
	 * @param exe
	 * @param engines
	 * @param msg
	 * @throws IOException
	 */
	private static void createEngines(String exe, int engines, JLabel msg) throws IOException{
		FileCopyPrep prep = new FileCopyPrep();
		String srcPath = getInstallPath(exe);
		Log.write(WFUtils.class, "Src: "+srcPath);
		for(int i = 2; i <= engines;i++){
			String path = srcPath+i;
			Log.write(WFUtils.class, "Target: "+path);
			prep.addDeleteFolder(path, 1000 - (10 * i) + 1);
			prep.addCopyFolder(srcPath, path, 1000 - (10 * i));
		}
		prep.start();
	}
	
	private static boolean isSharedCacheFile(File f) {
		String name = f.getName().toLowerCase();
		return
				name.contains("h.misc.") ||
				name.contains("h.font.") ||
				name.contains("b.font.")
				;
	}
	
	/**
	 * 
	 * @param exe
	 * @param engines
	 * @param msg
	 * @throws IOException
	 */
	private static void createEnginesSymbolic(String exe, int engines, JLabel msg) throws IOException{
		Log.write(WFUtils.class, "Creating linked engines");
		String srcPath = getInstallPath(exe);
		Log.write(WFUtils.class, "Src: "+srcPath);
		for(int i = 2; i <= engines;i++){
			String path = srcPath+i;
			Log.write(WFUtils.class, "Target: "+path);
			FileUtils.delete(new File(path));
			FileUtils.copyFolder(srcPath, path, new FileFilter() {
				public boolean accept(File f) {
					return !f.getParentFile().getName().toLowerCase().equals("cache.windows") || isSharedCacheFile(f);
				}
			});
			for(File linkTarget : new File(srcPath+"\\Downloaded\\Public\\Cache.Windows").listFiles()){
				if(!isSharedCacheFile(linkTarget)){
					String linkSrc = linkTarget.getParentFile().getParentFile().getParentFile().getParent()+i+"\\Downloaded\\Public\\Cache.Windows\\"+linkTarget.getName();
					Log.write(WFUtils.class, "MkLink "+linkSrc+" to "+linkTarget);
					Files.createSymbolicLink(new File(linkSrc).toPath(), linkTarget.toPath());
				}
			}
			sendMessage("Prepared "+(i * getMaxInstancesPerEngine())+" instances so far", msg);
		}
	}
}
