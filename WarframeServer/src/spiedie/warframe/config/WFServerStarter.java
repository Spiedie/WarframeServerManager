package spiedie.warframe.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import spiedie.data.json.Json;
import spiedie.data.json.data.IJson;
import spiedie.data.json.data.IJsonArray;
import spiedie.data.json.data.IJsonObject;
import spiedie.utilities.concurrency.ThreadUtils;
import spiedie.utilities.data.StringUtils;
import spiedie.utilities.graphics.layout.ComponentDisplay;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.Time;
import spiedie.utilities.util.log.Log;
import spiedie.utilities.util.persistentInfo.Info;
import spiedie.warframe.util.WFInstance;
import spiedie.warframe.util.WFUtils;

public class WFServerStarter implements ActionListener{
	public static final String KEY_CONFIGS = "WFConfigs";
	public static final String KEY_CONFIG_PROPERTY_NAME = "name";
	public static final long VAL_START_DELAY = 5000;
	private ConfigNameSorter sorter;
	private ComponentDisplay display;
	private JButton start, saveConfig, loadConfig;
	private JComboBox<String> configsBox;
	private DefaultComboBoxModel<String> configsModel;
	private List<WFServerSetting> settings;
	private int labelOff = 50;
	private int h = 20;
	private int yOff = -h;
	
	private JLabel msg;
	private String warframeExe;
	
	public WFServerStarter(String warframeExe, int xPosOff, int yPosOff, JLabel msg){
		if(!WFUtils.isValidExePath(warframeExe)){
			if(msg != null) msg.setText("Invalid exe: "+warframeExe);
			throw new IllegalArgumentException("Invalid exe: "+warframeExe);
		}
		this.msg = msg;
		this.warframeExe = warframeExe;
		this.settings = new ArrayList<>();
		this.sorter = new ConfigNameSorter();
		start = new JButton("Start");
		start.addActionListener(this);
		saveConfig = new JButton("Save");
		saveConfig.addActionListener(this);
		loadConfig = new JButton("Load");
		loadConfig.addActionListener(this);
		configsModel = new DefaultComboBoxModel<>();
		configsBox = new JComboBox<>(configsModel);
		display = new ComponentDisplay();
		loadFromTemplate();
		yOff += h;
		display.addSimple("configsBox", configsBox, 0, yOff += h, 0, 20, 0, 0, 100, 0);
		
		display.addSimple("loadConfig", loadConfig, 0, yOff += h, 0, 20, 0, 0, 100, 0);
		display.addSimple("saveConfig", saveConfig, 0, yOff += h, 0, 20, 0, 0, 100, 0);
		display.addSimple("start", start, 0, yOff += h, 0, 20, 0, 0, 100, 0);
		display.frame().setSize(400, 450);
		display.frame().setLocation(xPosOff,  yPosOff);
		display.frame().setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		int runningInstances = 0;
		try{
			runningInstances = WFUtils.getUsedIds().size();
		} catch(IOException e){
			Log.err(this, e);
		}
		int maxInstances = WFUtils.getMaxInstances(warframeExe);
		display.frame().setTitle("Server Starter, "+runningInstances+"/"+maxInstances+" Instances running");
	}
	
	/**
	 * 
	 */
	private final void loadFromTemplate() {
		String path = WFConfigFile.getCfgLocation();
		Log.write(this, "Load config from "+path);
		try {
			String template = Stream.readToEOF(path);
			List<String> matches = StringUtils.getAllFromPattern(template, "\\[(.*),.*LotusDedicatedServerSettings\\]");
			matches = new ArrayList<>(new HashSet<>(matches));
			Collections.sort(matches, sorter);
			for(String match : matches){
				WFServerSetting setting = new WFServerSetting(match);
				settings.add(setting);
				add(match, setting);
			}
			loadConfigs();
		} catch (IOException e) {
			Log.caught(this, e);
			msg.setText("Error loading template: "+e);
		}
	}
	
	static class ConfigNameSorter implements Comparator<String>{
		public int compare(String a, String b) {
			boolean aRC = a.contains("RC");
			boolean bRC = b.contains("RC");
			if(aRC && !bRC) return -1;
			if(!aRC && bRC) return 1;
			return a.compareTo(b);
		}
	}
	
	/**
	 * 
	 * @param label
	 * @param setting
	 */
	private void add(String label, WFServerSetting setting){
		display.addSimple(label+"Label", setting.getLabel(), 0, yOff += h, 0, h, 0, 0, labelOff, 0);
		display.addSimple(label, setting.getDropdown(), 0, yOff, 0, h, labelOff, 0, 100 - labelOff, 0);
	}
	
	/**
	 * 
	 * @return
	 */
	
	public void actionPerformed(final ActionEvent e) {
		if(e.getSource() == start){
			ThreadUtils.create(new Runnable() {
				public void run() {
					try {
						startServers();
					} catch (IOException e) {
						Log.caught(this, e);
					}
				}
			});
		}
		try{
			if(e.getSource() == saveConfig){
				addCurrentConfig();
			} else if(e.getSource() == loadConfig){
				loadSelectedConfig();
			}
		} catch(IOException ex){
			Log.caught(this, ex);
			if(msg != null) msg.setText("Error with config: "+ex);
		}
	}
	
	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	private IJsonArray loadConfigData() throws IOException{
		String json = Info.isSet(KEY_CONFIGS) ? Info.getProperty(KEY_CONFIGS) : "[]";
		if(json == null || json.isEmpty()) json = "[]";
		IJsonArray a = Json.parse(json).toJsonArray();
		return a;
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void loadConfigs() throws IOException{
		Log.write(this, "loadConfig");
		IJsonArray a = loadConfigData();
		if(a != null){
			configsModel.removeAllElements();
			for(IJson j : a){
				IJsonObject o = j.toJsonObject();
				if(o != null){
					configsModel.addElement(o.getProperty(KEY_CONFIG_PROPERTY_NAME));
				}
			}
		}
	}
	
	/**
	 * 
	 * @param config
	 * @return
	 */
	public boolean setSelected(String config){
		configsBox.setSelectedItem(config);
		if(!configsBox.getSelectedItem().toString().equals(config)){
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void loadSelectedConfig() throws IOException{
		IJsonArray a = loadConfigData();
		String name = configsBox.getSelectedItem().toString();
		if(a != null && name != null){
			for(IJson j : a){
				IJsonObject o = j.toJsonObject();
				if(o != null && name.equals(o.getProperty(KEY_CONFIG_PROPERTY_NAME))){
					for(String config : o.keySetJson()){
						if(!KEY_CONFIG_PROPERTY_NAME.equals(config)){
							for(WFServerSetting setting : settings){
								if(setting.getName().equals(config)){
									setting.getDropdown().setSelectedIndex(Integer.parseInt(o.getProperty(config)));
								}
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	public void addCurrentConfig() throws IOException{
		Log.write(this, "saveConfig");
		String name = JOptionPane.showInputDialog(null, "Config name");
		if(name != null){
			IJsonArray a = loadConfigData();
			IJsonObject o = Json.object();
			o.setProperty(KEY_CONFIG_PROPERTY_NAME, name);
			for(WFServerSetting setting : settings){
				String val = setting.getDropdown().getSelectedItem().toString();
				if(!"0".equals(val)){
					o.setProperty(setting.getName(), setting.getDropdown().getSelectedItem().toString());
				}
			}
			a.add(o);
			Log.write(a);
			Info.setProperty(KEY_CONFIGS, a.toJson());
		}
		loadConfigs();
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void startServers() throws IOException{
		List<Integer> used = WFUtils.getUsedIds();
		int maxInstances = WFUtils.getMaxInstances(warframeExe);
		int instanceId = 1;
		List<WFInstance> list = new ArrayList<>();
		for(WFServerSetting setting : settings){
			String cfg = setting.getName();
			int instances = Integer.parseInt(setting.getModel().getSelectedItem().toString());
			for(int i = 0; i < instances;i++){
				while(used.contains(instanceId)) instanceId++;
				if(instanceId <= maxInstances){
					WFInstance instance = new WFInstance(WFUtils.getExe(warframeExe, instanceId), cfg, instanceId, WFUtils.getNumaNodeCount());
					list.add(instance);
					instanceId++;
				}
			}
		}
		for(WFInstance instance : list){
			if(msg != null) msg.setText("Start "+instance.settings+"("+instance.instance+")");
			instance.start();
			Time.sleep(VAL_START_DELAY);
		}
		display.frame().dispose();
	}
	
}
