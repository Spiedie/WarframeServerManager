package spiedie.utilities.jvm;

import spiedie.utilities.util.Time;

public class ProcessTask {
	private Process p;
	private String in, err;
	private boolean resultSet = false;
	public ProcessTask(Process p){
		this.p = p;
	}
	
	public Process getProcess(){
		return p;
	}
	
	public String getResult(){
		while(!resultSet) Time.sleep(1);
		return in;
	}
	
	public String getErrorResult(){
		while(!resultSet) Time.sleep(1);
		return err;
	}
	
	public void setResult(String in, String err){
		this.in = in;
		this.err = err;
		resultSet = true;
	}
}
