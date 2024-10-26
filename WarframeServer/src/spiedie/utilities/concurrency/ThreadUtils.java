package spiedie.utilities.concurrency;

import java.util.concurrent.Callable;

public class ThreadUtils {
	private ThreadUtils(){}
	public static void sleep(long delay){
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static AbstractThread create(Runnable task){
		return create(null, task, false);
	}
	
	public static AbstractThread create(Callable<Boolean> loopCondition, Runnable task, boolean daemon){
		SingleTaskRunner t = new SingleTaskRunner(loopCondition, task);
		t.setDaemon(daemon);
		t.start();
		return t;
	}
	
}
