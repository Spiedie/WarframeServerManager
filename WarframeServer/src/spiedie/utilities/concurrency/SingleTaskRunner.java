package spiedie.utilities.concurrency;

import java.util.concurrent.Callable;

import spiedie.utilities.util.log.Log;

public class SingleTaskRunner extends AbstractThread{
	private Runnable r;
	private Callable<Boolean> condition;
	private static int id;
	public SingleTaskRunner(Callable<Boolean> condition, Runnable r){
		this.r = r;
		this.condition = condition;
	}
	
	public void start(){
		start("SingleTaskRunner-"+(id++));
	}
	
	public void run() {
		try {
			if(condition == null) r.run();
			else while(condition.call()) r.run();
		} catch (Exception e) {
			Log.caught(this, e);
		} finally{
			setFinished(true);
		}
	}
}
