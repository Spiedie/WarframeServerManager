package spiedie.utilities.concurrency;

import spiedie.utilities.util.Time;

public abstract class AsyncCallHelper<T> implements Runnable{
	protected long timeout, startTime;
	protected T result;
	public AsyncCallHelper(){
		
	}
	
	/**
	 * 
	 * @param time
	 */
	
	public void run() {
		startTime = Time.millis();
		fillResult();
	}
	
	/**
	 * 
	 */
	public abstract void fillResult();
	
	/**
	 * 
	 * @return the default value.
	 */
	public abstract T getDefaultResult();

}
