package spiedie.warframe.allocator;

import java.io.InputStream;

import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonObject;
import spiedie.utilities.encryption.KeyEncryption;
import spiedie.warframe.WFC;
import spiedie.warframe.util.WFGenericReader;

public class WFServerReader extends WFGenericReader<WFServerResponse>{
	public WFServerReader(InputStream in){
		super(in);
	}

	public void handleMessage(IJson j) {
		if(j != null && j.toJsonObject() != null){
			IJsonObject messageItem = j.toJsonObject();
			if(messageItem.isSet(WFC.KEY_CMD_MESSAGE)){
				IJson message = messageItem.getJson(WFC.KEY_CMD_MESSAGE);
				if(message != null){
					WFServerResponse res = new WFServerResponse(message.toJsonObject());
					q.add(res);
				}
			}
			if(messageItem.isSet(WFC.KEY_CMD_MESSAGE_ENCRYPTED)){
				String encodedEncrypted = messageItem.getProperty(WFC.KEY_CMD_MESSAGE_ENCRYPTED);
				IJson message = KeyEncryption.getJsonFromEncodedEncrypted(symmetricKey, encodedEncrypted);
				if(message != null){
					WFServerResponse res = new WFServerResponse(message.toJsonObject());
					q.add(res);
				}
			}
		}
	}
}
