package spiedie.utilities.encryption;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import spiedie.utilities.files.FileUtils;

public class KeyGen {
	/*
	 * 0.1.0.0 initial version
	 */
	public static final String VERSION = "0.1.0.0";
	
	public static void generate(String root, String name) throws NoSuchAlgorithmException, IOException{
		generate(root, name, null);
	}
	
	public static void generate(String root, String name, SecureRandom random) throws NoSuchAlgorithmException, IOException{
		generate(root, name, name, random);
	}
	
	public static void generate(String root, String publicName, String privateName, SecureRandom random) throws NoSuchAlgorithmException, IOException{
		if(!root.endsWith(File.separator)) root += File.separator;
		KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
		if(random == null) {
			gen.initialize(2048);
		} else {
			gen.initialize(2048, random);
		}
		KeyPair pair = gen.generateKeyPair();
		write(root+publicName+KeyEncryption.KEY_PUBLIC, pair.getPublic().getEncoded());
		write(root+privateName+KeyEncryption.KEY_PRIVATE, pair.getPrivate().getEncoded());
	}
	
	private static void write(String file, byte[] buf) throws IOException{
		FileUtils.ensurePathExists(new File(file), true);
		OutputStream out = new FileOutputStream(file);
		out.write(buf);
		out.close();
	}
	
	public static void main(String[] args) throws Exception {
		String root = "key";
		String keyName = "GeneratedKey";
		for(int i = 1; i < args.length;i++) {
			if(args[i - 1].equals("root")) {
				root = args[i];
			} else if(args[i - 1].equals("name")) {
				keyName = args[i];
			}
		}
		generate(root, keyName);
	}
}
