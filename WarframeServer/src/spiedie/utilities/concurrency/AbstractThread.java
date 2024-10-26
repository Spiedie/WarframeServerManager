package spiedie.utilities.concurrency;

public abstract class AbstractThread implements Runnable, IThread{
	protected boolean isStarted;
	protected boolean isFinished;
	protected boolean isDaemon;
	protected boolean onTask;
	protected Thread t;
	
	protected AbstractThread(){
		this(false);
	}
	
	protected AbstractThread(boolean daemon){
		setDaemon(daemon);
		isStarted = false;
		isFinished = false;
	}
	
	public void setDaemon(boolean val){
		isDaemon = val;
	}
	
	public void start(){
		start(getClass().getSimpleName());
	}
	
	public void start(String threadName){
		this.t = new Thread(this, threadName);
		startImpl();
	}
	
	public void startImpl(){
		this.t.setDaemon(isDaemon);
		isStarted = true;
		isFinished = false;
		this.t.start();
	}
	
	public boolean isRunning(){
		return isStarted && !isFinished;
	}
	
	public boolean isFinished(){
		return isFinished;
	}

	public void taskStarted(){
		onTask = true;
	}
	
	public void taskEnded(){
		onTask = false;
	}
	
	public boolean taskInProgress(){
		return onTask;
	}

	public void setFinished(boolean value){
		isFinished = value;
	}
	
}
