package spiedie.warframe.allocator.global;

import java.io.InputStream;

import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonObject;
import spiedie.utilities.encryption.KeyEncryption;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.util.WFGenericReader;

public class WFRemoteFromLocalReader extends WFGenericReader<IJson>{

	public WFRemoteFromLocalReader(InputStream in) {
		super(in);
	}

	public void handleMessage(IJson j) {
		if(j != null){
			boolean handled = false;
			IJsonObject messageItem = j.toJsonObject();
			if(messageItem != null){
				if(messageItem.isSet(WFC.KEY_CMD_MESSAGE)){
					handled = true;
					IJson message = messageItem.getJson(WFC.KEY_CMD_MESSAGE);
					if(message != null){
						Log.write(this, "Add unencrypted message", false, false, true);
						q.add(message);
					}
				} else if(messageItem.isSet(WFC.KEY_CMD_MESSAGE_ENCRYPTED)){
					handled = true;
					String encodedEncrypted = messageItem.getProperty(WFC.KEY_CMD_MESSAGE_ENCRYPTED);
					IJson message = KeyEncryption.getJsonFromEncodedEncrypted(symmetricKey, encodedEncrypted);
					if(message != null){
						Log.write(this, "Add encrypted message", false, false, true);
						q.add(message);
					}
				}
			}
			if(!handled){
				Log.write(this, "Add non-message", false, false, true);
				q.add(j);
			}
		}
	}
}
