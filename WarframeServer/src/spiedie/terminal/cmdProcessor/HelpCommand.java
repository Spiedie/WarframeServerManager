package spiedie.terminal.cmdProcessor;

import spiedie.utilities.graphics.display.ITextOutput;

public class HelpCommand<T> extends CommandProcessor<T>{
	
	private ICommandProcessor<T> p;
	public HelpCommand(ITextOutput out, ICommandProcessor<T> p){
		super(out);
		this.p = p;
	}
	
	public String help(){
		return "Help command, use to display help message for the commands.";
	}
	
	public boolean process(String arg, T settings) {
		if(out != null){
			String msg = p.help();
			if(msg == null) msg = "";
			if(msg.endsWith("\n")) msg = msg.substring(0, msg.length() - 1);
			out.println(p, msg);
		}
		return true;
	}
}
