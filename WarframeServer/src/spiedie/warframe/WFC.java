package spiedie.warframe;

import spiedie.utilities.util.Time;
import spiedie.warframe.config.WFLogFile;
import spiedie.warframe.config.WFServerConfigurator;
import spiedie.warframe.util.WFUtils;

public class WFC {
	public static boolean logPrintEnabled = true;
	public static boolean logVerbose = false;
	public static boolean encryptionEnabled = true;
	public static boolean blockUnencrypted = true;
	
	public static final int PORT_ALLOCATOR_LOGIN = 11024;
	public static final int PORT_LOCAL_ALLOCATOR_LOGIN = 11025;
	public static final int PORT_REMOTE_CONSOLE = 11028;
	public static final int PORT_REMOTE_LOG = 11029;
	
	public static final long VAL_ACTIVITY_TIME = Time.MINUTE * 10;
	
	public static final String KEY_NET_IP = "server/ip";
	public static final String KEY_NET_NAME = "server/name";
	public static final String KEY_NET_PORT = "server/port";
	
	public static final String VAR_USER = "%USERNAME%";
	
	public static final String CONST_WARFRAME_APPDATA = "C:\\Users\\"+VAR_USER+"\\AppData\\Local\\Warframe\\";
	public static final String CONST_WARFRAME_LOG_PREFIX = "DedicatedServer";
	public static final String CONST_WARFRAME_LOG_POSTFIX = ".log";
	
	public static final String KEY_PERMISSION = "permission";
	public static final String VAL_PERMISSION_DENIED = "denied";
	public static final String VAL_PERMISSION_GRANTED = "granted";
	
	public static final String KEY_CRYPTO_KEYS = "info";
	
	public static final String KEY_CMD = "command";
	public static final String KEY_CMD_MESSAGE = "message";
	public static final String KEY_CMD_MESSAGE_ENCRYPTED = "encryptedMessage";
	public static final String KEY_CMD_MESSAGES_LIST = "messages";
	public static final String KEY_CMD_MESSAGES_LIST_ENCRYPTED = "encryptedmessages";
	public static final String KEY_CMD_NAME = "name";
	
	public static final String KEY_CMD_PATTERN = "pattern";
	public static final String KEY_CMD_DATA = "data";
	
	public static final String KEY_CMD_LENGTH = "length";
	public static final String KEY_CMD_TIMESTAMP = "timestamp";
	
	public static final String KEY_CMD_INSTANCE = "instance";
	public static final String KEY_CMD_INSTANCE_OFFSET = "instanceOffset";
	public static final String KEY_CMD_SETTINGS = "settings";
	public static final String KEY_CMD_SERVER_POOL_STATE = "serverPoolState";
	public static final String KEY_CMD_SERVER_POOL_ACTIVITY = "serverPoolActivity";
	public static final String KEY_CMD_ENCRYPTION_ENABLED = "encrypted";
	
	public static final String KEY_CMD_REDIRECT = "redirect";
	
	public static final String KEY_REMOTE_CODEBLOCK = "codeblock";
	
	public static final String KEY_INIT_SINGLE = "singlePerf";
	public static final String KEY_INIT_MULTI = "multiPerf";
	public static final String KEY_INIT_THREADS = "threads";
	public static final String KEY_INIT_NAME = "name";
	public static final String KEY_INIT_ALLOC_NAME = "allocName";
	public static final String KEY_INIT_KEY = "key";
	public static final String KEY_INIT_VERSION = "version";
	
	public static final String KEY_CMD_ARGS = "args";
	
	public static final String VAL_CMD_FIND = "find";
	public static final String VAL_CMD_PING = "ping";
	public static final String VAL_CMD_REDIRECT = "redirect";
	public static final String VAL_CMD_EXECUTE_LOCAL = "executeLocal";
	public static final String VAL_CMD_INSTANCE_START = "instanceStart";
	public static final String VAL_CMD_INSTANCE_KILL = "instanceKill";
	public static final String VAL_CMD_TASKLIST = "tasklist";
	public static final String VAL_CMD_INIT = "init";
	public static final String VAL_CMD_TRANSFER_SERVER_STATE = "transferServerState";
	
	public static final String VAL_CMD_PATCH = "patch";
	
	public static final String VAL_NAME_FIND_SETTINGS = "findSettings";
	public static final String VAL_NAME_FIND_NEW_BUILD = "findNewBuild";
	public static final String VAL_NAME_FIND_BUILD_EXPIRED = "findBuildExpired";
	
	public static final String VAL_NAME_TASKLIST = "tasklist";
	
	public static final String KEY_MAPPING_GAME_MODE_NAME = "gameModeName";
	public static final String KEY_MAPPING_STRINGS = "strings";
	public static final String KEY_MAPPING_DISPLAY_NAME = "displayName";
	
	public static final String KEY_STRINGS_SYSTEMS_VERSION_HEADER = "systemsVersionHeader";
	public static final String KEY_STRINGS_SYSTEMS_MAX_ESTIMATE = "systemsMaxEstimate";
	
	public static final String ERROR_FILE_DOES_NOT_EXIST = "The requested file doesn't exist.";
	
	public static final String SERVER_CONFIGURATOR_VERSION_TITLE = "Warframe Config Manager v"+WFServerConfigurator.VERSION+" by Spiedie";
	public static final String SERVER_CONFIGURATOR_LIST_INSTANCE_TITLE = "Game mode listing";
	public static final String SERVER_CONFIGURATOR_LIST_PING_TITLE = "Instance ping listing";
	public static final String SERVER_CONFIGURATOR_HELP_TITLE = "Help for "+SERVER_CONFIGURATOR_VERSION_TITLE;
	
	public static final String SERVER_CONFIGURATOR_HELP_MSG = WFC.SERVER_CONFIGURATOR_HELP_TITLE+"\n"
			+ "\n"
			+ "Set config: Select options for DS.cfg\n"
			+ "\tSettings selected are saved, no need to retype motd every time.\n"
			+ "\tYou can change the settings used by moddifying the DS.cfg.dynamictemplate file on the config folder.\n"
			+ "Patch\n"
			+ "\tRuns the warframe launcher and updates to the latest version. If not running headless, warframe will start afterwards.\n"
			+ "Save Logs\n"
			+ "\tSave all logs containing \""+WFLogFile.LOG_CONTAINS_MARK+"\" and ending with \""+WFLogFile.LOG_END_MARK+"\" from %localappata%\\Warframe.\n"
			+ "\tFiles are copied to the specified Root\\"+WFLogFile.NEW_LOG_SAVE_NAME+" folder with a unique name.\n"
			+ "Clear Logs\n"
			+ "\tdelete all logs containing \""+WFLogFile.LOG_CONTAINS_MARK+"\" and ending with \""+WFLogFile.LOG_END_MARK+"\" from %localappata%\\Warframe\n"
			+ "Patch\n"
			+ "\tRuns the launcher to update warframe.\n"
			+ "Setup Instances: select the number of instances you want to be able to run.\n"
			+ "\tUnless running as Admin, this takes 35GB per "+WFUtils.getMaxInstancesPerEngine()+" instances, and the time required to copy those files.\n"
			+ "\tRequired after EVERY Patch!\n"
			+ "Start Instances: Brings you to the Instance starter in a popup.\n"
			+ "\tStarts instances up to the maximum currently prepared\n"
			+ "\tRunning instances will be skipped to avoid conflicts\n"
			+ "\t\texample: You start instances 1,2,3,4. Instance 2 crashes and you stop instance 4.\n"
			+ "\t\tYou start 3 new instances, they will be launched as instance 2, 4 and 5.\n"
			+ "\tNUMA aware :) (each instance is started on the next node. For 4 nodes, instances are started on node 1, 2, 3, 4, 1, 2, ...)\n"
			+ "\tYou can save your selected config, and load it again to make presets. Usefull for weekday/weekend/event etc. configs.\n"
			+ "List Instances: List instances currently running.\n"
			+ "\tLooks at all logfiles and lists the game mode and if they are still running.\n"
			+ "Auto\n"
			+ "\tMonitor running instances. Activates if all instances are down.\n"
			+ "\tOnce active, runs: Patch, Prepare (with the number of instances currently set up), Start instances (with given config)\n"
			+ "\n"
			+ "Note: Doesn't work with steam install, get Warframe from https://warframe.com/download\n";

}
