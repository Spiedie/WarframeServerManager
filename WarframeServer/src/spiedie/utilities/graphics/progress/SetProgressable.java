package spiedie.utilities.graphics.progress;

import spiedie.utilities.util.ISettings;
import spiedie.utilities.util.MemorySettings;

public class SetProgressable implements ISetProgressable{

	protected long val, max;
	protected String file;
	
	public void setValue(long val){
		this.val = val;
	}
	
	public void setMax(long val){
		this.max = val;
	}
	
	public void setFile(String file){
		this.file = file;
	}
	
	public void addValue(long val){
		setValue(getValue() + val);
	}
	
	public long getValue() {
		return val;
	}

	public long getMax() {
		return max;
	}

	public String getRequestedFile() {
		return file;
	}

	public String toString(){
		return "SetProgressable("+getValue()+"/"+getMax()+")";
	}

	public ISettings getSettings() {
		return new MemorySettings();
	}
}
