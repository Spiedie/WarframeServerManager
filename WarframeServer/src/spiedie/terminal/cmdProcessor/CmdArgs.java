package spiedie.terminal.cmdProcessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CmdArgs implements Iterable<CmdArg>{
	public static final String DEFAULT_ARG = "default";
	public static final char[] ARG_STARTS = {'-', '~', '/'};
	private List<CmdArg> list;
	public CmdArgs(){
		list = new ArrayList<>();
	}
	
	public CmdArgs process(String... args){
		addDefaultArg(args);
		for(int i = 0; i < args.length;i++){
			if(args[i] != null && args[i].length() > 0){
				if(isValidArg(args[i])) handleValidArg(args, i);
			}
		}
		return this;
	}
	
	private boolean isValidArg(String arg){
		for(int c = 0; c < ARG_STARTS.length;c++){
			if(!arg.isEmpty() && arg.charAt(0) == ARG_STARTS[c]){
				return true;
			}
		}
		return false;
	}
	
	private void addDefaultArg(String[] args){
		// -key v1 v2 "-v3"
		CmdArg a = new CmdArg();
		a.key = DEFAULT_ARG;
		int i = 0;
		while(args.length > i && !isValidArg(args[i])){
			String val = args[i];
			if(val.startsWith("\"") && val.endsWith("\"")) val = val.substring(1, val.length() - 1);
			a.values.add(val);
			i++;
		}
		if(!a.values.isEmpty()) list.add(a);
	}
	
	private void handleValidArg(String[] args, int i){
		if(args[i].contains(":")){
			handleKVArg(args, i);
		} else if(args.length > i + 1 && !isValidArg(args[i+1])){
			handleSplitKVArg(args, i);
		} else{
			handleEnableArg(args, i);
		}
	}
	
	private void handleEnableArg(String[] args, int i){
		CmdArg a = new CmdArg();
		a.key = args[i].substring(1);
		list.add(a);
	}
	
	private void handleKVArg(String[] args, int i){
		int split = args[i].indexOf(":");
		CmdArg a = new CmdArg();
		a.key = args[i].substring(0, split).substring(1);
		String val = args[i].substring(split + 1);
		if(val.startsWith("\"") && val.endsWith("\"")) val = val.substring(1, val.length() - 1);
		a.values.add(val);
		list.add(a);
	}
	
	private void handleSplitKVArg(String[] args, int i){
		// -key v1 v2 "-v3"
		CmdArg a = new CmdArg();
		a.key = args[i].substring(1);
		while(args.length > i + 1 && !isValidArg(args[i+1])){
			String val = args[i+1];
			if(val.startsWith("\"") && val.endsWith("\"")) val = val.substring(1, val.length() - 1);
			a.values.add(val);
			i++;
		}
		list.add(a);
	}
	
	public Iterator<CmdArg> iterator() {
		return list.iterator();
	}
	
	public boolean isSet(String key){
		return getArg(key) != null;
	}
	
	public String getValue(String key){
		CmdArg a = getArg(key);
		return a == null ? null : a.getValue();
	}
	
	public CmdArg getArg(String key){
		for(CmdArg a : this){
			if(key.equals(a.key)) return a;
		}
		return null;
	}
	
	public String toCmd(){
		StringBuilder sb = new StringBuilder();
		for(CmdArg a : this){
			sb.append(a.toCmd()+" ");
		}
		return sb.toString();
	}
	
	public String toString(){
		return toCmd();
	}
}
