package spiedie.warframe.allocator.global;

import java.io.IOException;
import java.io.OutputStream;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonObject;
import spiedie.utilities.encryption.KeyEncryption;
import spiedie.utilities.util.Constants;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.util.WFGenericWriter;

public class WFRemoteFromLocalWriter extends WFGenericWriter{

	public WFRemoteFromLocalWriter(OutputStream out) {
		super(out);
	}

	public void write(IJson j) throws IOException {
		IJsonObject o = j.toJsonObject();
		if(o != null && o.isSet(WFC.KEY_CMD)){
			IJsonObject message = Json.object();
			message.setProperty(WFC.KEY_CMD_TIMESTAMP, String.valueOf(Time.millis()));
			if(WFC.encryptionEnabled && (!o.isSet(WFC.KEY_CMD_ENCRYPTION_ENABLED) || o.getProperty(WFC.KEY_CMD_ENCRYPTION_ENABLED).equals(Constants.KEY_TRUE))){
				String encryptedEncoded = KeyEncryption.getEncodedEncryptedFromString(symmetricKey, o.toJson());
				Log.write(this, "Encrypted message "+o.getProperty(WFC.KEY_CMD), false, false, true);
				if(encryptedEncoded != null){
					message.setProperty(WFC.KEY_CMD_MESSAGE_ENCRYPTED, encryptedEncoded);
				}
			} else{
				message.putJson(WFC.KEY_CMD_MESSAGE, o);
			}
			Json.write(out, message);
		} else{
			Json.write(out, j);
		}
		out.flush();
	}
}
