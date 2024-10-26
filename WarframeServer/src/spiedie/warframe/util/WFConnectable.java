package spiedie.warframe.util;

public interface WFConnectable {
	/**
	 * 
	 * @return true iff the connection is still alive.
	 */
	public boolean isRunning();
	
	/**
	 * Set whether the connection should be finished.
	 * @param finished
	 */
	public void setFinished(boolean finished);
}
