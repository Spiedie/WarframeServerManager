package spiedie.terminal.cmdProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import spiedie.utilities.graphics.display.ITextOutput;
import spiedie.utilities.util.log.Log;

public abstract class CommandProcessor<T> implements ICommandProcessor<T>{
	public static final String DEFAULT_DESCRIPTION = "No description.";
	
	protected ITextOutput out;
	protected Map<String, ICommandProcessor<T>> processors;
	
	public CommandProcessor(ITextOutput out){
		processors = new HashMap<>();
		if(!(this instanceof HelpCommand)){
			add(new HelpCommand<T>(out, this), "help", "/?");
		}
		setOutput(out);
	}
	
	public String description(){
		return CommandProcessor.DEFAULT_DESCRIPTION;
	}
	
	public String help(){
		Queue<String> q = new LinkedList<>();
		StringBuilder help = new StringBuilder();
		q.addAll(supported());
		while(!q.isEmpty()){
			String cmd = q.poll();
			if(cmd != null){
				String[] cmds = cmd.split(" ");
				StringBuilder sb = new StringBuilder();
				ICommandProcessor<T> p = this;
				for(int i = 0; i < cmds.length;i++){
					if(sb.length() != 0) sb.append(" ");
					sb.append(cmds[i]);
					p = p.processors().get(cmds[i]);
				}
				for(String s : p.supported()){
					q.add(sb.toString()+" "+s);
				}
				if(p.supported().size() == 2){
					help.append(cmd);
					if(!CommandProcessor.DEFAULT_DESCRIPTION.equals(p.description())) help.append(" => "+p.description());
					help.append("\n");
				}
			}
		}
		return help.toString();
	}
	
	public Set<String> supported(){
		return processors().keySet();
	}
	
	public Map<String, ICommandProcessor<T>> processors(){
		return processors;
	}
	
	protected void println(Object o, String text){
		if(out != null){
			out.println(o, text);
		} else{
			Log.write(o, text, false, true, true);
		}
	}
	
	public boolean processDefault(String arg, T settings) {
		arg = arg.trim();
		String name = getNextArg(arg);
		if(name != null){
			ICommandProcessor<T> p = processors.get(name);
			if(p != null){
				String newArg = arg.substring(name.length());
				newArg = newArg.trim();
				return p.process(newArg, settings);
			}
			else Log.write(this, "Can't find processor for: "+name, false, false, true);
		} else{
			println(this, "Can't find cmd from: "+arg);
		}
		return false;
	}
	
	public void setOutput(ITextOutput out){
		this.out = out;
		for(ICommandProcessor<T> p : processors.values()){
			if(p instanceof CommandProcessor){
				((CommandProcessor<T>) p).setOutput(out);
			}
		}
	}
	
	public void add(ICommandProcessor<T> processor, String... cmd){
		for(String s : cmd) processors.put(s, processor);
	}
	
	public List<String> getOptions(String arg){
		Log.write(this, "Get options from "+arg);
		List<String> list = new ArrayList<>();
		int off = 0;
		boolean quote = false;
		boolean search = false;
		for(int i = 0; i < arg.length();i++){
			char c = arg.charAt(i);
			if(c == '\"') quote = !quote;
			else if(!search && !quote && c == ' '){
				off++;
			} else if(c == '/' || c == '-'){
				off++;
				search = true;
			} else if(search){
				if(c == ' ' && !quote){
					search = false;
					if(arg.charAt(off) == '/' || arg.charAt(off) == '-') off++;
					String s = arg.substring(off, i);
					list.add(s);
					off = i;
				}
			} else {
				// end
				list.add(arg.substring(off).trim());
				i = arg.length();
			}
		}
		if(off < arg.length()){
			String last = arg.substring(off).trim();
			if(last.startsWith("-") || last.startsWith("/")) last = last.substring(1);
			list.add(last);
		}
		return list;
	}
	
	public static CmdArgs getArgs(String arg){
		arg = arg.replace("```", "\"");
		arg = arg.replace("`", "\"");
		List<String> list = new ArrayList<>();
		boolean quote = false;
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < arg.length();i++){
			char c = arg.charAt(i);
			if(c == '\"') quote = !quote;
			if(c == ' ' && !quote){
				list.add(sb.toString());
				sb.setLength(0);
			} else{
				sb.append(c);
			}
			if(i + 1 == arg.length()){
				list.add(sb.toString());
				sb.setLength(0);
			}
		}
		return new CmdArgs().process(list.toArray(new String[list.size()]));
	}
	
	protected String getNextArg(String arg){
		String[] ss = arg.split(" ");
		if(ss != null && ss.length > 0){
			return ss[0];
		}
		return null;
	}
	
	protected String getOption(List<String> ops, String op){
		for(String s : ops){
			int colon = s.indexOf(":");
			if(colon != -1 && s.startsWith(op)){
				String val = s.substring(colon + 1);
				return val;
			}
		}
		return null;
	}
	
	public boolean process(String arg) {
		return process(arg, null);
	}
	
	public static void main(String[] args) throws Exception {
		CmdArgs a = getArgs("-x 5 -y 50 -z \"10 + 5\" -a `test ing` -code ```x = 5;\ny = 10;``` -spaced \"a\nb\"");
		a.forEach(arg -> Log.write(arg.key+" => "+arg.values.toString().replace("\n", "\\n")));
	}
}
