package spiedie.warframe.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import spiedie.utilities.data.StringUtils;
import spiedie.utilities.graphics.layout.ComponentDisplay;
import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.log.Log;

public class WFConfigCreator implements ActionListener{
	
	public static final String KEY_TEMPLATE = "dynamictemplate";
	private JLabel msg;
	private WFServerConfigurator configurator;
	private ComponentDisplay display;
	private JButton done, back;
	
	private int labelOff = 20;
	private int h = 20;
	private int yOff = -h;
	private String template;
	private List<WFConfigSettingControl> settings;
	
	public WFConfigCreator(WFServerConfigurator configurator, JLabel msg){
		this.configurator = configurator;
		this.msg = msg;
		this.settings = new ArrayList<>();
		done = new JButton("Create");
		done.addActionListener(this);
		back = new JButton("Back");
		back.addActionListener(this);
		display = new ComponentDisplay();
		boolean success = loadFromTemplate();
		if(success){
			display.addSimple("done", done, 0, yOff += h, 0, 20, 0, 0, 100, 0);
			display.addSimple("back", back, 0, yOff + h, 0, 20, 0, 0, 100, 0);
		} else{
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					abort();
				}
			});
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private final boolean loadFromTemplate() {
		String path = WFConfigFile.getConfigPath(WFConfigFile.FILE_CFG, KEY_TEMPLATE);
		Log.write(this, "Load template from "+path);
		try {
			template = Stream.readToEOF(path);
			List<String> matches = StringUtils.getAllFromPattern(template, "\\{\\{(.*)\\}\\}");
			for(String match : matches){
				WFConfigSettingControl control = new WFConfigSettingControl(match);
				control.load();
				add(control.getName(), control.getComponent());
				settings.add(control);
			}
			return true;
		} catch (IOException e) {
			Log.caught(this, e);
			msg.setText("Error loading template: "+e);
		}
		return false;
	}
	
	/**
	 * 
	 * @param label
	 * @param c
	 */
	private void add(String label, JComponent c){
		display.addSimple(label+"Label", new JLabel(label), 0, yOff += h, 0, h, 0, 0, labelOff, 0);
		display.addSimple(label, c, 0, yOff, 0, h, labelOff, 0, 100 - labelOff, 0);
	}
	
	/**
	 * 
	 * @return
	 */
	public ComponentDisplay getDisplay(){
		return display;
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == done) finish();
		else if(e.getSource() == back) abort();
	}

	/**
	 * 
	 */
	public void abort(){
		if(configurator != null) configurator.switchLayout(null);
	}
	
	/**
	 * 
	 */
	public void finish(){
		Log.write(this, "Finish");
		if(template != null){
			for(WFConfigSettingControl setting : settings){
				String input = "{{"+setting.getSettingsInput()+"}}";
				String result = setting.getSettingResult();
				Log.write(this, "Replace "+input+" with "+result);
				template = template.replace(input, result);
			}
			try {
				Stream.writeToFile(template, new File(WFConfigFile.getCfgLocation()));
				msg.setText("Config created.");
			} catch (IOException e) {
				Log.caught(this, e);
				msg.setText("Error "+e);
			}
		} else{
			msg.setText("Could not find template.");
		}
		abort();
		for(WFConfigSettingControl setting : settings){
			setting.save();
		}
	}
}
