package spiedie.warframe.allocator.console;

import spiedie.utilities.data.StringUtils;
import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.ISettings;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.allocator.command.WFEncryptedCommandProcessor;
import spiedie.warframe.allocator.command.WFStateProcessor;
import spiedie.warframe.allocator.command.WFStatusProcessor;
import spiedie.warframe.allocator.command.WFSystemsProcessor;
import spiedie.warframe.allocator.command.WFTrollProcessor;

public class WFRemoteConsole extends WFConsoleBase{
	public static final String VERSION = "0.1.0";
	public WFRemoteConsole(WFAllocationManager manager, ITextOutput out) {
		super(manager, new RemotePermission(), out);
		add(new WFStatusProcessor(manager, permissions, out), "-status");
		add(new WFSystemsProcessor(manager, permissions, out), "-systems");
		add(new WFEncryptedCommandProcessor(manager, permissions, out), "-enc");
		add(new WFTrollProcessor(manager, permissions, out), "-"+StringUtils.random(10, StringUtils.UPPER+StringUtils.LOWER+StringUtils.DIGITS));
	}
	
	public static class RemotePermission implements WFPermission{
		public boolean hasPermission(String key, ISettings settings) {
			if(key == null || settings == null) {
				return false;
			}
			Log.write(this, "Check permission "+key+" on "+settings.getProperties(), false, WFC.logPrintEnabled, true);
			if(key.equals(WFC.KEY_PERMISSION)) {
				return true;
			}
			if(key.equals("help")){
				settings.setProperty(WFC.KEY_PERMISSION, WFC.VAL_PERMISSION_GRANTED);
				return true;
			}
			if(key.equals(WFStateProcessor.class.getName()) || key.equals("state")) return true;
			if(key.equals(WFStatusProcessor.class.getName()) || key.equals("status")) return true;
			if(key.equals(WFSystemsProcessor.class.getName()) || key.equals("systems")) return true;
			if(key.equals(WFEncryptedCommandProcessor.class.getName()) || key.equals("encrypted")) return true;
			if(key.equals(WFTrollProcessor.class.getName()) || key.equals("troll")) return true;
			return false;
		}
	}
}
