package spiedie.utilities.graphics;

import java.io.Closeable;

import javax.swing.JFrame;

import spiedie.utilities.util.Time;

public abstract class AbstractFrame implements Showable, Closeable{
	private Object frameLock = new Object();
	private JFrame frame;
	private boolean initialized;
	private boolean closed;
	protected int x;
	protected int y;
	public AbstractFrame(boolean autorun){
		if(autorun) {
			create();
		}
	}
	
	protected abstract void initFrame();
	
	public void create(){
		initialized = false;
		GraphicsUtils.runOnEDT(this);
		while(!initialized) Time.sleep(1);
	}
	
	protected void setFrame(JFrame f){
		synchronized(frameLock){
			if(frame != null) throw new IllegalStateException("Frame already set.");
			frame = f;
		}
	}
	
	protected JFrame frame(){
		synchronized(frameLock){
			if(frame == null) setFrame(new JFrame());
		}
		return frame;
	}
	
	public void setTitle(String title){
		frame().setTitle(title);
	}
	
	public void createAndShowGUI(){
		frame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initFrame();
		initialized = true;
	}
	
	public void close(){
		if(!closed) {
			frame.dispose();
		}
		closed = true;
	}
}
