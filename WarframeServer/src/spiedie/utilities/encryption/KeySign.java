package spiedie.utilities.encryption;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJson;

import spiedie.utilities.data.StringUtils;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;

public class KeySign {
	public static final String KEY_KEY = "key";
	public static final String KEY_SIGN = "sign";
	public static final String KEY_CONTENT = "content";
	public static final String KEY_VALID_UNTIL = "validUntil";
	public static final String KEY_DATA = "data";
	public static final String ALGORITHM_SIGN = "SHA256withRSA";

	public static boolean verifySign(byte[] buf, byte[] sign, PublicKey pub) throws Exception{
		Signature sig = Signature.getInstance(ALGORITHM_SIGN);
		sig.initVerify(pub);
		sig.update(buf);
		return sig.verify(sign);
	}
	
	public static byte[] decrypt(PublicKey pub, PrivateKey priv, String jsonEncoded) throws Exception{
		if(jsonEncoded == null){
			Log.err(KeySign.class, "Invalid input");
			return null;
		}
		IJson j = Json.parse(new String(KeyEncryption.decode(jsonEncoded), KeyEncryption.CHARSET));
		if(j == null){
			Log.err(KeySign.class, "Invalid json");
			return null;
		}
		String encodedEncryptedKey = Json.getString(j, KEY_KEY);
		String encodedEncrypted = Json.getString(j, KEY_DATA);
		byte[] symmetricKey = KeyEncryption.decrypt(priv, KeyEncryption.decode(encodedEncryptedKey));
		
		byte[] jsonBuf = KeyEncryption.decryptSymmetric(symmetricKey, KeyEncryption.decode(encodedEncrypted));
		
		String jsonData = new String(jsonBuf, KeyEncryption.CHARSET);
		IJson data = Json.parse(jsonData);
		
		long validUntil = StringUtils.parseLong(Json.getString(data, KEY_VALID_UNTIL));
		if(Time.millis() > validUntil){
			Log.err(KeySign.class, "Timeout");
			return null;
		}
		byte[] content = KeyEncryption.decode(Json.getString(data, KEY_CONTENT));
		byte[] sign = KeyEncryption.decode(Json.getString(data, KEY_SIGN));
		
		if(!verifySign(content, sign, pub)){
			Log.err(KeySign.class, "Invalid sign");
			return null;
		}
		return content;
	}
	
}
