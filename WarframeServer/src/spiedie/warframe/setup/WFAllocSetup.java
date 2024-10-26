package spiedie.warframe.setup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import spiedie.utilities.graphics.layout.ComponentDisplay;
import spiedie.utilities.random.XorPerf;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.log.Log;
import spiedie.warframe.allocator.WFAllocationManager;

public class WFAllocSetup implements ActionListener{
	/*
	 * 0.1.0.0 set scripts/WFAllocInit.txt + info/server/name.dat
	 * 0.1.1.0 region dropdown, estimate max number of instances
	 * 0.1.2.0 include server files for allocator
	 * 0.1.2.1 added custom script file
	 * 0.1.2.2 fixed region select not working
	 * 0.1.2.3 added VT gamemode
	 * 0.1.2.4 added opt-in for remote control
	 * 0.1.2.5 add verbose log option
	 * 0.1.2.6 server change
	 * 0.1.2.7 remove VT game mode being enabled by default
	 */
	public static final String VERSION = "0.1.2.7";
	private ComponentDisplay display;
	private JButton generate, autoNumInstances;
	private JTextField allocName, serverName, instances;
	private DefaultComboBoxModel<String> regionModel;
	private JComboBox<String> regionBox;
	private JCheckBox enableStats, enableRemoteControl;
	public WFAllocSetup(){
		display = new ComponentDisplay();
		generate = new JButton("Setup");
		generate.addActionListener(this);
		autoNumInstances = new JButton("Auto Instances");
		autoNumInstances.addActionListener(this);
		
		allocName = new JTextField("John");
		serverName = new JTextField("JohnServer");
		instances = new JTextField("4");
		
		regionModel = new DefaultComboBoxModel<>();
		regionBox = new JComboBox<>(regionModel);
		
		regionModel.addElement("Europe");
		regionModel.addElement("North America");
		regionModel.addElement("South America");
		regionModel.addElement("Asia");
		regionModel.addElement("Oceania");
		regionModel.addElement("Russia");
		
		enableStats = new JCheckBox("(discord.me/conclave ::status)");
		enableStats.setSelected(true);
		enableRemoteControl = new JCheckBox("(allows remote patching)");
		int yOff = -20;
		int labelW = 30;
		int fieldW = 100 - labelW;
		
		display.addSimple("allocNameLabel", new JLabel("Allocator Name"), 0, yOff += 20, 0, 20, 0, 0, labelW, 0);
		display.addSimple("serverNameLabel", new JLabel("Server Name"), 0, yOff += 20, 0, 20, 0, 0, labelW, 0);
		display.addSimple("instancesLabel", new JLabel("Num Instances"), 0, yOff += 20, 0, 20, 0, 0, labelW, 0);
		display.addSimple("regionLabel", new JLabel("Region"), 0, yOff += 20, 0, 20, 0, 0, labelW, 0);
		display.addSimple("enableStatsLabel", new JLabel("Send stats"), 0, yOff += 20, 0, 20, 0, 0, labelW, 0);
		display.addSimple("enableRemoteControlLabel", new JLabel("Remote control"), 0, yOff += 20, 0, 20, 0, 0, labelW, 0);
		yOff = -20;
		display.addSimple("allocName", allocName, 0, yOff += 20, 0, 20, labelW, 0, fieldW, 0);
		display.addSimple("serverName", serverName, 0, yOff += 20, 0, 20, labelW, 0, fieldW, 0);
		display.addSimple("instances", instances, 0, yOff += 20, 0, 20, labelW, 0, fieldW, 0);
		display.addSimple("region", regionBox, 0, yOff += 20, 0, 20, labelW, 0, fieldW, 0);
		display.addSimple("enableStats", enableStats, 0, yOff += 20, 0, 20, labelW, 0, fieldW, 0);
		display.addSimple("enableRemoteControl", enableRemoteControl, 0, yOff += 20, 0, 20, labelW, 0, fieldW, 0);
		
		display.addSimple("estimate", autoNumInstances, 0, yOff += 20, 0, 20, 0, 0, 50, 0);
		display.addSimple("generate", generate, 0, yOff, 0, 20, 50, 0, 50, 0);
		
		display.frame().setSize(400, 400);
		display.frame().setTitle("Quick Setup v"+VERSION);
	}
	
	public void actionPerformed(ActionEvent e) {
		try {
			if(e.getSource() == generate){
				generate();
			} else if(e.getSource() == autoNumInstances){
				estimateNumInstances();
			} 
		} catch (IOException ex) {
			Log.caught(this, ex);
		}
	}
	
	/**
	 * 
	 */
	private void estimateNumInstances(){
		Log.write(this, "estimateNumInstances");
		double perf = XorPerf.getPerformance(Runtime.getRuntime().availableProcessors());
		String estimate = String.format("%.0f", Math.floor(perf));
		instances.setText(estimate);
		Log.write(this, "Perf: "+perf+", "+estimate);
	}
	
	/**
	 * 
	 * @throws IOException
	 */
	private void generate() throws IOException{
		Log.write(this, "generate");
		String script = generateScript(allocName.getText(), serverName.getText(), instances.getText(), regionModel.getSelectedItem().toString(), enableStats.isSelected(), enableRemoteControl.isSelected());
		Log.write(this, "Generated script:\n"+script);
		File pathToInitScript = new File("../WFAllocManagerLocal/"+WFAllocationManager.SCRIPT_INIT);
		File pathToInfoName = new File("../WFServerLoginLocal/info/server/name.dat");
		File pathToAllocInfoName = new File("../WFAllocManagerLocal/info/server/name.dat");
		if(pathToAllocInfoName.getParentFile().exists()){
			Stream.writeToFile(serverName.getText(), pathToAllocInfoName);
		} else{
			Log.write(pathToAllocInfoName+" Doesnt exist");
		}
		if(pathToInfoName.getParentFile().exists()){
			Stream.writeToFile(serverName.getText(), pathToInfoName);
		} else{
			Log.write(pathToInfoName+" Doesnt exist");
		}
		if(pathToInitScript.getParentFile().exists()){
			Stream.writeToFile(script, pathToInitScript);
		} else{
			Log.write(pathToInitScript+" Doesnt exist");
		}
	}
	
	/**
	 * 
	 * @param allocName
	 * @param serverName
	 * @param instances
	 * @param region
	 * @param enableSendStats
	 * @return the init script.
	 */
	public static String generateScript(String allocName, String serverName, String instances, String region, boolean enableSendStats, boolean enableRemoteControl){
		Log.write("Use region "+region);
		if(region.equals("Europe")) region = "7";
		if(region.equals("North America")) region = "4";
		if(region.equals("South America")) region = "6";
		if(region.equals("Asia")) region = "8";
		if(region.equals("Oceania")) region = "9";
		if(region.equals("Russia")) region = "14";
		
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(bout);
		out.println("-script -run scripts/WFAllocDisplayNames.txt");
		out.println("-script -run scripts/WFAllocGameModeNames.txt");
		out.println("-script -run scripts/WFAllocDefGameModes.txt");
		out.println("-script -run scripts/WFAllocStrings.txt");
		out.println();
		out.println(String.format("-config -connect -name \"%s\" -instances %s", serverName, instances));
		out.println();
		out.println(String.format("-config -allocName \"%s\"", allocName));
		out.println();
		out.println("-config -log -compactness 3 -verbose true");
		out.println("-config -listen");
		out.println("-serverLogin");
		out.println((enableSendStats ? "" : "//")+"-remote -alloc -ip 45.77.204.251 "+(enableRemoteControl ? "" : "//")+"-enableRemoteControl");
		out.println();
		out.println(String.format("-mode -enable %s 406000 //CTF", region));
		out.println(String.format("-mode -enable %s 406009 //TDM", region));
		out.println(String.format("-mode -enable %s 406010 //DM", region));
		out.println(String.format("-mode -enable %s 406011 //Lunaro", region));
		out.println(String.format("//-mode -enable %s 406015 //VT", region));
		out.println();
		out.println(String.format("//-mode -enable %s 406012 -trigger PVPMODE_CAPTURETHEFLAG //CTF Variant (not yet entirely set up)", region));
		out.println(String.format("-mode -enable %s 406013 -trigger PVPMODE_TEAMDEATHMATCH //TDM Variant", region));
		out.println(String.format("-mode -enable %s 406014 -trigger PVPMODE_DEATHMATCH  //DM Variant", region));
		out.println("-script -run scripts/WFCustom.txt");
		
		return new String(bout.toByteArray());
	}
	
	public static void main(String[] args){
		new WFAllocSetup();
	}
}
