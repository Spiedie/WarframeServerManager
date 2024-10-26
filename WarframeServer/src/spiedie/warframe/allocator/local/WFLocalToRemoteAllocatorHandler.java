package spiedie.warframe.allocator.local;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.PublicKey;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonObject;
import spiedie.utilities.concurrency.SimpleQueueProcessor;
import spiedie.utilities.encryption.KeyEncryption;
import spiedie.utilities.util.Constants;
import spiedie.utilities.util.MemorySettings;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.allocator.WFConnection;
import spiedie.warframe.util.WFConnectable;

public class WFLocalToRemoteAllocatorHandler extends SimpleQueueProcessor<IJson> implements WFConnectable{
	private WFLocalToRemoteReader reader;
	private WFLocalToRemoteWriter writer;
	private WFAllocationManager manager;
	private String name;
	public WFLocalToRemoteAllocatorHandler(WFAllocationManager manager, String name, InputStream in, OutputStream out, boolean enableRemoteControl){
		this.name = name;
		this.manager = manager;
		this.writer = new WFLocalToRemoteWriter(out);
		this.reader = new WFLocalToRemoteReader(in, enableRemoteControl);
		Queue<IJson> q = new ConcurrentLinkedDeque<>();
		setQueue(q);
		reader.setQueue(q);
		init();
		this.reader.start();
		this.writer.start();
		Log.write(this, "WFLocalToRemoteAllocatorHandler created with remote="+enableRemoteControl);
	}
	
	private final void init(){
		IJsonObject init = Json.object();
		try {
			byte[] symmetricKey = KeyEncryption.generateSymmetricKey();
			String keyName = KeyEncryption.getKeyName(WFLocalToRemoteAllocatorHandler.class);
			Log.write(this, "Using public key "+keyName);
			PublicKey pubKey = KeyEncryption.getPublicKey(WFC.KEY_CRYPTO_KEYS, keyName);
			byte[] encryptedKey = KeyEncryption.encrypt(pubKey, symmetricKey);
			String encodedEncryptedKey = KeyEncryption.encode(encryptedKey);
			init.setProperty(WFC.KEY_CMD, WFC.VAL_CMD_INIT);
			init.setProperty(WFC.KEY_INIT_KEY, encodedEncryptedKey);
			init.setProperty(WFC.KEY_INIT_VERSION, WFAllocationManager.VERSION);
			init.setProperty(WFC.KEY_INIT_ALLOC_NAME, String.valueOf(name));
			init.setProperty(WFC.KEY_CMD_ENCRYPTION_ENABLED, Constants.KEY_FALSE);
			Log.write(this, "Using "+KeyEncryption.encode(symmetricKey));
			reader.setKey(symmetricKey);
			writer.setKey(symmetricKey);
		} catch (Exception e) {
			Log.caught(this, e);
			Log.flush();
			throw new AssertionError("Key generation failed.");
		}
		getWriter().add(init);
	}
	
	public WFLocalToRemoteReader getReader(){
		return reader;
	}
	
	public WFLocalToRemoteWriter getWriter(){
		return writer;
	}
	
	public boolean isRunning() {
		return super.isRunning() && getReader().isRunning() && getWriter().isRunning();
	}
	
	public void run(){
		super.run();
		getReader().close();
		getWriter().close();
	}
	
	protected void process(IJson j) {
		Log.write(this, "Handle "+j.toJson());
		if(j.toJsonObject() != null){
			IJsonObject o = j.toJsonObject();
			String command = o.getProperty(WFC.KEY_CMD);
			Log.write(this, "Handle remote command "+command);
			if(command != null){
				if(command.equals(WFC.VAL_CMD_REDIRECT) && o.isSet(WFC.KEY_CMD_REDIRECT)){
					IJson redirect = o.getJson(WFC.KEY_CMD_REDIRECT);
					for(WFConnection con : manager.getConnections()){
						con.getWriter().add(redirect);
					}
				} else if(command.equals(WFC.VAL_CMD_EXECUTE_LOCAL) && o.isSet(WFC.KEY_CMD_DATA)) {
					manager.getConsole().process(o.getProperty(WFC.KEY_CMD_DATA), new MemorySettings());
				}
			}
		}
		Time.sleep(1000);
	}
}
