package spiedie.utilities.concurrency;

public interface IThread{
	public void start();
	public boolean isRunning();
	public void setFinished(boolean finished);
}
