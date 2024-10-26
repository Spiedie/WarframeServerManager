package spiedie.warframe.server;

import java.io.IOException;
import java.io.OutputStream;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonObject;
import spiedie.utilities.encryption.KeyEncryption;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.Constants;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.util.WFGenericWriter;

public class WFWriter extends WFGenericWriter{
	
	public WFWriter(OutputStream out){
		super(out);
	}
	
	/**
	 * This method writes date to the output stream.
	 * 
	 * encrypts the message if encryption is enabled and either WFC.KEY_CMD_ENCRYPTION_ENABLED isn't set or it is enabled
	 * 
	 * @param o
	 * @throws IOException
	 */
	public void write(IJson j) throws IOException{
		IJsonObject o = j.toJsonObject();
		if(WFC.logVerbose) write("Write based on message "+o.getProperty(WFC.KEY_CMD));
		if(o.isSet(WFC.KEY_CMD)){
			IJsonObject message = Json.object();
			message.setProperty(WFC.KEY_CMD_TIMESTAMP, String.valueOf(Time.millis()));
			if(WFC.encryptionEnabled && (!o.isSet(WFC.KEY_CMD_ENCRYPTION_ENABLED) || o.getProperty(WFC.KEY_CMD_ENCRYPTION_ENABLED).equals(Constants.KEY_TRUE))){
				String encryptedEncoded = KeyEncryption.getEncodedEncryptedFromString(symmetricKey, o.toJson());
				if(encryptedEncoded != null){
					if(WFC.logVerbose) write("Write encrypted message");
					message.setProperty(WFC.KEY_CMD_MESSAGE_ENCRYPTED, encryptedEncoded);
				}
			} else{
				write("Write plain message");
				message.putJson(WFC.KEY_CMD_MESSAGE, o);
			}
			writeJsonToStream(message);
			out.flush();
		}
	}
	
	/**
	 * 
	 * @param o
	 */
	private void write(Object o){
		Log.write(this, o, false, false, true);
	}
	
	/**
	 * 
	 * @param j
	 * @throws IOException
	 */
	private void writeJsonToStream(IJson j) throws IOException{
		String json = j.toJson();
		if(WFC.logVerbose) write("Write json, "+json.length()+" characters");
		Stream.writeString(out, json);
		out.flush();
	}
}
