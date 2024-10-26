package spiedie.warframe.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import spiedie.utilities.data.StringUtils;
import spiedie.utilities.util.log.Log;
import spiedie.utilities.util.persistentInfo.Info;

public class WFConfigSettingControl {
	public static final String KEY_PREVIOUS_CHOICE = "WFSetting"+File.separator+"Choice";
	public static final String CONTROL_DROPDOWN = "Dropdown";
	public static final String CONTROL_TEXTFIELD = "TextField";
	
	public static final int IDX_CONTROL = 0;
	public static final int IDX_SETTING = 1;
	public static final int IDX_NAME = 2;
	public static final int IDX_FIELD_FORMAT = 3;
	
	private String settings;
	private String[] args;
	
	private DefaultComboBoxModel<String> model;
	private JComboBox<String> box;
	private Map<String, String> opMapping;
	
	private JTextField field;
	
	public WFConfigSettingControl(String settings){
		this.settings = settings;
		args = settings.split(",");
		if(getControl().equalsIgnoreCase(CONTROL_DROPDOWN)){
			opMapping = new HashMap<>();
			model = new DefaultComboBoxModel<>();
			for(int i = 3; i < args.length;i++){
				if(args[i].contains("[")){
					String displayName = StringUtils.getFromPattern(args[i], "(.*)\\[");
					String value = StringUtils.getFromPattern(args[i], "\\[(.*)\\]");
					opMapping.put(displayName, value);
					model.addElement(displayName);
				} else{
					opMapping.put(args[i], args[i]);
					model.addElement(args[i]);
				}
			}
			box = new JComboBox<>(model);
		} else if(getControl().equalsIgnoreCase(CONTROL_TEXTFIELD)){
			field = new JTextField();
		}
	}
	
	public String getSettingsInput(){
		return settings;
	}
	
	public String getControl(){
		return args[IDX_CONTROL];
	}
	
	public String getSetting(){
		return args[IDX_SETTING];
	}
	
	public String getName(){
		return args[IDX_NAME];
	}
	
	private String getChoiceKey(){
		return KEY_PREVIOUS_CHOICE+getSetting();
	}
	
	private boolean isControl(String control){
		return getControl().equalsIgnoreCase(control);
	}
	
	public JComponent getComponent(){
		if(isControl(CONTROL_DROPDOWN)) return box;
		if(isControl(CONTROL_TEXTFIELD)) return field;
		return null;
	}
	
	private String getControlResult(){
		if(isControl(CONTROL_DROPDOWN)) return opMapping.get(box.getSelectedItem());
		if(isControl(CONTROL_TEXTFIELD)) return String.format(args[IDX_FIELD_FORMAT], field.getText());
		return null;
	}
	
	public String getSettingResult(){
		return getSetting()+"="+getControlResult();
	}
	
	public void load(){
		Log.write(this, "Load "+getSetting());
		try {
			if(Info.isSet(getChoiceKey()) && getComponent() != null){
				String choice = Info.getProperty(getChoiceKey());
				Log.write(this, "Load choice "+choice+" for "+getChoiceKey());
				if(isControl(CONTROL_DROPDOWN)) model.setSelectedItem(choice);
				else if(isControl(CONTROL_TEXTFIELD)) field.setText(choice);
			}
		} catch (IOException e) {
			Log.err(this, e);
		}
	}
	
	public void save(){
		Log.write(this, "Save "+getSetting());
		try {
			String choice = null;
			if(isControl(CONTROL_DROPDOWN)) choice = model.getSelectedItem().toString();
			else if(isControl(CONTROL_TEXTFIELD)) choice = field.getText();
			if(choice != null){
				Log.write(this, "Save choice "+choice+" for "+getChoiceKey());
				Info.setProperty(getChoiceKey(), choice);
			}
		} catch (IOException e) {
			Log.err(this, e);
		}
	}
}
