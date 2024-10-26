package spiedie.warframe.allocator.command;

import java.security.PrivateKey;
import java.security.PublicKey;

import spiedie.terminal.cmdProcessor.CmdArgs;

import spiedie.utilities.encryption.KeyEncryption;
import spiedie.utilities.encryption.KeySign;
import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.ISettings;
import spiedie.utilities.util.MemorySettings;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.allocator.console.WFPermission;

public class WFEncryptedCommandProcessor extends WFDefaultCmdProcessor{

	public WFEncryptedCommandProcessor(WFAllocationManager manager, WFPermission permissions, ITextOutput out) {
		super(manager, permissions, out);
	}

	public String description(){
		return "Command Redirection";
	}

	public void process(CmdArgs args, ISettings settings) {
		boolean permission = permissions.hasPermission("encrypted", settings);
		settings.setProperty(WFC.KEY_PERMISSION, WFC.VAL_PERMISSION_DENIED);
		if(!permission){
			println(this, "Permission denied.");
		} else{
			if(args.isSet("command")){
				try{
					String encrypted = args.getValue("command");
					PublicKey pub = KeyEncryption.getPublicKey("info", "WFRemoteSignKey");
					PrivateKey priv = KeyEncryption.getPrivateKey("info", "WFRemoteEncryptionKey");
					byte[] buf = KeySign.decrypt(pub, priv, encrypted);
					if(buf == null){
						Log.err(this, "Decryption failed.");
					} else{
						String command = new String(buf, KeyEncryption.CHARSET);
						println(this, "Execute `"+command+"`");
						manager.getConsole().process(command, new MemorySettings());
						settings.setProperty(WFC.KEY_PERMISSION, WFC.VAL_PERMISSION_GRANTED);
					}
				} catch(Exception e){
					Log.caught(this, e);
				}
			}
		}
	}
	
}
