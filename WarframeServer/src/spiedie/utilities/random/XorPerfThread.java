package spiedie.utilities.random;

import spiedie.utilities.concurrency.SimpleQueueProcessor;

public class XorPerfThread extends SimpleQueueProcessor<XorPerfTask>{
	private XOR64Random r;
	
	public XorPerfThread(){

	}
	
	public void run(){
		r = new XOR64Random();
		super.run();
	}
	
	protected void process(XorPerfTask t) {
		long sum = 0;
		for(long i = 0; i < t.size;i++){
			sum += r.next(32);
		}
		t.result = sum;
	}

}
