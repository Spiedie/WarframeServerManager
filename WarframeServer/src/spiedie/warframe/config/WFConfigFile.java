package spiedie.warframe.config;

import java.io.File;

public class WFConfigFile {
	
	public static final String SERVER_CONFIGS = "config";
	public static final String FILE_CFG = "DS.cfg";
	
	/**
	 * 
	 * @return
	 */
	public static String getCfgLocation(){
		return System.getProperty("user.home")+"\\AppData\\Local\\Warframe\\DS.cfg";
	}
	
	/**
	 * 
	 * @param file
	 * @param user
	 * @return
	 */
	public static String getConfigPath(String file, String user){
		return SERVER_CONFIGS+File.separator+file+"."+user;
	}
	
}
