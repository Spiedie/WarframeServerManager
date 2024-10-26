package spiedie.terminal.cmdProcessor;

import java.util.Scanner;

import spiedie.utilities.concurrency.AbstractThread;
import spiedie.utilities.util.Time;

public class ConsoleHandler<T> extends AbstractThread{
	private ICommandProcessor<T> processor;
	private ISettingsFactory<T> factory;
	public ConsoleHandler(ICommandProcessor<T> processor){
		this.processor = processor;
	}
	
	public ConsoleHandler<T> setFactory(ISettingsFactory<T> factory){
		this.factory = factory;
		return this;
	}
	
	public void run() {
		Scanner sc = new Scanner(System.in);
		int delay = 100;
		while(isRunning()){
			if(sc.hasNextLine()){
				String cmd = sc.nextLine();
				execute(cmd);
			} else {
				Time.sleep(delay);
			}
		}
		setFinished(true);
	}
	
	public void execute(String cmd){
		processor.process(cmd, factory == null ? null : factory.getEmpty());
	}
}
