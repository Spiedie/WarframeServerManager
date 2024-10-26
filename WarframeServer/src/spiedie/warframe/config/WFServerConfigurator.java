package spiedie.warframe.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJsonObject;
import spiedie.utilities.concurrency.ThreadUtils;
import spiedie.utilities.data.StringUtils;
import spiedie.utilities.graphics.display.TextOutput;
import spiedie.utilities.graphics.layout.ComponentDisplay;
import spiedie.utilities.jvm.Sys;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.stream.raf.RAFStream;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;
import spiedie.utilities.util.persistentInfo.Info;
import spiedie.warframe.WFC;
import spiedie.warframe.util.WFRunningInstance;
import spiedie.warframe.util.WFUtils;

public class WFServerConfigurator implements ActionListener{
	/*
	 * 0.1.0.0 save logs with "dedicated" in the name from %localappdata%\warframe to share
	 * 0.1.1.0 change output messages
	 * 0.1.2.0 dynamic ui loading for setting config
	 * 0.1.3.0 exec + exit
	 * 0.1.4.0 cmd args
	 * 0.1.5.0 companion update
	 * 0.1.6.0 companion start
	 * 0.2.0.0 local config only, multistart
	 * 0.2.1.0 list instances in log
	 * 0.2.2.0 list running instances only
	 * 0.2.3.0 re-enable set config
	 * 0.2.4.0 prepare instances using symbolic links if admin
	 * 0.2.5.0 load and save starter configs
	 * 0.2.6.0 auto patch and run config
	 * 0.2.7.0 fixed patch launching warframe when gpu present
	 * 0.2.8.0 kill empty instances
	 * 0.2.9.0 added list ping button
	 * 0.2.10.0 add max instance per engine setting
	 * 0.2.10.1 fix max instance per engine setting
	 * 0.2.10.2 add options for dx10 and dx11 settings
	 * 0.2.10.3 fix error deleting on engines
	 * 0.2.10.4 added more shared cache files to symlink exceptions
	 */
	public static final String VERSION = "0.2.10.4";
	public static final String KEY_SERVER_ROOT = "WFServerConfigRoot";
	
	public static final String KEY_WF_EXE = "wfExe";
	
	public static final String KEY_AUTO_CONFIG = "autoConfig";
	
	private ComponentDisplay display;
	private JTextField server, wfExe, autoConfig;
	private JLabel msg;
	private JButton config, saveLogs, clear, patch, prepareMultiInstance, runInstances, listRunning, listPing, auto, help, killAll;
	private boolean autoRunning = false;
	
	public WFServerConfigurator(){
		server = new JTextField();
		wfExe = new JTextField();
		autoConfig = new JTextField();
		config = new JButton("Set config");
		config.addActionListener(this);
		saveLogs = new JButton("Save logs (to Root)");
		saveLogs.addActionListener(this);
		clear = new JButton("Clear logs");
		clear.addActionListener(this);
		patch = new JButton("Patch");
		patch.addActionListener(this);
		listRunning = new JButton("List instances");
		listRunning.addActionListener(this);
		listPing = new JButton("List ping");
		listPing.addActionListener(this);
		help = new JButton("halp!");
		help.addActionListener(this);
		auto = new JButton("Auto");
		auto.addActionListener(this);
		killAll = new JButton("Kill all empty instances");
		killAll.addActionListener(this);
		
		prepareMultiInstance = new JButton("Setup Instances (>"+WFUtils.getMaxInstancesPerEngine()+" instances)");
		prepareMultiInstance.addActionListener(this);
		runInstances = new JButton("Start Instances");
		runInstances.addActionListener(this);
		
		msg = new JLabel();
		display = new ComponentDisplay();
		int labelOff = 10;
		int h = 20;
		int yOff = -h;
		display.addSimple("rootLabel", new JLabel("Root"), 0, 0, 0, h, 0, 0, labelOff, 0);
		display.addSimple("server", server, 0, yOff += h, 0, h, labelOff, 0, 100 - labelOff, 0);
		
		labelOff = 30;
		display.addSimple("wfExeLabel", new JLabel("Warframe Exe"), 0, yOff + h, 0, h, 0, 0, labelOff, 0);
		display.addSimple("wfExe", wfExe, 0, yOff += h, 0, h, labelOff, 0, 100 - labelOff, 0);
		
		labelOff = 10;
		
		display.addSimple("config", config, 0, yOff += h, 0, h, 0, 0, 100, 0);
		display.addSimple("saveLogs", saveLogs, 0, yOff += h, 0, h, 0, 0, 100, 0);
		display.addSimple("clear", clear, 0, yOff += h, 0, h, 0, 0, 100, 0);
		display.addSimple("patch", patch, 0, yOff += h, 0, h, 0, 0, 100, 0);
		display.addSimple("prepareMultiInstance", prepareMultiInstance, 0, yOff += h, 0, h, 0, 0, 100, 0);
		display.addSimple("runInstances", runInstances, 0, yOff += h, 0, h, 0, 0, 100, 0);
		display.addSimple("listRunning", listRunning, 0, yOff += h, 0, h, 0, 0, 100, 0);
		display.addSimple("listPing", listPing, 0, yOff += h, 0, h, 0, 0, 100, 0);
		display.addSimple("killAll", killAll, 0, yOff += h, 0, h, 0, 0, 100, 0);

		yOff += h;
		labelOff = 30;
		display.addSimple("autoLabel", new JLabel("Auto Config"), 0, yOff + h, 0, h, 0, 0, labelOff, 0);
		display.addSimple("autoConfig", autoConfig, 0, yOff += h, 0, h, labelOff, 0, 100 - labelOff, 0);
		display.addSimple("auto", auto, 0, yOff += h, 0, h, 0, 0, 100, 0);
		
		display.addSimple("help", help, 0, yOff += h, 0, h, 0, 0, 100, 0);
		display.addSimple("msg", msg, 0, yOff += h, 0, h, 0, 0, 100, 0);
		display.frame().setSize(400, 400);
		display.frame().setTitle(WFC.SERVER_CONFIGURATOR_VERSION_TITLE);
		
		try {
			if(Info.isSet(KEY_WF_EXE)) wfExe.setText(Info.getProperty(KEY_WF_EXE));
			if(Info.isSet(KEY_SERVER_ROOT)) server.setText(Info.getProperty(KEY_SERVER_ROOT));
			if(Info.isSet(KEY_AUTO_CONFIG)) autoConfig.setText(Info.getProperty(KEY_AUTO_CONFIG));
			if(wfExe.getText() == null || wfExe.getText().isEmpty()){
				String exe = WFUtils.getWarframeExeFromRegistry();
				if(exe != null) wfExe.setText(exe);
			}
		} catch (IOException e) {
			Log.caught(this, e);
		}
	}
	
	/**
	 * Switch the layout to the given display.
	 * @param d
	 */
	public void switchLayout(ComponentDisplay d){
		if(d == null) d = display;
		display.frame().getContentPane().removeAll();
		display.frame().getContentPane().add(d);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				display.frame().revalidate();
				display.frame().repaint();
			}
		});
	}
	
	public void actionPerformed(ActionEvent e) {
		msg.setText("");
		if(e.getSource() == config){
			setConfig();
		} else if(e.getSource() == saveLogs){
			saveLogs();
		} else if(e.getSource() == clear){
			clearLogs();
		} else if(e.getSource() == patch){
			WFPatch.patch(true, msg);
		} else if(e.getSource() == prepareMultiInstance){
			prepareInstances();
		} else if(e.getSource() == runInstances){
			new WFServerStarter(wfExe.getText(), display.frame().getX() + 100, display.frame().getY() + 100, msg);
		} else if(e.getSource() == listRunning){
			listRunningInstances();
		} else if(e.getSource() == listPing){
			listPing();
		} else if(e.getSource() == auto){
			auto();
		} else if(e.getSource() == help){
			showHelp();
		} else if(e.getSource() == killAll){
			killAllEmptyInstances();
		}
		try {
			Info.setProperty(KEY_SERVER_ROOT, server.getText());
			Info.setProperty(KEY_WF_EXE, wfExe.getText());
			Info.setProperty(KEY_AUTO_CONFIG, autoConfig.getText());
		} catch (IOException ex) {
			Log.caught(this, ex);
		}
		
	}
	
	class InstancePreparationRunner implements Runnable{
		public void run() {
			try {
				WFUtils.prepareMultiInstance(wfExe.getText(), msg);
			} catch (IOException e) {
				Log.caught(this, e);
			}
		}
	}

	/**
	 * 
	 */
	private void setConfig(){
		WFConfigCreator creator = new WFConfigCreator(this, msg);
		switchLayout(creator.getDisplay());
	}

	/**
	 * 
	 */
	private void saveLogs(){
		try {
			WFLogFile.saveLogs(server.getText(), true, msg);
		} catch (IOException e) {
			msg.setText("Error "+e);
			Log.caught(this, e);
		}
	}
	
	/**
	 * 
	 */
	private void clearLogs(){
		WFLogFile.clearLogs(msg);
	}
	
	/**
	 * 
	 */
	public void prepareInstances(){
		try {
			if(WFUtils.getUsedIds().isEmpty()){
				ThreadUtils.create(new InstancePreparationRunner());
			} else{
				msg.setText("Shut down all instances first.");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			msg.setText("Error getting running instances: "+ex);
		}
	}

	/**
	 * 
	 */
	private void listRunningInstances() {
		final Map<String, String> nodeMap = new HashMap<>();
		nodeMap.put("406000", "CTF");
		nodeMap.put("406009", "TDM");
		nodeMap.put("406010", "DM");
		nodeMap.put("406011", "SB");
		nodeMap.put("406012", "CTF Alt");
		nodeMap.put("406013", "TDM Alt");
		nodeMap.put("406014", "DM Alt");
		nodeMap.put("406015", "VT");
		final File[] fs = new File(WFConfigFile.getCfgLocation()).getParentFile().listFiles();
		if(fs != null){
			ThreadUtils.create(new Runnable() {
				public void run() {
					try {
						List<Integer> ids = WFUtils.getUsedIds();
						TextOutput tout = new TextOutput().withDisposeOnclose();
						tout.setTitle(WFC.SERVER_CONFIGURATOR_LIST_INSTANCE_TITLE);
						List<String> results = new ArrayList<>();
						for(File f : fs){
							if(f.getName().toLowerCase().contains("dedicatedserver") && f.getName().toLowerCase().endsWith(".log")){
								RAFStream stream = new RAFStream(new RandomAccessFile(f, "r"));
								String log = Stream.readToEOF(stream.in);
								stream.raf.close();
								int instance = -1;
								String elo = "Unknown";
								String id = "gameModeId";
								String user = "unknown";
								String[] ss = log.split("\n");
								for(String part : ss[0].split(" ")){
									part = part.trim();
									if(part.startsWith("-instance")) instance = Integer.parseInt("0"+StringUtils.keep(part, StringUtils.DIGITS));
								}
								for(String line : ss){
									if(line.toLowerCase().contains("logged in")){
										user = StringUtils.getFromPattern(line, "Logged in (.*)_server");
									}
									if(line.toLowerCase().contains("hostsession - setting")){
										int start = line.indexOf('{');
										if(start >= 0){
											IJsonObject o = Json.parse(line.substring(start)).toJsonObject();
											id = o.getProperty("gameModeId");
											elo = o.getProperty("eloRating");
										}
									}
								}
								if(nodeMap.containsKey(id)) id = nodeMap.get(id);
								if(elo.equals("0")) elo = "RC";
								if(elo.equals("2")) elo = "Non-RC";
								results.add(instance+": "+id+" "+elo+" as "+user+", running: "+ids.contains(instance));
							}
						}
						Collections.sort(results, new Comparator<String>() {
							public int compare(String a, String b) {
								int an = StringUtils.parseInt(a);
								int bn = StringUtils.parseInt(b);
								return an - bn;
							}
						});
						for(String line : results){
							tout.append(line+"\n");
						}
					} catch (IOException e) {
						Log.caught(this, e);
					}
				}
			});
		}
	}
	
	/**
	 * list ping of users in all running instances
	 */
	public void listPing() {
		final File[] fs = new File(WFConfigFile.getCfgLocation()).getParentFile().listFiles();
		if(fs != null){
			ThreadUtils.create(new Runnable() {
				public void run() {
					Log.start();
					try {
						List<Integer> ids = WFUtils.getUsedIds();
						TextOutput tout = new TextOutput().withDisposeOnclose();
						tout.setTitle(WFC.SERVER_CONFIGURATOR_LIST_PING_TITLE);
						List<List<String>> results = new ArrayList<>();
						for(File f : fs){
							if(f.getName().toLowerCase().contains("dedicatedserver") && f.getName().toLowerCase().endsWith(".log")){
								RAFStream stream = new RAFStream(new RandomAccessFile(f, "r"));
								Scanner sc = new Scanner(stream.in);
								//CreatePlayerForClient. id=1, user name=d33psl33p
								//0 (0) : need bps: 2054.93, bcs: NONE, allocated: 81920, congest %: 8, throttle: 0.000, max bps=16384, slow start=0, bad client: 0, rtt: 79
								//5 (7) : need bps: 7175.88, bcs: NONE, allocated: 81920, congest %: 15, throttle: 0.999, max bps=8192, slow start=0, bad client: 0, rtt: 129
								Log.write(f);
								boolean validId = false;
								Map<String, String> idToUserMap = new HashMap<>();
								Map<String, String> idToPingMap = new HashMap<>();
								while(sc.hasNextLine()) {
									String line = sc.nextLine();
									if(!validId && line.contains("-instance") ) {
										int id = StringUtils.parseInt(StringUtils.getFromPattern(line, ".*-instance:(\\d*) .*"));
										if(!ids.contains(id)) {
											break;
										} else {
											Log.err(this, id);
											validId = true;
										}
									} else if(line.contains("****")) {
										idToPingMap.clear();
									} else if(line.contains("CreatePlayerForClient")) {
										String id = StringUtils.getFromPattern(line, "id=(\\d*),");
										String name = StringUtils.getFromPattern(line, "user name=(.*)");
										if(id != null && name != null) {
											idToUserMap.put(id, name);
										}
									} else if(line.contains("need bps")) {
										String id = StringUtils.getFromPattern(line, "\\d*\\((\\d*)\\) : ");
										String rtt = StringUtils.getFromPattern(line, "rtt: (\\d*)");
										if(id != null && rtt != null) {
											idToPingMap.put(id, rtt);
										}
									}
								}
								stream.raf.close();
								double avg = 0;
								if(idToPingMap.size() > 0) {
									List<String> header = new ArrayList<>();
									header.add(f.getName());
									header.add("avg ping");
									results.add(header);
									for(String id : idToPingMap.keySet()) {
										String user = idToUserMap.get(id);
										String ping = idToPingMap.get(id);
										avg += StringUtils.parseInt(ping);
										Log.write(user+" = "+ping);
										List<String> list = new ArrayList<>();
										list.add("");
										list.add(user);
										list.add(ping);
										results.add(list);
									}
									avg /= idToPingMap.size();
									header.add(String.format("%.2f ms", avg));
									results.add(new ArrayList<>());
								}
							}
						}
						tout.setText(StringUtils.format(results));
					} catch (IOException e) {
						Log.caught(this, e);
					}
					Log.end("list ping took ");
				}
			});
		}
	}
	
	/**
	 * Continuously checks if all instances are empty. If they are, patch and restart based on config.
	 */
	private void auto(){
		if(!autoRunning){
			autoRunning = true;
			msg.setText("Auto patch enabled.");
			ThreadUtils.create(new Runnable() {
				public void run() {
					try {
						boolean running = true;
						while(running){
							boolean down = WFUtils.getUsedIds().isEmpty();
							if(down){
								String exe = wfExe.getText();
								WFPatch.patchAndPrep(server.getText(), exe, true, msg);
								msg.setText("Launch...");
								String config = autoConfig.getText();
								WFServerStarter st = new WFServerStarter(exe, display.frame().getX() + 100, display.frame().getY() + 100, msg);
								if(st.setSelected(config)){
									st.loadSelectedConfig();
									st.startServers();
								} else{
									msg.setText("Couldn't find config "+config);
								}
							}
							Time.sleep(60000);
						}
					} catch (IOException e) {
						Log.caught(this, e);
					}
				}
			});
		} else{
			msg.setText("Auto already patch enabled.");
		}
	}
	
	/**
	 * 
	 */
	public void killAllEmptyInstances(){
		try {
			List<WFRunningInstance> instances = WFUtils.getRunningInstances();
			for(int i = 0; i < instances.size();i++){
				WFRunningInstance instance = instances.get(i);
				if(instance.players == 0){
					Sys.execute("taskkill /pid "+instance.pid);
					instances.remove(i--);
				}
			}
		} catch (IOException e) {
			Log.caught(this, e);
		}
	}
	
	/**
	 * 
	 */
	private void showHelp() {
		ThreadUtils.create(new Runnable() {
			public void run() {
				TextOutput tout = new TextOutput(50, 50, 800, 600).withDisposeOnclose();
				tout.setTitle(WFC.SERVER_CONFIGURATOR_HELP_TITLE);
				tout.setText(WFC.SERVER_CONFIGURATOR_HELP_MSG);
			}
		});
	}
	
	/**
	 * 
	 * @param path
	 * @return
	 */
	public static String toPath(String path){
		if(!path.isEmpty() && !path.endsWith(File.separator)) path += File.separator;
		return new File(path).getAbsolutePath()+File.separator;
	}
	
	public static void main(String[] args){
		new WFServerConfigurator();
	}
}
