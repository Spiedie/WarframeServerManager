package spiedie.utilities.process;

public class ProcessUtils {
	private ProcessUtils(){}
	public static boolean isRunning(Process p){
		return !isFinished(p);
	}
	
	public static boolean isFinished(Process p){
		try{
			p.exitValue();
			return true;
		} catch(IllegalThreadStateException e){
			
		}
		return false;
	}
	
}
