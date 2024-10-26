package spiedie.utilities.jvm;

import java.io.IOException;

import spiedie.utilities.util.Constants;
import spiedie.utilities.util.log.Log;

public class Sys {
	public static final String ADMIN_OVERRIDE = "isAdminOverride";
	
	private static String osName = null;
	private static ProcessWorker worker;
	private static Runtime r = Runtime.getRuntime();
	public static final String commandPrefix;
	
	static {
		osName = System.getProperty("os.name", "").toLowerCase();
		if(isWindows()) {
			commandPrefix = "cmd /c";
		} else if(isLinux()) {
			commandPrefix = "";
		} else {
			commandPrefix = "";
		}
	}
	
	public synchronized static ProcessWorker getWorker(){
		if(worker == null){
			worker = new ProcessWorker();
			worker.start();
		}
		return worker;
	}
	
	public static String execute(String batch) throws IOException{
		return execute(batch, true, false);
	}
	
	public static String execute(String batch, boolean out, boolean err) throws IOException{
		ProcessTask task = executeTask(batch);
		StringBuilder sb = new StringBuilder();
		if(out) sb.append(task.getResult());
		if(out && err) sb.append("\n");
		if(err) sb.append(task.getErrorResult());
		if(!out && !err) task.getResult();
		task.getProcess().getOutputStream().close();
		return sb.toString();
	}
	
	public static ProcessTask executeTask(String batch) throws IOException{
		return executeTask(getWorker(), batch);
	}
	
	public static ProcessTask executeTask(ProcessWorker worker, String batch) throws IOException{
		Process p = r.exec(commandPrefix+batch);
		ProcessTask task = new ProcessTask(p);
		worker.add(task);
		return task;
	}
	
	public static boolean isAdmin() {
		if(Constants.isSet(ADMIN_OVERRIDE)) return Constants.use(ADMIN_OVERRIDE, Constants.KEY_TRUE, false);
		// windows
		String dism = "";
		try {
			dism = execute("dism");
		} catch (IOException e) {
			Log.caught(Sys.class, e);
		}
		if(dism.length() < 50) Log.err(Sys.class, "Dism probably isn't available, detection inconclusive.");
		if(!dism.contains("740")) return true;
		return false;
	}
	
	public static boolean isWindows() {
		return osName.contains("windows");
	}
	
	public static boolean isLinux() {
		return osName.contains("linux");
	}
	
}
