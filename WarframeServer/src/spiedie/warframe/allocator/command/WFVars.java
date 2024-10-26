package spiedie.warframe.allocator.command;

import java.util.Scanner;

import spiedie.utilities.util.ISettings;
import spiedie.utilities.util.MemorySettings;

public class WFVars{
	private static ISettings instance;
	public static synchronized ISettings getInstance(){
		if(instance == null){
			instance = new MemorySettings();
		}
		return instance;
	}
	
	public static String eval(String command){
		if(command == null) return null;
		Scanner sc = new Scanner(command);
		while(sc.hasNext()){
			String token = sc.next();
			if(token.toLowerCase().startsWith("var.")){
				int dot = token.indexOf('.');
				if(dot >= 0){
					String var = token.substring(dot + 1);
					if(getInstance().isSet(var)){
						command = command.replace(token, getInstance().getProperty(var));
					}
				}
			}
		}
		return command;
	}
}
