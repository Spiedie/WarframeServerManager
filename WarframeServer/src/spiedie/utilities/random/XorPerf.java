package spiedie.utilities.random;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import spiedie.utilities.util.Time;

public class XorPerf {
	public static final long BATCH_SIZE = 40000;
	public long time, batches;
	
	/**
	 * 
	 * @return
	 */
	public double calc(){
		double d = batches * BATCH_SIZE;
		d /= time;
		d /= 1000000;
		d *= 3.5;// legacy scale
		return d;
	}
	
	public String toString(){
		return time+" ms";
	}
	
	/**
	 * 
	 * @param threads
	 * @param batches
	 * @return
	 */
	public static XorPerf getPerformance(int threads, int batches){
		List<XorPerfThread> ts = new ArrayList<>();
		Queue<XorPerfTask> tasks = new ConcurrentLinkedDeque<XorPerfTask>();
		for(int i = 0; i < threads;i++){
			XorPerfThread t = new XorPerfThread();
			t.setQueue(tasks);
			t.start();
			ts.add(t);
		}
		long time = Time.millis();
		for(int i = 0; i < batches;i++){
			tasks.add(new XorPerfTask(BATCH_SIZE));
		}
		while(!tasks.isEmpty()) Time.sleep(1);
		for(XorPerfThread t : ts){
			while(t.taskInProgress()) Time.sleep(1);
			t.setFinished(true);
		}
		time = Time.millis() - time;
		XorPerf res = new XorPerf();
		res.batches = batches;
		res.time = time;
		return res;
	}
	
	/**
	 * 
	 * @param threads
	 * @return
	 */
	public static double getPerformance(int threads){
		int batches = 16;
		XorPerf perf = null;
		while((perf = getPerformance(threads, batches *= 2)).time < 1000);
		return perf.calc();
	}
	
}
