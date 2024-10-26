package spiedie.utilities.concurrency;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import spiedie.utilities.stream.Stream;
import spiedie.utilities.util.Time;

public abstract class SimpleQueueProcessor<T> extends AbstractThread implements Closeable{
	protected long baseDelay = 1;
	protected long delay = 1;
	protected Queue<T> q;
	public SimpleQueueProcessor(){
		super(true);
		setQueue(new ConcurrentLinkedDeque<T>());
	}
		
	public void add(T t){
		q.add(t);
	}
	
	public Queue<T> getQueue(){
		return q;
	}
	
	public void setQueue(Queue<T> q){
		this.q = Objects.requireNonNull(q);
	}
	
	public void run() {
		try{
			long currentDelay = baseDelay;
			while(isRunning()){
				update();
				taskStarted();
				T t = q.poll();
				if(t != null){
					currentDelay = baseDelay;
					updateProcessing();
					process(t);
					taskEnded();
				} else if(q.isEmpty()){
					currentDelay = Math.max(1, Math.min(delay, currentDelay * 2));
					taskEnded();
					updateWaiting();
					Time.sleep(currentDelay);
				}
			}
		} finally {
			setFinished(true);
			Stream.close(this);
		}
	}
	
	public void close() throws IOException{
		setFinished(true);
		if(taskInProgress()) {
			taskEnded();
		}
	}
	
	protected abstract void process(T t);
	
	protected void update() {
		
	}

	protected void updateProcessing() {
		
	}

	protected void updateWaiting() {
		
	}
}
