package spiedie.terminal.cmdProcessor;

import java.util.ArrayList;
import java.util.List;

public class CmdArg {
	public String key;
	public List<String> values;
	
	public CmdArg(){
		values = new ArrayList<>();
	}
	
	public String getValue(){
		return values.size() >= 1 ? values.get(0) : null;
	}
	
	public String toCmd(){
		StringBuilder sb = new StringBuilder();
		sb.append(CmdArgs.ARG_STARTS[0]+key);
		for(String val : values){
			sb.append(" \""+val+"\"");
		}
		return sb.toString();
	}
	
	public String toString(){
		return key+(values.isEmpty() ? "" : ":"+values);
	}
}
