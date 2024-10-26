package spiedie.warframe.allocator;

import java.io.IOException;
import java.io.OutputStream;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonObject;
import spiedie.utilities.encryption.KeyEncryption;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.util.WFGenericWriter;

public class WFServerWriter extends WFGenericWriter{
	public WFServerWriter(OutputStream out){
		super(out);
	}
	
	public void write(IJson j) throws IOException {
		writeJsonToStream(j);
	}
	
	private void writeJsonToStream(IJson j) throws IOException{
		IJsonObject o = j.toJsonObject();
		Log.write(this, "Write "+o.toJson(), false, false, true);
		if(WFC.encryptionEnabled && o.isSet(WFC.KEY_CMD_MESSAGES_LIST)){
			String jsonList = o.getJson(WFC.KEY_CMD_MESSAGES_LIST).toJson();
			o = Json.object();
			if(symmetricKey != null){
				String encodedEncrypted = KeyEncryption.getEncodedEncryptedFromString(symmetricKey, jsonList);
				if(encodedEncrypted != null){
					o.setProperty(WFC.KEY_CMD_MESSAGES_LIST_ENCRYPTED, encodedEncrypted);
				}
			}
		}
		String json = o.toJson();
		Stream.writeString(out, json);
		out.flush();
	}
}
