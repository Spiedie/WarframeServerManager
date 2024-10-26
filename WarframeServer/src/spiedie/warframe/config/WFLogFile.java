package spiedie.warframe.config;

import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;

import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.log.Log;

public class WFLogFile {
	public static final String NEW_LOG_SAVE_NAME = "newLogs";
	public static final String LOG_CONTAINS_MARK = "dedicated";
	public static final String LOG_END_MARK = ".log";
	
	/**
	 * 
	 * @param save
	 * @param userName
	 * @param f
	 * @return
	 */
	private static String getPath(String save, String userName, File f){
		save = WFServerConfigurator.toPath(save);
		String path = f.getName()+Math.random()+".log";
		if(path.length() > 200) path = userName+"-"+Math.random()+".log";
		if(!path.startsWith(userName)) path = userName+"-"+path;
		return save+NEW_LOG_SAVE_NAME+File.separator+path;
	}
	
	/**
	 * 
	 * @param msg
	 */
	public static void clearLogs(JLabel msg){
		String localAppData = new File(WFConfigFile.getCfgLocation()).getParent();
		File[] files = new File(localAppData).listFiles();
		for(File f : files){
			if(f.isFile() && f.getName().toLowerCase().contains(LOG_CONTAINS_MARK) && f.getName().toLowerCase().contains(LOG_END_MARK)){
				Log.write(WFServerConfigurator.class, "Delete "+f);
				f.delete();
			}
		}
		if(msg != null) msg.setText("Logs cleared.");
	}
	
	/**
	 * 
	 * @param server
	 * @param includeNewlogs
	 * @param msg
	 * @throws IOException
	 */
	public static void saveLogs(String server, boolean includeNewlogs, JLabel msg) throws IOException{
		String home = System.getProperty("user.home", "");
		String localAppData = home+"\\AppData\\Local\\Warframe";
		String name = new File(home).getName();
		try{
			copyLogs(localAppData, server, name, false);
			if(includeNewlogs) copyLogs(localAppData+File.separator+NEW_LOG_SAVE_NAME, server, name, true);
		} catch(IOException e){
			Log.caught(WFLogFile.class, e);
			if(msg != null) msg.setText("Error saving logs: "+e);
		}
	}
	
	/**
	 * 
	 * @param src
	 * @param save
	 * @param name
	 * @param delete
	 * @throws IOException
	 */
	private static void copyLogs(String src, String save, String name, boolean delete) throws IOException{
		Log.write(WFLogFile.class, "Add files from "+src+" to "+save);
		File[] files = new File(src).listFiles();
		if(files != null) for(File f : files){
			if(f.isFile() && f.getName().toLowerCase().contains(LOG_CONTAINS_MARK) && f.getName().toLowerCase().endsWith(LOG_END_MARK)){
				String path = null;
				while(new File((path = getPath(save, name, f))).exists());// get unique path
				Log.write(WFLogFile.class, "Copy "+f.getName()+" to "+path);
				Stream.copyFile(f.getAbsolutePath(), path, false);
				if(delete) f.delete();
			}
		}
	}
}
