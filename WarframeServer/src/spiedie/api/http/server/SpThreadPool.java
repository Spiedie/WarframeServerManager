package spiedie.api.http.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import spiedie.utilities.concurrency.AbstractThread;
import spiedie.utilities.util.Time;

public class SpThreadPool extends AbstractThread {
	public static final int DEFAULT_PARALLELISM = 8;
	public static final int DEFAULT_IDLE_TIMEOUT = 10000;
	
	private Queue<SpHttpRequestHandler> queue;
	private List<SpHttpRequestHandlerThread> threadPool;
	private int parallelism = DEFAULT_PARALLELISM;
	public SpThreadPool() {
		this.queue = new ConcurrentLinkedDeque<>();
		this.threadPool = Collections.synchronizedList(new ArrayList<>());
		this.setDaemon(true);
		createThread(0);
		this.start();
	}
	
	private void createThread(long idleTImeout) {
		SpHttpRequestHandlerThread t = new SpHttpRequestHandlerThread();
		t.setIdleTimeout(idleTImeout);
		t.setQueue(this.queue);
		t.start();
		threadPool.add(t);
	}
	
	public void add(SpHttpRequestHandler thread) {
		if(threadPool.size() < parallelism) {
			createThread(DEFAULT_IDLE_TIMEOUT);
		}
		queue.add(thread);
	}

	public void run() {
		while(isRunning()) {
			int size = threadPool.size();
			for(int i = size - 1; i >= 0;i--) {
				if(threadPool.get(i).isFinished()) {
					threadPool.remove(i);
				}
			}
			Time.sleep(DEFAULT_IDLE_TIMEOUT);
		}
	}
}
