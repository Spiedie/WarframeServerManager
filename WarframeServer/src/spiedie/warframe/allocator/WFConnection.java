package spiedie.warframe.allocator;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonArray;
import spiedie.data.json.data.IJsonObject;
import spiedie.utilities.data.StringUtils;
import spiedie.utilities.encryption.KeyEncryption;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.util.WFRunningInstance;
import spiedie.warframe.util.WFUtils;

public class WFConnection {
	private static final long VAL_LOG_LIMIT_LENGTH = 1024 * 1024;
	private static final String VAL_PATTERN_SETTINGS = "Session - set";
	private static final String VAL_PATTERN_NEW_BUILD = "Login failed; old build";
	private static final String VAL_PATTERN_BUILD_EXPIRED = "[Error]: The cache could not be updated.";
	private WFConnector ctr;
	private WFServerReader reader;
	private WFServerWriter writer;
	private Queue<WFServerResponse> q;
	private IJsonObject serverInitData;
	private byte[] symmetricKey;
	
	public WFConnection(){
		setConnector(ctr);
	}
	
	public WFConnector getConnector(){
		return ctr;
	}
	
	public WFServerReader getReader(){
		return reader;
	}
	
	public WFServerWriter getWriter(){
		return writer;
	}
	
	public IJsonObject getServerData(){
		return serverInitData;
	}
	
	public byte[] getKey(){
		return symmetricKey;
	}
	
	public void setConnector(WFConnector ctr){
		this.ctr = ctr;
	}
	
	public boolean isConnected(){
		return getReader() != null && getWriter() != null && getReader().isRunning() && getWriter().isRunning();
	}
	
	public boolean init(InputStream in, OutputStream out){
		reader = new WFServerReader(in);
		writer = new WFServerWriter(out);
		q = new ConcurrentLinkedDeque<>();
		getReader().setQueue(q);
		getReader().start();
		Log.write(this, "Wait for init data...", false, WFC.logPrintEnabled, true);
		long time = Time.millis();
		while(q.isEmpty() && Time.millis() - time < 10000) Time.sleep(1);
		WFServerResponse res = q.poll();
		if(res == null){
			serverInitData = Json.object();
			return false;
		}
		else{
			serverInitData = res.object;
			String encodedEncryptedKey = serverInitData.getProperty(WFC.KEY_INIT_KEY);
			try {
				byte[] encryptedKey = KeyEncryption.decode(encodedEncryptedKey);
				String keyName = KeyEncryption.getKeyName(WFAllocationManager.class);
				Log.write(this, "Using private key "+keyName);
				PrivateKey privKey = KeyEncryption.getPrivateKey(WFC.KEY_CRYPTO_KEYS, keyName);
				symmetricKey = KeyEncryption.decrypt(privKey, encryptedKey); 
			} catch (Exception e) {
				Log.err(this, "Key decryption failed.");
				return false;
			}
		}
		getWriter().setKey(symmetricKey);
		getReader().setKey(symmetricKey);
		getWriter().start();
		return true;
	}
	
	public void requestInstances(){
		IJsonObject action = Json.object();
		IJsonArray actions = Json.array();
		action.putJson(WFC.KEY_CMD_MESSAGES_LIST, actions);
		IJsonObject tasklist = Json.object();
		tasklist.setProperty(WFC.KEY_CMD, WFC.VAL_CMD_TASKLIST);
		tasklist.setProperty(WFC.KEY_CMD_NAME, WFC.VAL_NAME_TASKLIST);
		actions.add(tasklist);
		
		for(int i = 1; i <= getConnector().getInstances();i++){
			IJsonObject findSettings = Json.object();
			findSettings.setProperty(WFC.KEY_CMD, WFC.VAL_CMD_FIND);
			findSettings.setProperty(WFC.KEY_CMD_NAME, WFC.VAL_NAME_FIND_SETTINGS+i);
			findSettings.setProperty(WFC.KEY_CMD_INSTANCE, String.valueOf(i));
			findSettings.setProperty(WFC.KEY_CMD_LENGTH, String.valueOf(VAL_LOG_LIMIT_LENGTH));
			findSettings.setProperty(WFC.KEY_CMD_PATTERN, VAL_PATTERN_SETTINGS);
			actions.add(findSettings);
			
			IJsonObject findNewBuild = Json.object();
			findNewBuild.setProperty(WFC.KEY_CMD, WFC.VAL_CMD_FIND);
			findNewBuild.setProperty(WFC.KEY_CMD_NAME, WFC.VAL_NAME_FIND_NEW_BUILD+i);
			findNewBuild.setProperty(WFC.KEY_CMD_INSTANCE, String.valueOf(i));
			findNewBuild.setProperty(WFC.KEY_CMD_LENGTH, String.valueOf(VAL_LOG_LIMIT_LENGTH));
			findNewBuild.setProperty(WFC.KEY_CMD_PATTERN, VAL_PATTERN_NEW_BUILD);
			actions.add(findNewBuild);
			
			IJsonObject findBuildExpired = Json.object();
			findBuildExpired.setProperty(WFC.KEY_CMD, WFC.VAL_CMD_FIND);
			findBuildExpired.setProperty(WFC.KEY_CMD_NAME, WFC.VAL_NAME_FIND_BUILD_EXPIRED+i);
			findBuildExpired.setProperty(WFC.KEY_CMD_INSTANCE, String.valueOf(i));
			findBuildExpired.setProperty(WFC.KEY_CMD_LENGTH, String.valueOf(VAL_LOG_LIMIT_LENGTH));
			findBuildExpired.setProperty(WFC.KEY_CMD_PATTERN, VAL_PATTERN_BUILD_EXPIRED);
			actions.add(findBuildExpired);
		}
		getWriter().add(action);
//		Log.write(action);
	}
	
	public List<WFRemoteInstance> tryReadInstances(long timeout){
		List<WFRemoteInstance> list = new ArrayList<>();
		long start = Time.millis();
		int processed = 0;
		int expectedProcessed = 1 + 3 * getConnector().getInstances();
		// wait for tasklist + settings per instance
		Log.write(this, "try reading instances", false, WFC.logPrintEnabled, true);
		Set<String> expected = new HashSet<>();
		expected.add(WFC.VAL_NAME_TASKLIST);
		for(int i = 1; i <= getConnector().getInstances();i++){
			expected.add(WFC.VAL_NAME_FIND_SETTINGS+i);
			expected.add(WFC.VAL_NAME_FIND_NEW_BUILD+i);
			expected.add(WFC.VAL_NAME_FIND_BUILD_EXPIRED+i);
		}
		List<IJsonObject> logs = new ArrayList<>();
		while(Time.millis() - start < timeout && processed < expectedProcessed){
			if(!q.isEmpty()){
				WFServerResponse res = q.poll();
				logs.add(res.object);
				if(res.object.isSet(WFC.KEY_CMD_NAME) && expected.contains(res.object.getProperty(WFC.KEY_CMD_NAME))){
					processed++;
				}
				if(WFC.logVerbose) Log.write(this, "Processed total "+processed, false, false, true);
				String objData = res.object.toString();
				if(WFC.logVerbose) Log.write(this, objData, false, false, true);
			} else Time.sleep(25);
		}
		for(IJsonObject o : logs){
			if(WFC.VAL_NAME_TASKLIST.equals(o.getProperty(WFC.KEY_CMD_NAME))){
				String tasklist = o.getProperty(WFC.KEY_CMD_DATA);
				for(WFRunningInstance instance : WFUtils.getRunningInstances(tasklist)){
					if(instance.id <= getConnector().getInstances()){
						list.add(new WFRemoteInstance(this, instance));
					}
				}
			}
		}
		for(int id = 1; id <= getConnector().getInstances();id++){
			boolean present = false;
			for(WFRemoteInstance remote : list){
				present = present || (id == remote.instance.id);
			}
			if(!present){
				list.add(new WFRemoteInstance(this, id));
			}
		}
		for(WFRemoteInstance remote : list){
			for(IJsonObject o : logs){
				if((WFC.VAL_NAME_FIND_SETTINGS+remote.instance.id).equals(o.getProperty(WFC.KEY_CMD_NAME))){
					String line = o.getProperty(WFC.KEY_CMD_DATA);
					if(line != null){
						int settingStart = line.indexOf('{');
						if(settingStart >= 0){
							String json = line.substring(settingStart);
							if(WFC.logVerbose) Log.write(this, ""+json, false, false, true);
							IJson j = Json.parse(json);
							if(j != null){
								remote.instance.settings = j.toJsonObject();
							}
						}
					}
				}
				if((WFC.VAL_NAME_FIND_NEW_BUILD+remote.instance.id).equals(o.getProperty(WFC.KEY_CMD_NAME))){
					String line = o.getProperty(WFC.KEY_CMD_DATA);
					if(line != null && line.contains(VAL_PATTERN_NEW_BUILD)){
						remote.newBuild = true;
					}
				}
				if((WFC.VAL_NAME_FIND_BUILD_EXPIRED+remote.instance.id).equals(o.getProperty(WFC.KEY_CMD_NAME))){
					String line = o.getProperty(WFC.KEY_CMD_DATA);
					if(line != null && line.contains(VAL_PATTERN_BUILD_EXPIRED)){
						remote.newBuild = true;
					}
				}
			}
		}
		long processTime = Time.millis() - start;
		Log.write(this, "Processing took "+processTime+" ms", false, WFC.logPrintEnabled, true);
		if(processed < expectedProcessed){
			close();
		}
		return list;
	}
	
	public long getPing(){
		String id = StringUtils.random(10);
		IJsonObject ping = Json.object();
		ping.setProperty(WFC.KEY_CMD, WFC.VAL_CMD_PING);
		ping.setProperty(WFC.KEY_CMD_NAME, id);
		ping = WFAllocationManager.getSingleCommandAction(ping);
		long time = Time.nanos();
		getWriter().add(ping);
		boolean match = false;
		while(q.isEmpty() && Time.millis() - time < 10000) Time.sleep(1);
		time = Time.nanos() - time;
		if(!q.isEmpty() && q.peek() != null && q.peek().object.isSet(WFC.KEY_CMD_NAME) && q.peek().object.getProperty(WFC.KEY_CMD_NAME).equals(id)){
			match = true;
			q.poll();
		}
		if(!match) return 10000;
		return time/1000000;
	}
	
	public void close(){
		if(getReader() != null) getReader().close();
		if(getWriter() != null) getWriter().close();
	}
	
	public String toString(){
		return "Connection("+ctr.toString()+")";
	}
	
	public IJson toJson(){
		IJsonObject o = Json.object();
		o.putJson("ctr", ctr.toJson());
		o.putJson("serverInitData", serverInitData == null ? Json.object() : serverInitData);
		return o;
	}
	
	public static WFConnection parse(IJson j){
		IJsonObject o = j.toJsonObject();
		WFConnection con = new WFConnection();
		con.ctr = WFConnector.parse(o.getJson("ctr"));
		con.serverInitData = o.getJson("serverInitData").toJsonObject();
		return con;
	}
}
