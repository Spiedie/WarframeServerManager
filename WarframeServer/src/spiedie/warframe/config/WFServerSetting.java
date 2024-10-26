package spiedie.warframe.config;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;

public class WFServerSetting {
	public static int MAX_INSTANCES_OF_TYPE = 32;
	private JLabel description;
	private JComboBox<String> instances;
	private DefaultComboBoxModel<String> instancesModel;
	
	public WFServerSetting(String name){
		description = new JLabel(name);
		instancesModel = new DefaultComboBoxModel<>();
		for(int i = 0; i <= MAX_INSTANCES_OF_TYPE;i++){
			instancesModel.addElement(String.valueOf(i));
		}
		instances = new JComboBox<>(instancesModel);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getName(){
		return getLabel().getText();
	}
	
	/**
	 * 
	 * @return
	 */
	public JLabel getLabel(){
		return description;
	}
	
	/**
	 * 
	 * @return
	 */
	public DefaultComboBoxModel<String> getModel(){
		return instancesModel;
	}
	
	/**
	 * 
	 * @return
	 */
	public JComboBox<String> getDropdown(){
		return instances;
	}
}
