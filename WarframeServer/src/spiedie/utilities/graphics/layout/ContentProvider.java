package spiedie.utilities.graphics.layout;

import javax.swing.JComponent;

public class ContentProvider {
	protected String name;
	protected JComponent c;
	public ContentProvider(){
		
	}
	
	public ContentProvider(JComponent c, String name){
		setName(name);
		setComponent(c);
	}
	
	public String getName() {
		return name;
	}

	public JComponent getComponent() {
		return c;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setComponent(JComponent comp) {
		this.c = comp;
	}
}
