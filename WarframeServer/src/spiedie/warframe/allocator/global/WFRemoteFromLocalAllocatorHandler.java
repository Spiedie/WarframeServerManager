package spiedie.warframe.allocator.global;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonArray;
import spiedie.data.json.data.IJsonObject;
import spiedie.utilities.concurrency.SimpleQueueProcessor;
import spiedie.utilities.encryption.KeyEncryption;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.allocator.WFRemoteInstance;
import spiedie.warframe.allocator.WFServerPoolState;
import spiedie.warframe.util.WFNetActivity;

public class WFRemoteFromLocalAllocatorHandler extends SimpleQueueProcessor<IJson>{
	private WFServerPoolState state;
	private WFNetActivity activity;
	private WFRemoteFromLocalReader reader;
	private WFRemoteFromLocalWriter writer;
	private String name, version;
	private long updateTime, timeout = 20 * Time.MINUTE;
	public WFRemoteFromLocalAllocatorHandler(InputStream in, OutputStream out){
		this.writer = new WFRemoteFromLocalWriter(out);
		this.reader = new WFRemoteFromLocalReader(in);
		Queue<IJson> q = new ConcurrentLinkedDeque<>();
		setQueue(q);
		reader.setQueue(q);
		this.reader.start();
		this.writer.start();
		updateTime = Time.millis();
	}
	
	public WFRemoteFromLocalReader getReader(){
		return reader;
	}
	
	public WFRemoteFromLocalWriter getWriter(){
		return writer;
	}
	
	public boolean isRunning() {
		return super.isRunning() && getReader().isRunning() && getWriter().isRunning();
	}
	
	public WFServerPoolState getState(){
		return state;
	}
	
	public WFNetActivity getActivity(){
		return activity;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return name;
	}
	
	public void setVersion(String version){
		this.version = version;
	}
	
	public String getVersion(){
		return version;
	}
	
	public boolean isTimedOut(){
		return Time.millis() - updateTime > timeout;
	}
	
	public void run(){
		super.run();
		getReader().close();
		getWriter().close();
	}

	protected void process(IJson j) {
		if(j.toJsonArray() != null){
			processServerStateArray(j);
			Log.write(this, "Updated state for "+name+" with array", false, false, true);
		} else if(j.toJsonObject() != null){
			IJsonObject o = j.toJsonObject();
			String cmd = o.getProperty(WFC.KEY_CMD);
			if(WFC.VAL_CMD_INIT.equals(cmd) || o.isSet(WFC.KEY_INIT_VERSION)){
				processInit(o);
			} else if(WFC.VAL_CMD_TRANSFER_SERVER_STATE.equals(cmd)){
				processServerState(o);
			}
		}
		Time.sleep(1000);
	}
	
	private void processInit(IJsonObject o){
		if(o.isSet(WFC.KEY_INIT_ALLOC_NAME)) setName(o.getProperty(WFC.KEY_INIT_ALLOC_NAME));
		if(o.isSet(WFC.KEY_INIT_VERSION)) setVersion(o.getProperty(WFC.KEY_INIT_VERSION));
		if(o.isSet(WFC.KEY_INIT_KEY)){
			String encodedEncryptedKey = o.getProperty(WFC.KEY_INIT_KEY);
			try {
				byte[] encryptedKey = KeyEncryption.decode(encodedEncryptedKey);
				String keyName = KeyEncryption.getKeyName(WFRemoteFromLocalAllocatorHandler.class);
				Log.write(this, "Using private key "+keyName);
				PrivateKey privKey = KeyEncryption.getPrivateKey(WFC.KEY_CRYPTO_KEYS, keyName);
				byte[] symmetricKey = KeyEncryption.decrypt(privKey, encryptedKey);
				reader.setKey(symmetricKey);
				writer.setKey(symmetricKey);
				Log.write(this, "Encryption key set.");
			} catch (Exception e) {
				Log.write(this, e, true, false, true);
				Log.err(this, "Key decryption failed.");
			}
		}
	}
	
	private void processServerState(IJsonObject o){
		if(o.isSet(WFC.KEY_CMD_SERVER_POOL_STATE)){
			IJson j = o.getJson(WFC.KEY_CMD_SERVER_POOL_STATE);
			if(j.toJsonArray() != null){
				processServerStateArray(j);
				Log.write(this, "Updated state for "+name, false, false, true);
			}
		}
		if(o.isSet(WFC.KEY_CMD_SERVER_POOL_ACTIVITY)){
			IJson j = o.getJson(WFC.KEY_CMD_SERVER_POOL_ACTIVITY);
			if(j.toJsonArray() != null){
				processServerActivity(j.toJsonArray());
				Log.write(this, "Updated activity for "+name, false, false, true);
			}
		}
	}
	
	private void processServerStateArray(IJson j){
		WFServerPoolState state = WFServerPoolState.parse(j);
		if(state != null){
			for(WFRemoteInstance instance : state.getInstances()){
				if(getName() != null){
					instance.connection.getServerData().setProperty(WFC.KEY_INIT_ALLOC_NAME, getName());
				}
			}
			this.state = state;
		}
		updateTime = Time.millis();
	}
	
	private void processServerActivity(IJsonArray j){
		activity = WFNetActivity.parse(j);
	}
	
	public String toString(){
		return "RemoteHandler("+getName()+")";
	}
}
