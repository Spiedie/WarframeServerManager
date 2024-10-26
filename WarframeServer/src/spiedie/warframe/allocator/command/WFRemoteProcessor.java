package spiedie.warframe.allocator.command;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import spiedie.terminal.cmdProcessor.CmdArgs;
import spiedie.utilities.concurrency.ThreadUtils;
import spiedie.utilities.data.StringUtils;
import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.ISettings;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.WFC;
import spiedie.warframe.allocator.WFAllocationManager;
import spiedie.warframe.allocator.console.WFPermission;
import spiedie.warframe.allocator.console.WFRemoteConsole;
import spiedie.warframe.allocator.console.WFRemoteConsoleManager;
import spiedie.warframe.allocator.console.WFRemoteOutput;
import spiedie.warframe.allocator.data.WFRemoteLog;
import spiedie.warframe.allocator.local.WFLocalToRemoteAllocatorConnector;

public class WFRemoteProcessor extends WFDefaultCmdProcessor{
	private WFRemoteOutput rout;
	public WFRemoteProcessor(WFAllocationManager manager, WFPermission permissions, ITextOutput out, WFRemoteOutput rout) {
		super(manager, permissions, out);
		this.rout = rout;
	}

	public String description(){
		return "Add remote connections. Examples:\n"
				+ "\t-remote -console -ip MyServer -port 12345\n"
				+ "\t-remote -log -ip MyServer -port 12345\n"
				+ "\t-remote -alloc -ip MyServer -port 12345";
	}

	public void process(CmdArgs args, ISettings settings) {
		if(args.isSet("console")){
			processConnect(args);
		} else if(args.isSet("log")){
			processConnectLog(args);
		} else if(args.isSet("alloc")){
			processConnectAlloc(args);
		}
	}
	
	private void processConnect(CmdArgs args) {
		final String ip = WFVars.eval(args.getValue("ip"));
		int port = WFC.PORT_REMOTE_CONSOLE;
		if(args.isSet("port")) port = Integer.parseInt("0"+StringUtils.keep(args.getValue("port"), StringUtils.DIGITS));
		if(ip == null) println(this, "No ip defined.");
		if(ip != null){
			final int socketPort = port;
			println(this, "Connect to remote console at "+ip+":"+port);
			ThreadUtils.create(new Runnable() {
				public void run() {
					try {
						Socket s = new Socket(ip, socketPort);
						println(this, "Connected to remote console");
						WFRemoteConsoleManager c = new WFRemoteConsoleManager(new WFRemoteConsole(manager, rout), rout, s.getInputStream(), s.getOutputStream());
						c.start();
					} catch (UnknownHostException e) {
						println(this, e.toString());
					} catch (IOException e) {
						println(this, e.toString());
					}
				}
			});
		}
	}
	
	private void processConnectLog(CmdArgs args) {
		final String ip = WFVars.eval(args.getValue("ip"));
		int port = WFC.PORT_REMOTE_LOG;
		if(args.isSet("port")) port = Integer.parseInt("0"+StringUtils.keep(args.getValue("port"), StringUtils.DIGITS));
		if(ip == null) println(this, "No ip defined.");
		if(ip != null){
			final int socketPort = port;
			println(this, "Connect to remote log at "+ip+":"+port);
			ThreadUtils.create(new Runnable() {
				public void run() {
					try {
						Socket s = new Socket(ip, socketPort);
						println(this, "Connected to remote log");
						WFRemoteLog log = new WFRemoteLog(s.getOutputStream());
						manager.getLogs().add(log);
					} catch (UnknownHostException e) {
						println(this, e.toString());
					} catch (IOException e) {
						println(this, e.toString());
					}
				}
			});
		}
	}
	
	private void processConnectAlloc(CmdArgs args) {
		Log.write(this, "processConnectAlloc "+args);
		final String ip = WFVars.eval(args.getValue("ip"));
		int port = WFC.PORT_LOCAL_ALLOCATOR_LOGIN;
		boolean enableRemoteControl = args.isSet("enableRemoteControl");
		if(args.isSet("port")) port = Integer.parseInt("0"+StringUtils.keep(args.getValue("port"), StringUtils.DIGITS));
		if(ip == null) println(this, "No ip defined.");
		if(ip != null){
			println(this, "Connect to remote allocator at "+ip+":"+port);
			WFLocalToRemoteAllocatorConnector con = new WFLocalToRemoteAllocatorConnector(manager, ip, port, manager.getName(), enableRemoteControl);
			con.start();
			manager.setRemoteStateConnector(con);
		}
	}
}
