package spiedie.utilities.encryption;

import java.io.File;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJson;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.log.Log;

public class KeyEncryption {
	public static final String CHARSET = "UTF-8";
	public static final String ALGORITHM_SYMMETRIC = "AES";
	public static final String ALGORITHM_SYMMETRIC_WITH_PADDING = "AES/CBC/PKCS5Padding";
	public static final int ALGORITHM_SYMMETRIC_BITS = 128;
	public static final String ALGORITHM_ASYMMETRIC = "RSA";
	public static final String ALGORITHM_ASYMMETRIC_WITH_PADDING = "RSA/ECB/PKCS1Padding";
	public static final String KEY_PRIVATE = "Private";
	public static final String KEY_PUBLIC = "Public";
	
	public static final String getKeyName(Class<?> cls) throws IOException{
		return cls.getSimpleName();
	}
	
	public static PublicKey getPublicKey(String root, String keyName) throws Exception{
		if(!root.endsWith(File.separator)) root += File.separator;
		String keyPath = root+keyName+KEY_PUBLIC;
		
		byte[] buf = Stream.read(new File(keyPath));
		X509EncodedKeySpec spec = new X509EncodedKeySpec(buf);
		KeyFactory factory = KeyFactory.getInstance(ALGORITHM_ASYMMETRIC);
		return factory.generatePublic(spec);
	}
	
	public static PrivateKey getPrivateKey(String root, String keyName) throws Exception{
		if(!root.endsWith(File.separator)) root += File.separator;
		String keyPath = root+keyName+KEY_PRIVATE;
		
		byte[] buf = Stream.read(new File(keyPath));
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(buf);
		KeyFactory factory = KeyFactory.getInstance(ALGORITHM_ASYMMETRIC);
		return factory.generatePrivate(spec);
	}
	
	public static byte[] encrypt(PublicKey key, byte[] buf) throws Exception{
		Cipher cypher = Cipher.getInstance(ALGORITHM_ASYMMETRIC_WITH_PADDING);
		cypher.init(Cipher.ENCRYPT_MODE, key);
		return cypher.doFinal(buf);
	}
	
	public static byte[] decrypt(PrivateKey key, byte[] buf) throws Exception{
		Cipher cypher = Cipher.getInstance(ALGORITHM_ASYMMETRIC_WITH_PADDING);
		cypher.init(Cipher.DECRYPT_MODE, key);
		return cypher.doFinal(buf);
	}
	
	public static byte[] generateSymmetricKey() throws NoSuchAlgorithmException{
		KeyGenerator gen = KeyGenerator.getInstance(ALGORITHM_SYMMETRIC);
		gen.init(ALGORITHM_SYMMETRIC_BITS);
		return gen.generateKey().getEncoded();
	}
	
	public static byte[] encryptSymmetric(byte[] key, byte[] buf) throws Exception{
		Cipher cipher = Cipher.getInstance(ALGORITHM_SYMMETRIC_WITH_PADDING);
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, ALGORITHM_SYMMETRIC));
		byte[] encrypted = cipher.doFinal(buf);
		byte[] iv = cipher.getIV();
		byte[] data = new byte[encrypted.length + iv.length];
		System.arraycopy(iv, 0, data, 0, iv.length);
		System.arraycopy(encrypted, 0, data, iv.length, encrypted.length);
		return data;
	}
	
	public static byte[] decryptSymmetric(byte[] key, byte[] buf) throws Exception{
		byte[] iv = new byte[ALGORITHM_SYMMETRIC_BITS/8];
		byte[] encrypted = new byte[buf.length - iv.length];
		System.arraycopy(buf, 0, iv, 0, iv.length);
		System.arraycopy(buf, 16, encrypted, 0, encrypted.length);
		Cipher cipher = Cipher.getInstance(ALGORITHM_SYMMETRIC_WITH_PADDING);
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, ALGORITHM_SYMMETRIC), new IvParameterSpec(iv));
		return cipher.doFinal(encrypted);
	}
	
	public static String encode(byte[] buf){
		return Base64.getEncoder().encodeToString(buf);
	}
	
	public static byte[] decode(String s){
		return Base64.getDecoder().decode(s);
	}
	
	public static IJson getJsonFromEncodedEncrypted(byte[] key, String encodedEncrypted){
		try {
			byte[] encrypted = KeyEncryption.decode(encodedEncrypted);
			byte[] buf = KeyEncryption.decryptSymmetric(key, encrypted);
			String json = new String(buf, KeyEncryption.CHARSET);
			return Json.parse(json);
		} catch (Exception e) {
			Log.caught(KeyEncryption.class, e);
		}
		return null;
	}
	
	public static String getEncodedEncryptedFromString(byte[] key, String json){
		try {
			byte[] buf = json.getBytes(KeyEncryption.CHARSET);
			byte[] encrypted = KeyEncryption.encryptSymmetric(key, buf);
			return KeyEncryption.encode(encrypted);
		} catch (Exception e) {
			Log.caught(KeyEncryption.class, e);
		}
		return null;
	}
}
