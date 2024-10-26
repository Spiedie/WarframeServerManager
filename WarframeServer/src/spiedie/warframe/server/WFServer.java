package spiedie.warframe.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.PublicKey;
import java.util.List;
import java.util.Scanner;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonObject;
import spiedie.utilities.concurrency.SimpleQueueProcessor;
import spiedie.utilities.concurrency.ThreadUtils;
import spiedie.utilities.data.StringUtils;
import spiedie.utilities.encryption.KeyEncryption;
import spiedie.utilities.jvm.Sys;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.stream.raf.RAFStream;
import spiedie.utilities.util.Constants;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.config.WFLogFile;
import spiedie.warframe.config.WFPatch;
import spiedie.warframe.util.WFConnectable;
import spiedie.warframe.util.WFInstance;
import spiedie.warframe.util.WFRunningInstance;
import spiedie.warframe.util.WFUtils;

public class WFServer extends SimpleQueueProcessor<IJsonObject> implements WFConnectable{
	/*
	 * 0.1.0 handle execute
	 * 0.1.1 send as json
	 * 0.1.2 fix not sending log when server restarted
	 * 0.1.3 fix crash when specified log doesn't exist
	 * 0.1.4 run commands in seperate threads
	 * 0.1.5 improve json performance
	 * 0.2.0 rewrite
	 * 0.2.1 include timestamp
	 * 0.2.2 added find command
	 * 0.2.3 init data, perf
	 * 0.2.4 remote execute, added startInstance, killInstance, getTasklist
	 * 0.2.5 encryption
	 * 0.2.6 get exe locally
	 * 0.2.7 removed listener, now connects to allocator
	 * 0.2.7.2 patch detection state shared between logins
	 * 0.2.7.3 fix ServerLogin name, fix threads not being terminated after connection is closed
	 * 0.2.7.4 fix killing populated servers when getting patch command
	 * 0.2.7.5 send version
	 * 0.2.7.6 named encryption keys
	 * 0.2.7.7 process start/stop sequential, delay instance starts
	 * 0.2.7.8 increase instance start delay to 500 ms
	 * 0.2.7.9 increase instance start delay to 2500 ms
	 * 0.2.7.10 increase instance start delay to 4500 ms
	 * 0.2.7.11 fix access denied copying logfile before starting instance blocking starting an instance
	 */
	public static final String VERSION = "0.2.7.11";
	public static final long START_INSTANCE_DELAY = 5000;
	private static boolean patching;
	
	private InputStream in;
	private OutputStream out;
	private WFReader reader;
	private WFWriter writer;
	private IJsonObject initData;
	private String exe;
	private byte[] symmetricKey;
	
	public WFServer(InputStream in, OutputStream out){
		this.in = in;
		this.out = out;
	}
	
	/**
	 * 
	 * @return
	 */
	public WFReader getReader(){
		return reader;
	}
	
	/**
	 * 
	 * @return
	 */
	public WFWriter getWriter(){
		return writer;
	}
	
	/**
	 * 
	 * @param initData
	 */
	public void setInitData(IJsonObject initData){
		this.initData = initData;
	}
	
	/**
	 * 
	 * @param exe
	 */
	public void setExe(String exe){
		this.exe = exe;
	}
	
	public boolean isRunning(){
		return super.isRunning() && getWriter().isRunning() && getReader().isRunning();
	}
	
	public void start(String name){
		writer = new WFWriter(out);
		reader = new WFReader(in);
		reader.setQueue(getQueue());
		init();
		writer.setKey(symmetricKey);
		writer.start();
		reader.start();
		super.start(name);
	}
	
	protected void process(IJsonObject o) {
		if(WFC.logVerbose) Log.write(this, "Handle command "+o.toJson(), false, false, true);
		if(!WFC.blockUnencrypted && o.isSet(WFC.KEY_CMD_MESSAGES_LIST)){
			Log.write(this, "Process plain message list", false, WFC.logPrintEnabled, true);
			processList(o.getJson(WFC.KEY_CMD_MESSAGES_LIST));
		}
		if(o.isSet(WFC.KEY_CMD_MESSAGES_LIST_ENCRYPTED)){
			String encodedEncrypted = o.getProperty(WFC.KEY_CMD_MESSAGES_LIST_ENCRYPTED);
			IJson j = KeyEncryption.getJsonFromEncodedEncrypted(symmetricKey, encodedEncrypted);
			Log.write(this, "Process encrypted message list", false, WFC.logPrintEnabled, true);
			processList(j);
		}
	}
	
	/**
	 * 
	 * @param j
	 */
	private void processList(IJson j){
		if(j != null && j.toJsonArray() != null){
			for(IJson a : j.toJsonArray()){
				if(a != null && a.toJsonObject() != null){
					final IJsonObject message = a.toJsonObject();
					final String cmd = message.getProperty(WFC.KEY_CMD);
					if(cmd != null){
						process(message, cmd); 
					}
				}
			}
		}
	}
	
	/**
	 * Handles start, kill and patch synchronously.
	 * find, tasklist and ping are handled asynchronously.
	 * 
	 * Note: These are the operations exposed to remote systems.
	 * 
	 * @param message
	 * @param cmd
	 */
	private void process(final IJsonObject message, final String cmd){
		if(WFC.VAL_CMD_INSTANCE_START.equals(cmd)){
			startInstance(message);
		} else if(WFC.VAL_CMD_INSTANCE_KILL.equals(cmd)){
			killInstance(message);
		} else if(WFC.VAL_CMD_PATCH.equals(cmd)){
			patch(message);
		} else ThreadUtils.create(new Runnable() {
			public void run() {
				if(WFC.VAL_CMD_FIND.equals(cmd)){
					find(message);
				} else if(WFC.VAL_CMD_TASKLIST.equals(cmd)){
					getTaskList(message);
				} else if(WFC.VAL_CMD_PING.equals(cmd)){
					ping(message);
				} else{
					Log.write(this, "Unknown command "+cmd, true, WFC.logPrintEnabled, true);
				}
			}
		});
	}
	
	/**
	 * 
	 */
	private final void init(){
		IJsonObject init = Json.object();
		for(String key : initData.keySetJson()){
			init.setProperty(key, initData.getProperty(key));
		}
		try {
			symmetricKey = KeyEncryption.generateSymmetricKey();
			String keyName = KeyEncryption.getKeyName(WFServer.class);
			Log.write(this, "Using public key "+keyName, false, WFC.logPrintEnabled, true);
			PublicKey pubKey = KeyEncryption.getPublicKey(WFC.KEY_CRYPTO_KEYS, keyName);
			byte[] encryptedKey = KeyEncryption.encrypt(pubKey, symmetricKey);
			String encodedEncryptedKey = KeyEncryption.encode(encryptedKey);
			init.setProperty(WFC.KEY_INIT_KEY, encodedEncryptedKey);
			init.setProperty(WFC.KEY_CMD_ENCRYPTION_ENABLED, Constants.KEY_FALSE);
			Log.write(this, "Using "+KeyEncryption.encode(symmetricKey), false, WFC.logPrintEnabled, true);
		} catch (Exception e) {
			Log.caught(this, e);
			Log.flush();
			throw new AssertionError("Key generation failed.");
		}
		getWriter().add(init);
	}
	
	/**
	 * Search the logfile matching the provided instanceId.
	 * The returned data will be the line that contains the seach pattern.
	 * @param o
	 */
	private void find(IJsonObject o){
		String path = WFC.CONST_WARFRAME_APPDATA;
		path = path.replace(WFC.VAR_USER, System.getProperty("user.name"));
		int instanceId = Integer.parseInt("0"+StringUtils.keep(o.getProperty(WFC.KEY_CMD_INSTANCE), StringUtils.DIGITS));
		path = path+WFC.CONST_WARFRAME_LOG_PREFIX+instanceId+WFC.CONST_WARFRAME_LOG_POSTFIX;
		File f = new File(path);
		String pattern = o.getProperty(WFC.KEY_CMD_PATTERN);
		String length = o.getProperty(WFC.KEY_CMD_LENGTH);
		if(path != null && pattern != null){
			if(WFC.logVerbose) Log.write(this, "Search "+path+" for "+pattern, false, false, true);
			long len = length == null ? Long.MAX_VALUE : Long.parseLong("0"+StringUtils.keep(length, StringUtils.DIGITS));
			try {
				if(!f.exists()){
					o.setProperty(WFC.KEY_CMD_DATA, WFC.ERROR_FILE_DOES_NOT_EXIST);
				} else{
					RAFStream raf = new RAFStream(new RandomAccessFile(path, "r"));
					Scanner sc = new Scanner(raf.in);
					long read = 0;
					while(read <= len && sc.hasNextLine()){
						String line = sc.nextLine();
						if(line != null){
							if(line.contains(pattern)){
								o.setProperty(WFC.KEY_CMD_DATA, line);
								break;
							}
							read += line.length();
						}
					}
					sc.close();
				}
				writer.add(o);
			} catch (IOException e) {
				Log.caught(this, e);
			}
		}
	}
	
	/**
	 * Start a new instance using the provided settings. Settings found in DS.cfg.
	 * @param o
	 */
	private void startInstance(IJsonObject o) {
		try {
			String settings = o.getProperty(WFC.KEY_CMD_SETTINGS);
			int instanceId = Integer.parseInt("0"+StringUtils.keep(o.getProperty(WFC.KEY_CMD_INSTANCE), StringUtils.DIGITS));
			Log.write(this, "Start instance "+instanceId+", "+settings, false, WFC.logPrintEnabled, true);
			if(!patching && exe != null && settings != null && instanceId != 0){
				int offset = 0;
				if(o.isSet(WFC.KEY_CMD_INSTANCE_OFFSET)) offset = StringUtils.parseInt(o.getProperty(WFC.KEY_CMD_INSTANCE_OFFSET));
				String path = WFC.CONST_WARFRAME_APPDATA.replace(WFC.VAR_USER, System.getProperty("user.name"));
				String logFile = WFC.CONST_WARFRAME_LOG_PREFIX+instanceId+WFC.CONST_WARFRAME_LOG_POSTFIX;
				if(new File(path+logFile).exists()) {
					try {
						Stream.copyFile(path+logFile, path+WFLogFile.NEW_LOG_SAVE_NAME+File.separator+logFile+Time.millis()+WFC.CONST_WARFRAME_LOG_POSTFIX, false);
					} catch(IOException e) {
						Log.caught(this, e);
					}
				}
				WFInstance instance = new WFInstance(WFUtils.getExe(exe, instanceId), settings, instanceId + offset, WFUtils.getNumaNodeCount());
				instance.start();
				Time.sleep(START_INSTANCE_DELAY);
			}
		} catch (IOException e) {
			Log.caught(this, e);
		}
	}

	/**
	 * Kill thte instance matching the provided instanceId.
	 * @param o
	 */
	private void killInstance(IJsonObject o) {
		try {
			int instanceId = StringUtils.parseInt(o.getProperty(WFC.KEY_CMD_INSTANCE));
			if(!patching && instanceId != 0){
				Log.write(this, "Kill instance "+instanceId, false, WFC.logPrintEnabled, true);
				List<WFRunningInstance> instances = WFUtils.getRunningInstances();
				for(WFRunningInstance instance : instances){
					if(instance.id == instanceId) Sys.execute("taskkill /pid "+instance.pid);
				}
			}
		} catch (IOException e) {
			Log.caught(this, e);
		}
	}
	
	/**
	 * Get info about the running warframe processes.
	 * @param o
	 */
	private void getTaskList(IJsonObject o) {
		try {
			Log.write(this, "Get tasklist", false, WFC.logPrintEnabled, true);
			String tasklist = Sys.execute("tasklist /v /FI \"IMAGENAME eq Warframe*\"");
			o.setProperty(WFC.KEY_CMD_DATA, tasklist);
			getWriter().add(o);
		} catch (IOException e) {
			Log.caught(this, e);
		}
	}
	
	/**
	 * Patch the game to the latest version.
	 * Will wait 30 minutes before forcefully closing instances.
	 * If there are more then a single engine prepared, they will be prepared again after patching.
	 * @param o
	 */
	private void patch(IJsonObject o){
		if(!patching){
			patching = true;
			Time.sleep(5000);
			try {
				Log.write(this, "Patching "+o.toJson(), false, WFC.logPrintEnabled, true);
				if(exe != null){
					Log.write(WFPatch.class, "Waiting for servers to close...", false, WFC.logPrintEnabled, true);
					long time = Time.millis();
					while(!WFUtils.getUsedIds().isEmpty() && Time.millis() - time < Time.HOUR/2){
						Time.sleep(5000);
					}
					// ensure all servers are off if they timeout
					Sys.execute("taskkill /im \"Warframe.x64.exe\"");
					Sys.execute("taskkill /im \"Warframe.exe\"");
					Time.sleep(2000);
					// patch
					String home = System.getProperty("user.home", "");
					String localAppData = home+"\\AppData\\Local\\Warframe";
					WFPatch.patchAndPrep(localAppData, exe, false, null);
					WFLogFile.clearLogs(null);
				}
			} catch (IOException e) {
				Log.caught(this, e);
			} finally{
				patching = false;
			}
		}
	}
	
	/**
	 * Return given input.
	 * @param o
	 */
	private void ping(IJsonObject o){
		getWriter().add(o);
	}
}
